package com.mimiworld.mimiworld.event;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.ModConfig;
import com.mimiworld.mimiworld.ModItems;
import com.mimiworld.mimiworld.effect.ModEffects;
import com.mimiworld.mimiworld.enchantment.ModEnchantments;
import com.mimiworld.mimiworld.entity.CompoundAzurePythonEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 伤害结算总览（Forge 1.20.1）。本模组所有「修改伤害数值 / 伤害相关结算」的监听器都集中在这里，
 * 按一个固定顺序表达为一条伤害公式。
 *
 * <p><b>两个 Forge 事件的触发时机（正确版）：</b></p>
 * <ul>
 *   <li>{@link LivingHurtEvent} 在 {@code LivingEntity#actuallyHurt} 最开头触发，
 *       <b>早于</b>盔甲、保护附魔、抗性提升、伤害吸收。此处的 {@code event.getAmount()} 是
 *       <b>护甲前数值</b>（并非最终伤害）。</li>
 *   <li>{@link LivingDamageEvent} 在盔甲/附魔/抗性/吸收全部结算之后、真正扣血之前触发，
 *       此时 {@code event.getAmount()} 才是本次会从生命值里扣除的最终数值。</li>
 *   <li>{@link CriticalHitEvent} 在原版暴击判定处触发，属于攻击力/暴击环节。</li>
 * </ul>
 *
 * <p><b>完整伤害链条（加法区 × (1 + 乘法区) − 抵消）：</b></p>
 * <pre>
 * 基础攻击力 / 暴击判定（CriticalHitEvent：月有圆缺掷骰）
 *   ↓
 * 加法区 —— 武器基础攻击 + 固定数值加成（如意 +5、月有圆缺 +10 等属性修饰符），
 *           进入 LivingHurtEvent 时已经包含在 event.getAmount() 里
 *   ↓
 * 乘法区 —— 阶段 1（LivingHurtEvent，护甲前）：下列百分比先<b>求和</b>，最后只 +1 一次
 *   1. [combat] 攻防倍率   (mobDamageMultiplier − 1) + (mobDamageTakenMultiplier − 1)
 *   2. 腥红烛照            +(amplifier + 1)          （原 ×(amplifier + 2)）
 *   3. 蟒蛇缠绕            +0.5
 *   4. 残堕化              −0.03 × (amplifier + 1)
 *   5. 月有圆缺（受击）    +0.25；5 格内怪物来源另有 20% 概率再 +0.5
 *   6. 易怒                受击方维护 1 秒窗口；攻击方 25% 概率 +(0.05 × min(等级, 5))
 *   7. 天青轮盘            50% +1.0、50% −0.5
 *   8. 双倍快乐            +1.0
 *   9. 多多益善            +0.1 × min(10×10 范围内怪物数, 20)
 *  10. 山海恩泽（下落）    +下落格数 × 0.01
 *  11. 迅击                +附魔等级 × 当前移动速度值
 *   合计 percent 下限 −100%，最终写入 event.setAmount(加法区 × (1 + percent))；
 *   随后由暗灵毒牙附加暗毒（不改数值）
 *   ★ 虎王之蔑（LOWEST）  在上述求和之后：取消事件，把金额拆成 5 次 ×20% 排队延迟结算
 *   ↓
 * 抵消 —— 原版：盔甲 → 保护附魔 → 抗性提升 → 伤害吸收（连乘，不归本类处理）
 *   ↓
 * 阶段 2 —— LivingDamageEvent（护甲后最终数值，只做非加伤结算）
 *   泣血                  按最终伤害的 6% + 2% × (等级 − 1) 回复攻击方
 *   米米圣盾              受伤后获得 10% 最大生命值的吸收，累计上限 40%
 *   逆鳞龙胆（LOWEST）    单次受伤封顶为最大生命值的 20%
 *   ↓
 * 扣血
 * </pre>
 *
 * <p>注意：虎王之蔑的延迟伤害类型 {@code mimiworld:tiger_king_disdain} 带 {@code bypasses_armor}，
 * 原版不会做盔甲减伤、也就不会调用 hurtArmor 扣耐久，因此本类按原版公式手动补扣护甲耐久。</p>
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamagePipeline {

    /** 月有圆缺：暴击率、受伤倍率、附近怪物额外伤害的概率与倍率。 */
    private static final float MOON_CRIT_CHANCE = 0.30F;
    /** AttributesLib 的暴击率属性（未安装时注册表中不存在，退化为事件概率判定）。 */
    private static final ResourceLocation MOON_CRIT_CHANCE_ID =
            new ResourceLocation("attributeslib", "crit_chance");
    private static final float MOON_DAMAGE_TAKEN_MULTIPLIER = 1.25F;
    private static final double MOON_MONSTER_RADIUS = 5.0D;
    private static final float MOON_MONSTER_CHANCE = 0.20F;
    private static final float MOON_MONSTER_EXTRA_MULTIPLIER = 1.5F;

    /** 易怒：受伤后的窗口时长、触发概率、每级增伤与等级上限。 */
    private static final int IRRITABLE_WINDOW_TICKS = 20;
    private static final float IRRITABLE_CHANCE = 0.25F;
    private static final float IRRITABLE_PER_LEVEL = 0.05F;
    private static final int IRRITABLE_MAX_LEVEL = 5;

    /** 各实体「易怒」窗口的到期游戏刻（键为实体 UUID），到期前攻击才能触发增伤。 */
    private static final Map<UUID, Long> IRRITABLE_WINDOWS = new HashMap<>();

    /** 暗灵毒牙：命中时附加的暗毒持续时间（3 秒）。 */
    private static final int DARK_POISON_DURATION = 60;

    /** 上古毛皮：易怒最高 5 级、10 秒（200 tick）未触发降级、效果刷新时长 8 秒（剩余 ≤3 秒时重置，每 5 秒一次）。 */
    private static final int PELT_MAX_LEVEL = 5;
    private static final long PELT_DECAY_TICKS = 200L;
    private static final int PELT_EFFECT_DURATION = 160;
    private static final int PELT_REFRESH_REMAINING = 60;

    /** 佩戴上古毛皮的玩家当前的易怒等级与最近一次增伤触发时刻。 */
    private static final Map<UUID, PeltRageState> PELT_STATES = new HashMap<>();

    /** 上古毛皮的易怒升级状态。 */
    private static final class PeltRageState {
        private int level = 1;
        private long lastProcTick;

        private PeltRageState(long now) {
            this.lastProcTick = now;
        }
    }

    /** 多多益善：水平判定半径（10×10 范围）、每只怪物增伤比例、计入上限。 */
    private static final double MERRIER_RADIUS = 5.0D;
    private static final float MERRIER_PER_MONSTER = 0.1F;
    private static final int MERRIER_MAX_MONSTERS = 20;

    /** 米米圣盾：每次受伤的额外生命值比例与累计上限。 */
    private static final float MIMI_SHIELD_GAIN_RATIO = 0.1F;
    private static final float MIMI_SHIELD_CAP_RATIO = 0.4F;

    /** 虎王之蔑延迟伤害的自定义伤害类型（data/mimiworld/damage_type/tiger_king_disdain.json）。 */
    private static final ResourceLocation DELAYED_DAMAGE_ID =
            new ResourceLocation(MimiWorld.MODID, "tiger_king_disdain");
    private static final ResourceKey<DamageType> DELAYED_DAMAGE_KEY =
            ResourceKey.create(Registries.DAMAGE_TYPE, DELAYED_DAMAGE_ID);
    private static final int DELAY_INSTALLMENTS = 5;
    private static final int DELAY_INTERVAL_TICKS = 20;
    private static final float DELAY_FRACTION = 0.2F;

    /** 每个玩家独立排队的延迟伤害；同一玩家可同时存在多笔，每笔各自计时。 */
    private static final Map<UUID, List<DelayedDamage>> DELAYED_DAMAGES = new HashMap<>();

    /** 一笔延迟伤害的结算状态。 */
    private static final class DelayedDamage {
        private final DamageSource source;
        private final float amount;
        private int remaining = DELAY_INSTALLMENTS;
        private int ticksUntilNext = DELAY_INTERVAL_TICKS;

        private DelayedDamage(DamageSource source, float amount) {
            this.source = source;
            this.amount = amount;
        }
    }

    // ------------------------------------------------------------------
    // 阶段 1：LivingHurtEvent（护甲前倍率）
    // ------------------------------------------------------------------

    /**
     * 阶段 1 主入口：把加法区与乘法区分开结算。
     *
     * <p>顺序即公式顺序：攻防倍率 → 腥红烛照 → 蟒蛇缠绕 → 残堕化 → 月有圆缺 → 易怒
     * → 天青轮盘 → 双倍快乐 → 多多益善 → 山海恩泽 → 迅击；各方法只返回百分比，
     * 求和后统一 ×(1 + 合计)，再附加暗毒。
     * 各方法内部的条件（客户端、延迟伤害、攻击方追溯、概率）与旧监听器逐字一致，
     * 条件不满足时返回 0 百分比，不会提前终止整条管线。</p>
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 加法区：event.getAmount() 已含武器基础攻击与固定加成（如意、月有圆缺等属性修饰符）
        float additive = event.getAmount();
        // 乘法区：所有百分比加成先求和，最后只 +1 一次（不逐个复利相乘）
        float percent = 0.0F;
        percent += configBonusPercent(event);
        percent += crimsonCandlelightBonusPercent(event);
        percent += pythonConstrictionBonusPercent(event);
        percent += remnantCorruptionBonusPercent(event);
        percent += moonPhasesBonusPercent(event);
        percent += irritableBonusPercent(event);
        percent += azureWheelBonusPercent(event);
        percent += doubleHappinessBonusPercent(event);
        percent += moreTheMerrierBonusPercent(event);
        percent += shanhaiGraceBonusPercent(event);
        percent += swiftStrikeBonusPercent(event);
        // 伤害不可为负：合计最低 −100%
        percent = Math.max(percent, -1.0F);
        event.setAmount(additive * (1.0F + percent));
        // 暗灵毒牙只附加暗毒，不改数值
        applyDarkSpiritFang(event);
    }

    /** 公式 1：[combat] 攻防倍率 —— 攻击方为本模组生物 +(mobDamageMultiplier − 1)，受击方为本模组生物 +(mobDamageTakenMultiplier − 1)。 */
    private static float configBonusPercent(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return 0.0F;
        }

        double percent = 0.0D;
        // 追溯攻击方：直接实体 -> 投掷物发射者
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof Projectile projectile) {
            attacker = projectile.getOwner();
        }
        if (attacker instanceof LivingEntity livingAttacker && ModConfigEvents.isModMob(livingAttacker)) {
            percent += ModConfig.mobDamageMultiplier() - 1.0D;
        }
        if (ModConfigEvents.isModMob(target)) {
            percent += ModConfig.mobDamageTakenMultiplier() - 1.0D;
        }
        return (float) percent;
    }

    /**
     * 公式 2：腥红烛照 —— 攻击方持有该效果时 +(amplifier + 1)（原 ×(amplifier + 2)）。
     *（amplifier=0 → +100%，amplifier=1 → +200%……；攻击方按 直接实体 → 投掷物发射者 追溯。）
     */
    private static float crimsonCandlelightBonusPercent(LivingHurtEvent event) {
        // 只在服务端处理（LivingHurtEvent 可能在客户端也收到，但修改伤害只服务端有效）
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return 0.0F;
        }

        DamageSource source = event.getSource();
        if (isDelayedDamage(source)) {
            return 0.0F;
        }

        Entity directEntity = source.getEntity();
        Entity attacker = directEntity;
        if (directEntity instanceof Projectile projectile) {
            attacker = projectile.getOwner();
        }

        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return 0.0F;
        }

        var effectInstance = livingAttacker.getEffect(ModEffects.CRIMSON_CANDLELIGHT.get());
        if (effectInstance == null) {
            return 0.0F;
        }

        return effectInstance.getAmplifier() + 1.0F;
    }

    /** 公式 3：蟒蛇缠绕 —— 受击者为复晶天青蟒且攻击方持有该效果时 +0.5（与其它百分比求和）。 */
    private static float pythonConstrictionBonusPercent(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide || !(target instanceof CompoundAzurePythonEntity)) {
            return 0.0F;
        }

        DamageSource source = event.getSource();
        if (isDelayedDamage(source)) {
            return 0.0F;
        }
        Entity directEntity = source.getEntity();
        Entity attacker = directEntity;
        if (directEntity instanceof Projectile projectile) {
            attacker = projectile.getOwner();
        }

        if (attacker instanceof LivingEntity livingAttacker
                && livingAttacker.hasEffect(ModEffects.PYTHON_CONSTRICTION.get())) {
            return 0.5F;
        }
        return 0.0F;
    }

    /** 公式 4：残堕化 —— 受击方持有该效果时 −0.03 × (amplifier + 1)。 */
    private static float remnantCorruptionBonusPercent(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return 0.0F;
        }
        if (isDelayedDamage(event.getSource())) {
            return 0.0F;
        }

        var effectInstance = target.getEffect(ModEffects.REMNANT_CORRUPTION.get());
        if (effectInstance == null) {
            return 0.0F;
        }

        return -0.03F * (effectInstance.getAmplifier() + 1);
    }

    /**
     * 公式 5：月有圆缺（受击） —— +0.25；若攻击者为 5 格内怪物，再按 20% 概率额外 +0.5。
     *（虎王之蔑延迟结算在排队前已计入本加成，这里跳过延迟伤害避免二次放大。）
     */
    private static float moonPhasesBonusPercent(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return 0.0F;
        }
        if (isDelayedDamage(event.getSource())) {
            return 0.0F;
        }
        boolean equipped = CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.MOON_PHASES.get()))
                .orElse(false);
        if (!equipped) {
            return 0.0F;
        }
        float percent = MOON_DAMAGE_TAKEN_MULTIPLIER - 1.0F;
        Entity attacker = resolveAttacker(event.getSource());
        if (attacker instanceof Monster monster
                && monster.distanceTo(player) <= MOON_MONSTER_RADIUS
                && player.getRandom().nextFloat() < MOON_MONSTER_CHANCE) {
            percent += MOON_MONSTER_EXTRA_MULTIPLIER - 1.0F;
        }
        return percent;
    }

    /**
     * 公式 6：易怒 —— 受击方维护/刷新 1 秒窗口；攻击方在窗口内有 25% 概率
     * +(0.05 × min(amplifier + 1, 5))，本次增伤成功时由上古毛皮升级易怒。
     *
     * <p>受击与攻击两件事都可能落在同一个 LivingHurtEvent 中（双方都带易怒），
     * 因此先维护受击方窗口，再按攻击方判定增伤。虎王之蔑的延迟结算不计入。</p>
     */
    private static float irritableBonusPercent(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return 0.0F;
        }
        DamageSource source = event.getSource();
        if (isDelayedDamage(source)) {
            return 0.0F;
        }
        long now = target.level().getGameTime();

        if (target.hasEffect(ModEffects.IRRITABLE.get())) {
            IRRITABLE_WINDOWS.put(target.getUUID(), now + IRRITABLE_WINDOW_TICKS);
        }

        Entity attacker = resolveAttacker(source);
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return 0.0F;
        }
        MobEffectInstance instance = livingAttacker.getEffect(ModEffects.IRRITABLE.get());
        if (instance == null) {
            return 0.0F;
        }
        Long expiry = IRRITABLE_WINDOWS.get(livingAttacker.getUUID());
        if (expiry == null || now > expiry) {
            IRRITABLE_WINDOWS.remove(livingAttacker.getUUID());
            return 0.0F;
        }
        if (livingAttacker.getRandom().nextFloat() < IRRITABLE_CHANCE) {
            int level = Math.min(instance.getAmplifier() + 1, IRRITABLE_MAX_LEVEL);
            // 上古毛皮：本次增伤触发成功，易怒等级 +1
            escalateAncientPelt(livingAttacker, now);
            return IRRITABLE_PER_LEVEL * level;
        }
        return 0.0F;
    }

    /** 公式 7：天青轮盘 —— 攻击命中时 50% 概率 +1.0、50% 概率 −0.5。 */
    private static float azureWheelBonusPercent(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return 0.0F;
        }
        DamageSource source = event.getSource();
        if (isDelayedDamage(source)) {
            return 0.0F;
        }
        Entity attacker = resolveAttacker(source);
        if (!(attacker instanceof Player player) || player.level().isClientSide) {
            return 0.0F;
        }
        boolean equipped = CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.AZURE_WHEEL.get()))
                .orElse(false);
        if (!equipped) {
            return 0.0F;
        }
        return player.getRandom().nextBoolean() ? 1.0F : -0.5F;
    }

    /** 公式 8：双倍快乐 —— 主副手为同种物品（忽略 NBT）时 +1.0。 */
    private static float doubleHappinessBonusPercent(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return 0.0F;
        }
        DamageSource source = event.getSource();
        if (isDelayedDamage(source)) {
            return 0.0F;
        }
        Entity attacker = resolveAttacker(source);
        if (!(attacker instanceof Player player) || player.level().isClientSide) {
            return 0.0F;
        }
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        // isSameItem 只比较物品类型，天然忽略 NBT
        if (mainHand.isEmpty() || offHand.isEmpty() || !ItemStack.isSameItem(mainHand, offHand)) {
            return 0.0F;
        }
        boolean equipped = CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.DOUBLE_HAPPINESS.get()))
                .orElse(false);
        if (!equipped) {
            return 0.0F;
        }
        return 1.0F;
    }

    /** 公式 9：多多益善 —— 乘法区 +0.1 × min(自身 10×10 范围内存活怪物数, 20)。 */
    private static float moreTheMerrierBonusPercent(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return 0.0F;
        }
        DamageSource source = event.getSource();
        if (isDelayedDamage(source)) {
            return 0.0F;
        }
        Entity attacker = resolveAttacker(source);
        if (!(attacker instanceof Player player) || player.level().isClientSide) {
            return 0.0F;
        }
        boolean equipped = CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.THE_MORE_THE_MERRIER.get()))
                .orElse(false);
        if (!equipped) {
            return 0.0F;
        }

        Level level = player.level();
        AABB area = new AABB(
                player.getX() - MERRIER_RADIUS, level.getMinBuildHeight(), player.getZ() - MERRIER_RADIUS,
                player.getX() + MERRIER_RADIUS, level.getMaxBuildHeight(), player.getZ() + MERRIER_RADIUS);
        int count = level.getEntitiesOfClass(Monster.class, area, Entity::isAlive).size();
        if (count <= 0) {
            return 0.0F;
        }
        int capped = Math.min(count, MERRIER_MAX_MONSTERS);
        return MERRIER_PER_MONSTER * capped;
    }

    /**
     * 公式 10：山海恩泽 —— 攻击者本人处于下落中的直接近战命中，乘法区 + 下落格数 × 0.01。
     *（只有直接实体是玩家才生效，箭矢等远程攻击不受影响。）
     */
    private static float shanhaiGraceBonusPercent(LivingHurtEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof Player player)
                || player.level().isClientSide || player.fallDistance <= 0.0F) {
            return 0.0F;
        }
        boolean equipped = CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.SHANHAI_GRACE.get()))
                .orElse(false);
        if (!equipped) {
            return 0.0F;
        }
        return player.fallDistance * 0.01F;
    }

    /** 公式 11：迅击 —— 附魔等级 × 当前移动速度值（玩家默认 0.1 → I 级 +10%）。 */
    private static float swiftStrikeBonusPercent(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide || event.getAmount() <= 0.0F) {
            return 0.0F;
        }
        if (isDelayedDamage(event.getSource())) {
            return 0.0F;
        }
        LivingEntity attacker = resolveLivingAttacker(event.getSource());
        if (attacker == null) {
            return 0.0F;
        }
        int level = getWeaponEnchantLevel(attacker, ModEnchantments.SWIFT_STRIKE.get());
        if (level <= 0) {
            return 0.0F;
        }
        // 当前移动速度值（含药水/属性修饰符加成），玩家默认 0.1
        double speed = attacker.getAttributeValue(Attributes.MOVEMENT_SPEED);
        if (speed <= 0.0D) {
            return 0.0F;
        }
        return (float) (level * speed);
    }

    /**
     * 暗灵毒牙：佩戴者攻击命中任意实体时为其附加 3 秒「暗毒」（不改数值）。
     * 近战与弹射物攻击都通过发射者追溯生效；虎王之蔑的延迟结算不计入。
     */
    private static void applyDarkSpiritFang(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }
        DamageSource source = event.getSource();
        if (isDelayedDamage(source)) {
            return;
        }
        Entity attacker = resolveAttacker(source);
        if (!(attacker instanceof Player player) || player.level().isClientSide) {
            return;
        }
        boolean equipped = CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.DARK_SPIRIT_FANG.get()))
                .orElse(false);
        if (!equipped) {
            return;
        }
        target.addEffect(new MobEffectInstance(ModEffects.DARK_POISON.get(),
                DARK_POISON_DURATION, 0, false, true, true));
    }

    // ------------------------------------------------------------------
    // 阶段 1.5：虎王之蔑（LOWEST，必须在所有倍率之后）
    // ------------------------------------------------------------------

    /**
     * 虎王之蔑：佩戴于圣遗物槽位时，把任意伤害拦下，改为 5 次、每次 20% 的延迟结算。
     *
     * <p>使用 {@link EventPriority#LOWEST}，确保所有护甲前倍率都乘完之后再拆分。
     * 此处的 amount 是<b>护甲前数值</b>；这里取消立即扣血，并按该值拆成 5 笔排队，每笔独立计时。
     * 延迟伤害自身不再拆分；{@code /kill} 等无视无敌的伤害保持立即生效。</p>
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTigerKingDisdainHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        DamageSource source = event.getSource();
        if (isDelayedDamage(source) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        if (event.getAmount() <= 0.0F || player.isDeadOrDying()) {
            return;
        }
        boolean equipped = CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.TIGER_KING_DISDAIN.get()))
                .orElse(false);
        if (!equipped) {
            return;
        }
        event.setCanceled(true);
        DELAYED_DAMAGES.computeIfAbsent(player.getUUID(), key -> new ArrayList<>())
                .add(new DelayedDamage(source, event.getAmount() * DELAY_FRACTION));
    }

    // ------------------------------------------------------------------
    // 阶段 2：LivingDamageEvent（护甲/附魔/抗性/吸收之后，只做回血/吸收/封顶）
    // ------------------------------------------------------------------

    /**
     * 阶段 2 之默认优先级：先泣血回血，再米米圣盾。两者互不影响，保持各自原始条件。
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        applyWeepingBlood(event);
        applyMimiShield(event);
    }

    /**
     * 泣血：读取攻击方主手/副手武器上的附魔等级，按本次最终伤害的
     * 6% + 2% × (等级 − 1) 回复攻击方生命。
     */
    private static void applyWeepingBlood(LivingDamageEvent event) {
        // 只在服务端处理（事件在客户端也可能收到，但治疗只应服务端生效）
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }

        LivingEntity attacker = resolveLivingAttacker(event.getSource());
        if (attacker == null || event.getAmount() <= 0.0F) {
            return;
        }

        int level = getWeaponEnchantLevel(attacker, ModEnchantments.WEEPING_BLOOD.get());
        if (level <= 0) {
            return;
        }

        // 1 级 6%，之后每级 +2%
        float healAmount = event.getAmount() * (0.06F + 0.02F * (level - 1));
        attacker.heal(healAmount);
    }

    /**
     * 米米圣盾：受到任意伤害后获得 10% 最大生命值的额外生命值（原版吸收机制），
     * 总额最多累计到最大生命值的 40%。此时本次伤害已结算完，新增吸收只抵挡后续伤害。
     */
    private static void applyMimiShield(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        boolean equipped = CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.MIMI_SHIELD.get()))
                .orElse(false);
        if (!equipped) {
            return;
        }
        float maxHealth = player.getMaxHealth();
        float cap = maxHealth * MIMI_SHIELD_CAP_RATIO;
        float current = player.getAbsorptionAmount();
        if (current >= cap) {
            return;
        }
        player.setAbsorptionAmount(Math.min(current + maxHealth * MIMI_SHIELD_GAIN_RATIO, cap));
    }

    /**
     * 阶段 2 之逆鳞龙胆（LOWEST）：单次受到的最终伤害不超过最大生命值的 20%。
     * 使用 LOWEST 优先级，确保其他增伤/减伤逻辑全部执行完再封顶。
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamageReverseScaleGall(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        boolean equipped = CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.REVERSE_SCALE_GALL.get()))
                .orElse(false);
        if (!equipped) {
            return;
        }
        float cap = player.getMaxHealth() * 0.2F;
        if (event.getAmount() > cap) {
            event.setAmount(cap);
        }
    }

    // ------------------------------------------------------------------
    // 其它伤害相关事件
    // ------------------------------------------------------------------

    /**
     * 月有圆缺：非暴击的近战攻击有 30% 概率被强制转为暴击（倍率取原版 1.5 倍）。
     *
     * <p>攻击力 +10 已由物品的属性修饰符提供，这里只处理暴击率。
     * 原版判定为暴击的攻击直接跳过；若安装了 AttributesLib，
     * 暴击率由 {@code attributeslib:crit_chance} 属性负责，这里不再重复掷骰。</p>
     */
    @SubscribeEvent
    public static void onMoonPhasesCriticalHit(CriticalHitEvent event) {
        if (event.isVanillaCritical()) {
            return;
        }
        if (ForgeRegistries.ATTRIBUTES.getValue(MOON_CRIT_CHANCE_ID) != null) {
            return;
        }
        Player player = event.getEntity();
        boolean equipped = CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.MOON_PHASES.get()))
                .orElse(false);
        if (!equipped) {
            return;
        }
        if (player.getRandom().nextFloat() < MOON_CRIT_CHANCE) {
            event.setResult(Event.Result.ALLOW);
            event.setDamageModifier(1.5F);
        }
    }

    /**
     * 夔牛之力：攻击实体前先把目标的无敌帧清零，
     * 使连续攻击不被原版 20 tick 无敌帧吞掉（出手间隔仍受攻击速度冷却限制）。
     */
    @SubscribeEvent
    public static void onKuiNiuPowerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        boolean equipped = CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.KUI_NIU_POWER.get()))
                .orElse(false);
        if (!equipped) {
            return;
        }
        target.invulnerableTime = 0;
    }

    // ------------------------------------------------------------------
    // 虎王之蔑延迟结算引擎
    // ------------------------------------------------------------------

    /** 每 tick 推进各笔延迟伤害的计时，到点结算一次 20%。 */
    @SubscribeEvent
    public static void onDelayedDamageTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        Player player = event.player;
        List<DelayedDamage> pending = DELAYED_DAMAGES.get(player.getUUID());
        if (pending == null || pending.isEmpty()) {
            return;
        }
        Iterator<DelayedDamage> iterator = pending.iterator();
        while (iterator.hasNext()) {
            DelayedDamage delayed = iterator.next();
            if (--delayed.ticksUntilNext > 0) {
                continue;
            }
            delayed.ticksUntilNext = DELAY_INTERVAL_TICKS;
            // 多笔伤害可能同一 tick 到点，且延迟伤害不应额外提供无敌帧
            player.invulnerableTime = 0;
            if (player.hurt(delayedSource(player, delayed.source), delayed.amount)) {
                hurtArmorDurability(player, delayed.source, delayed.amount);
            }
            player.invulnerableTime = 0;
            if (--delayed.remaining <= 0) {
                iterator.remove();
            }
        }
        if (pending.isEmpty()) {
            DELAYED_DAMAGES.remove(player.getUUID());
        }
    }

    /**
     * 虎王之蔑的延迟伤害类型带 bypasses_armor，原版不会做盔甲减伤，也就不会调用 hurtArmor 扣耐久；
     * 这里按原版公式补扣：每件护甲损失 max(1, 本次伤害 / 4) 点耐久。
     */
    private static void hurtArmorDurability(Player player, DamageSource source, float amount) {
        if (amount <= 0.0F) {
            return;
        }
        int wear = (int) Math.max(1.0F, amount / 4.0F);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof ArmorItem)) {
                continue;
            }
            if (source.is(DamageTypeTags.IS_FIRE) && stack.getItem().isFireResistant()) {
                continue;
            }
            stack.hurtAndBreak(wear, player, entity -> entity.broadcastBreakEvent(slot));
        }
    }

    /** 玩家死亡时丢弃尚未结算的延迟伤害，同时清理易怒窗口与上古毛皮状态。 */
    @SubscribeEvent
    public static void onDelayedDamageClear(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            DELAYED_DAMAGES.remove(player.getUUID());
        }
        IRRITABLE_WINDOWS.remove(event.getEntity().getUUID());
        PELT_STATES.remove(event.getEntity().getUUID());
    }

    /** 玩家退出时丢弃全部本类维护的运行时状态，避免残留计时器继续扣血。 */
    @SubscribeEvent
    public static void onDelayedDamageClearOnLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DELAYED_DAMAGES.remove(event.getEntity().getUUID());
        IRRITABLE_WINDOWS.remove(event.getEntity().getUUID());
        PELT_STATES.remove(event.getEntity().getUUID());
    }

    /** 构造延迟结算用的 DamageSource：沿用原攻击方，伤害类型换成本模组的穿透类型。 */
    private static DamageSource delayedSource(Player player, DamageSource original) {
        Registry<DamageType> registry = player.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE);
        Holder<DamageType> holder = registry.getHolderOrThrow(DELAYED_DAMAGE_KEY);
        return new DamageSource(holder, original.getDirectEntity(), original.getEntity());
    }

    /** 判断伤害是否来自虎王之蔑的延迟结算。 */
    private static boolean isDelayedDamage(DamageSource source) {
        return source.typeHolder().unwrapKey()
                .map(key -> key.location().equals(DELAYED_DAMAGE_ID))
                .orElse(false);
    }

    // ------------------------------------------------------------------
    // 易怒 / 上古毛皮的状态维护
    // ------------------------------------------------------------------

    /** 玩家是否佩戴上古毛皮。 */
    private static boolean hasAncientPelt(Player player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.ANCIENT_PELT.get()))
                .orElse(false);
    }

    /** 上古毛皮触发增伤后升级易怒（最高 5 级）并重置 10 秒降级计时。 */
    private static void escalateAncientPelt(LivingEntity attacker, long now) {
        if (!(attacker instanceof Player player) || player.level().isClientSide
                || !hasAncientPelt(player)) {
            return;
        }
        PeltRageState state = PELT_STATES.computeIfAbsent(player.getUUID(),
                key -> new PeltRageState(now));
        if (state.level < PELT_MAX_LEVEL) {
            state.level++;
        }
        state.lastProcTick = now;
    }

    /**
     * 上古毛皮：佩戴期间持续维持「易怒」效果，等级取当前升级状态；
     * 10 秒未触发增伤则降回 I 级。脱下时清除本方灌注的残留效果。
     *
     * <p>等级变化时先移除旧效果再重新施加，确保 HUD 等级立即回落到目标值；
     * 平时仅在剩余时长不足 3 秒时刷新回 8 秒（即每 5 秒一次）。</p>
     */
    @SubscribeEvent
    public static void onAncientPeltTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        Player player = event.player;
        UUID id = player.getUUID();
        if (!hasAncientPelt(player)) {
            if (PELT_STATES.remove(id) != null) {
                MobEffectInstance current = player.getEffect(ModEffects.IRRITABLE.get());
                if (current != null && current.isAmbient()
                        && current.getDuration() <= PELT_EFFECT_DURATION) {
                    player.removeEffect(ModEffects.IRRITABLE.get());
                }
            }
            return;
        }
        long now = player.level().getGameTime();
        PeltRageState state = PELT_STATES.computeIfAbsent(id, key -> new PeltRageState(now));
        if (now - state.lastProcTick > PELT_DECAY_TICKS) {
            state.level = 1;
        }
        int amplifier = state.level - 1;
        MobEffectInstance current = player.getEffect(ModEffects.IRRITABLE.get());
        if (current != null && current.getAmplifier() != amplifier) {
            player.removeEffect(ModEffects.IRRITABLE.get());
            current = null;
        }
        if (current == null || current.getDuration() <= PELT_REFRESH_REMAINING) {
            player.addEffect(new MobEffectInstance(ModEffects.IRRITABLE.get(),
                    PELT_EFFECT_DURATION, amplifier, true, true, true));
        }
    }

    /** 每 5 秒清理一次过期的易怒窗口，避免长期运行后堆积失效的 UUID 条目。 */
    @SubscribeEvent
    public static void onIrritableWindowCleanup(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 100 != 0) {
            return;
        }
        long now = event.getServer().overworld().getGameTime();
        IRRITABLE_WINDOWS.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    // ------------------------------------------------------------------
    // 辅助方法
    // ------------------------------------------------------------------

    /** 追溯伤害的实际来源实体：直接实体若为投掷物则取其发射者。 */
    private static Entity resolveAttacker(DamageSource source) {
        Entity direct = source.getEntity();
        return direct instanceof Projectile projectile ? projectile.getOwner() : direct;
    }

    /** 追溯伤害的实际攻击方（仅返回生物）：直接实体 → 若是弹射物则取其发射者。 */
    private static LivingEntity resolveLivingAttacker(DamageSource source) {
        Entity entity = source.getEntity();
        if (entity instanceof Projectile projectile) {
            entity = projectile.getOwner();
        }
        return entity instanceof LivingEntity living ? living : null;
    }

    /** 读取攻击方手持武器上的指定附魔等级（优先主手，主手没有时再看副手）。 */
    private static int getWeaponEnchantLevel(LivingEntity attacker, Enchantment enchantment) {
        int mainHand = EnchantmentHelper.getItemEnchantmentLevel(enchantment, attacker.getMainHandItem());
        if (mainHand > 0) {
            return mainHand;
        }
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, attacker.getOffhandItem());
    }
}
