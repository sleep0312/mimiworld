package com.mimiworld.mimiworld.event;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.ModItems;
import com.mimiworld.mimiworld.effect.ModEffects;
import com.mimiworld.mimiworld.entity.CompoundAzurePythonEntity;
import com.mimiworld.mimiworld.entity.ModEntities;
import com.mimiworld.mimiworld.item.ArmorPieceEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 药水效果相关事件处理器（Forge EVENT_BUS）。
 *
 * <p>目前处理：</p>
 * <ul>
 *   <li>{@code crimson_candlelight}（腥红烛照）：LivingHurtEvent 中检查伤害源的攻击方是否拥有该效果，
 *       若有则将最终伤害乘以 (amplifier + 2)。覆盖近战、远程（弹射物追溯发射者）等攻击类伤害。</li>
 *   <li>{@code python_constriction}（蟒蛇缠绕）：拥有该效果的攻击方对复晶天青蟒造成的伤害 ×1.5。</li>
 *   <li>{@code remnant_corruption}（残堕化）：受击者每级减少 3% 受到的伤害；
 *       第一至第八 Boss 生成/载入时永久获得对应等级（I～VIII 级）。</li>
 *   <li>{@code creative_flight}（创造飞行）：PlayerTickEvent 中拥有该效果时授予 mayfly，
 *       效果结束后撤销（创造模式或穿戴飞行盔甲时保留）。</li>
 *   <li>虎王之蔑（tiger_king_disdain）：佩戴于圣遗物槽位时，受到的伤害被取消并拆成
 *       5 次、每次 20%、每 1 秒结算一次；多笔伤害各自独立计时（见 DelayedDamage）。</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEffectEvents {

    /** 记录玩家当前的 mayfly 是否由「食用创造飞行料理」授予，用于效果结束后精确撤销。 */
    private static final String FOOD_FLIGHT_FLAG = "mimiworld.food_creative_flight";

    private static final EquipmentSlot[] ARMOR_SLOTS =
            {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /** 虎王之蔑延迟伤害的自定义伤害类型（data/mimiworld/damage_type/tiger_king_disdain.json）。 */
    private static final ResourceLocation DELAYED_DAMAGE_ID =
            new ResourceLocation(MimiWorld.MODID, "tiger_king_disdain");
    private static final ResourceKey<DamageType> DELAYED_DAMAGE_KEY =
            ResourceKey.create(Registries.DAMAGE_TYPE, DELAYED_DAMAGE_ID);
    private static final int DELAY_INSTALLMENTS = 5;
    private static final int DELAY_INTERVAL_TICKS = 20;
    private static final float DELAY_FRACTION = 0.2F;

    /** 坚韧虎皮：基础护甲加成、按总护甲换算攻击/扣减护甲的比例。 */
    private static final double TOUGH_HIDE_ARMOR_BONUS = 12.0D;
    private static final double TOUGH_HIDE_RATIO = 0.1D;
    private static final UUID TOUGH_HIDE_ARMOR_UUID =
            UUID.fromString("8b0a1d1e-6f2e-4c54-9a44-3d1e6f2b7c01");
    private static final UUID TOUGH_HIDE_PENALTY_UUID =
            UUID.fromString("9c1b2e2f-7a3f-4d65-8b55-4e2f7a3c8d02");
    private static final UUID TOUGH_HIDE_ATTACK_UUID =
            UUID.fromString("ad2c3f30-8b40-4e76-9c66-5f3a8b4d9e03");

    /** 月有圆缺：暴击率、受伤倍率、附近怪物额外伤害的概率与倍率。 */
    private static final float MOON_CRIT_CHANCE = 0.30F;
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

    /** 上古毛皮：易怒最高 5 级、10 秒（200 tick）未触发降级、效果刷新时长 2 秒。 */
    private static final int PELT_MAX_LEVEL = 5;
    private static final long PELT_DECAY_TICKS = 200L;
    private static final int PELT_EFFECT_DURATION = 40;

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

    /** 每名装备坚韧虎皮的玩家缓存「不含本饰品修正的基础护甲」与当前罚值，用于判断是否需要重算。 */
    private static final Map<UUID, ToughHideState> TOUGH_HIDE_STATES = new HashMap<>();

    /** 坚韧虎皮的动态属性快照。 */
    private static final class ToughHideState {
        private double baseArmor;
        private double bonus;
    }

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



    /**
     * 腥红烛照伤害倍率处理。
     *
     * <p>Forge LivingHurtEvent 在原版 {@code LivingEntity#actuallyHurt} 里被触发，
     * 此时 {@code event.getAmount()} 是经过盔甲、抗性等修正后的最终伤害值。
     * 我们在这之上乘以倍率。</p>
     *
     * <p>攻击方追溯链：{@code source.getEntity()} 先拿到直接造成伤害的实体
     * （例如箭矢），如果是 {@link AbstractProjectile} 子类则进一步取 {@code getOwner()}
     * 追溯回发射者。</p>
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 只在服务端处理（LivingHurtEvent 可能在客户端也收到，但修改伤害只服务端有效）
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }

        DamageSource source = event.getSource();
        if (isDelayedDamage(source)) {
            return;
        }

        // 追溯攻击方：直接实体 → 若是投掷物则取其发射者
        Entity directEntity = source.getEntity();
        Entity attacker = directEntity;
        if (directEntity instanceof Projectile projectile) {
            attacker = projectile.getOwner();
        }

        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }

        var effectInstance = livingAttacker.getEffect(ModEffects.CRIMSON_CANDLELIGHT.get());
        if (effectInstance == null) {
            return;
        }

        int amplifier = effectInstance.getAmplifier();
        float multiplier = amplifier + 2.0F; // amplifier=0 → ×2, amplifier=1 → ×3, ...
        float originalDamage = event.getAmount();
        float newDamage = originalDamage * multiplier;
        event.setAmount(newDamage);
    }

    /**
     * 残堕化减伤处理：受击者拥有 {@code remnant_corruption} 时，
     * 最终伤害乘以 {@code 1 - 0.03 × (amplifier + 1)}（每级 -3%，VIII 级 -24%），最低为 0。
     */
    @SubscribeEvent
    public static void onRemnantCorruption(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }
        if (isDelayedDamage(event.getSource())) {
            return;
        }

        var effectInstance = target.getEffect(ModEffects.REMNANT_CORRUPTION.get());
        if (effectInstance == null) {
            return;
        }

        float reduction = 0.03F * (effectInstance.getAmplifier() + 1);
        float multiplier = Math.max(0.0F, 1.0F - reduction);
        event.setAmount(event.getAmount() * multiplier);
    }

    /**
     * Boss 残堕化等级赋予：第一至第八 Boss 生成或随存档载入时，
     * 永久获得对应等级的 {@code remnant_corruption}（I～VIII 级，amplifier 0～7）。
     */
    @SubscribeEvent
    public static void onBossJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof LivingEntity living)) {
            return;
        }

        int tier = bossTier(living.getType());
        if (tier <= 0) {
            return;
        }

        MobEffectInstance instance = new MobEffectInstance(
                ModEffects.REMNANT_CORRUPTION.get(),
                MobEffectInstance.INFINITE_DURATION,
                tier - 1,
                false,  // ambient
                false,  // visible（不显示粒子）
                true);  // showIcon
        instance.setCurativeItems(Collections.emptyList());
        living.addEffect(instance);
    }

    /** 第一至第八 Boss 对应的残堕化等级（按 ModEntities 注册顺序），非 Boss 返回 0。 */
    private static int bossTier(EntityType<?> type) {
        if (type == ModEntities.MIMI_MONKEY_KING.get()) {
            return 1;
        }
        if (type == ModEntities.RICE_CRYSTAL_TIGER_KING.get()) {
            return 2;
        }
        if (type == ModEntities.SUPER_CRYSTAL_COW_KING.get()) {
            return 3;
        }
        if (type == ModEntities.HEAVY_CRYSTAL_RABBIT_KING.get()) {
            return 4;
        }
        if (type == ModEntities.COMPOUND_AZURE_PYTHON.get()) {
            return 5;
        }
        if (type == ModEntities.CLEANSING_LONGEVITY_KUI.get()) {
            return 6;
        }
        if (type == ModEntities.LAZIEST_FINAL_DRAGON.get()) {
            return 7;
        }
        if (type == ModEntities.DIBAZONGZUILONG.get()) {
            return 8;
        }
        return 0;
    }

    /**
     * 蟒蛇缠绕易伤处理：攻击方带有 {@code python_constriction} 且受击者是复晶天青蟒时，
     * 最终伤害 ×1.5。与腥红烛照倍率独立（极端情况下两者相乘）。
     */
    @SubscribeEvent
    public static void onPythonConstriction(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide || !(target instanceof CompoundAzurePythonEntity)) {
            return;
        }

        DamageSource source = event.getSource();
        if (isDelayedDamage(source)) {
            return;
        }
        Entity directEntity = source.getEntity();
        Entity attacker = directEntity;
        if (directEntity instanceof Projectile projectile) {
            attacker = projectile.getOwner();
        }

        if (attacker instanceof LivingEntity livingAttacker
                && livingAttacker.hasEffect(ModEffects.PYTHON_CONSTRICTION.get())) {
            event.setAmount(event.getAmount() * 1.5F);
        }
    }

    /**
     * 火眼金睛（fiery_golden_eyes）免疫失明/黑暗：佩戴于圣遗物槽位时，
     * 拦截所有新的失明、黑暗效果施加；佩戴瞬间已有的效果由物品 onEquip 清除。
     */
    @SubscribeEvent
    public static void onBlindnessOrDarkness(MobEffectEvent.Applicable event) {
        MobEffect effect = event.getEffectInstance().getEffect();
        if (effect != MobEffects.BLINDNESS && effect != MobEffects.DARKNESS) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        boolean equipped = CuriosApi.getCuriosInventory(entity).resolve()
                .map(handler -> handler.isEquipped(ModItems.FIERY_GOLDEN_EYES.get()))
                .orElse(false);
        if (equipped) {
            event.setResult(Event.Result.DENY);
        }
    }

    /**
     * 虎王之蔑：佩戴于圣遗物槽位时，把任意伤害拦下，改为 5 次、每次 20% 的延迟结算。
     *
     * <p>{@code LivingHurtEvent} 的 amount 已经过盔甲、附魔、抗性等修正，是本次真正会扣除的血量；
     * 这里取消立即扣血，并按该值拆成 5 笔排队，每笔独立计时。</p>
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTigerKingDisdainHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        DamageSource source = event.getSource();
        // 延迟结算自身不再拆分；/kill 等无视无敌的伤害保持立即生效
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
            player.hurt(delayedSource(player, delayed.source), delayed.amount);
            player.invulnerableTime = 0;
            if (--delayed.remaining <= 0) {
                iterator.remove();
            }
        }
        if (pending.isEmpty()) {
            DELAYED_DAMAGES.remove(player.getUUID());
        }
    }

    /** 玩家死亡或退出时丢弃尚未结算的延迟伤害，避免残留计时器继续扣血。 */
    @SubscribeEvent
    public static void onDelayedDamageClear(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            DELAYED_DAMAGES.remove(player.getUUID());
        }
        IRRITABLE_WINDOWS.remove(event.getEntity().getUUID());
        PELT_STATES.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onDelayedDamageClearOnLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DELAYED_DAMAGES.remove(event.getEntity().getUUID());
        IRRITABLE_WINDOWS.remove(event.getEntity().getUUID());
        PELT_STATES.remove(event.getEntity().getUUID());
        // 属性修饰符随玩家实体消失而不复存在，缓存也一并丢弃，避免下次登录首 tick 误算
        TOUGH_HIDE_STATES.remove(event.getEntity().getUUID());
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

    /**
     * 坚韧虎皮：佩戴于圣遗物槽位期间，护甲 +12，并以（基础护甲 + 12）为「总护甲」：
     * 攻击力 +总护甲 × 10%，随后护甲 −总护甲 × 10%。
     *
     * <p>每 tick 读出当前护甲并减去本饰品自身修正，得到不含本饰品的基础护甲；
     * 若与上次缓存不同（换装、增益等改变了护甲）就移除三个修饰符后按新值重算。</p>
     */
    @SubscribeEvent
    public static void onToughTigerHideTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        Player player = event.player;
        UUID id = player.getUUID();
        boolean equipped = CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.TOUGH_TIGER_HIDE.get()))
                .orElse(false);
        if (!equipped) {
            if (TOUGH_HIDE_STATES.remove(id) != null) {
                removeToughHideModifiers(player);
            }
            return;
        }
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (armor == null || attack == null) {
            return;
        }
        ToughHideState state = TOUGH_HIDE_STATES.get(id);
        // 当前护甲含本饰品的 +12 与 -罚值，先减掉得到基础护甲
        double baseArmor = state == null
                ? armor.getValue()
                : armor.getValue() - (TOUGH_HIDE_ARMOR_BONUS - state.bonus);
        double bonus = Math.max(0.0D, (baseArmor + TOUGH_HIDE_ARMOR_BONUS) * TOUGH_HIDE_RATIO);
        if (state != null
                && Math.abs(baseArmor - state.baseArmor) < 1.0E-4D
                && Math.abs(bonus - state.bonus) < 1.0E-4D) {
            return;
        }
        removeToughHideModifiers(player);
        armor.addTransientModifier(new AttributeModifier(TOUGH_HIDE_ARMOR_UUID,
                "mimiworld.tough_tiger_hide.armor", TOUGH_HIDE_ARMOR_BONUS,
                AttributeModifier.Operation.ADDITION));
        if (bonus > 0.0D) {
            armor.addTransientModifier(new AttributeModifier(TOUGH_HIDE_PENALTY_UUID,
                    "mimiworld.tough_tiger_hide.penalty", -bonus,
                    AttributeModifier.Operation.ADDITION));
            attack.addTransientModifier(new AttributeModifier(TOUGH_HIDE_ATTACK_UUID,
                    "mimiworld.tough_tiger_hide.attack", bonus,
                    AttributeModifier.Operation.ADDITION));
        }
        ToughHideState newState = new ToughHideState();
        newState.baseArmor = baseArmor;
        newState.bonus = bonus;
        TOUGH_HIDE_STATES.put(id, newState);
    }

    /** 移除坚韧虎皮施加的全部属性修饰符。 */
    private static void removeToughHideModifiers(Player player) {
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(TOUGH_HIDE_ARMOR_UUID);
            armor.removeModifier(TOUGH_HIDE_PENALTY_UUID);
        }
        AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.removeModifier(TOUGH_HIDE_ATTACK_UUID);
        }
    }

    /**
     * 月有圆缺：非暴击的近战攻击有 30% 概率被强制转为暴击（倍率取原版 1.5 倍）。
     *
     * <p>攻击力 +10 已由物品的属性修饰符提供，这里只处理暴击率。
     * 原版判定为暴击的攻击直接跳过，避免再次掷骰。</p>
     */
    @SubscribeEvent
    public static void onMoonPhasesCriticalHit(CriticalHitEvent event) {
        if (event.isVanillaCritical()) {
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
     * 月有圆缺：玩家受到的总伤害 ×1.25；若伤害来自 5 格内的怪物，
     * 再按 20% 概率额外 ×1.5（与受伤加成相乘）。
     *
     * <p>虎王之蔑的延迟结算金额在排队前已乘过本饰品加成，这里跳过延迟伤害避免二次放大。</p>
     */
    @SubscribeEvent
    public static void onMoonPhasesHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        if (isDelayedDamage(event.getSource())) {
            return;
        }
        boolean equipped = CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.MOON_PHASES.get()))
                .orElse(false);
        if (!equipped) {
            return;
        }
        float amount = event.getAmount() * MOON_DAMAGE_TAKEN_MULTIPLIER;
        Entity attacker = resolveAttacker(event.getSource());
        if (attacker instanceof Monster monster
                && monster.distanceTo(player) <= MOON_MONSTER_RADIUS
                && player.getRandom().nextFloat() < MOON_MONSTER_CHANCE) {
            amount *= MOON_MONSTER_EXTRA_MULTIPLIER;
        }
        event.setAmount(amount);
    }

    /** 追溯伤害的实际来源实体：直接实体若为投掷物则取其发射者。 */
    private static Entity resolveAttacker(DamageSource source) {
        Entity direct = source.getEntity();
        return direct instanceof Projectile projectile ? projectile.getOwner() : direct;
    }

    /**
     * 易怒：受到任意伤害时开启/刷新 1 秒窗口；窗口内的攻击有 25% 概率增伤，
     * 增伤幅度为 5% × 等级（I 级 +5%、II 级 +10%……上限 5 级 +25%）。
     *
     * <p>受击与攻击两件事都可能在同一个 LivingHurtEvent 中发生（双方都带易怒），
     * 因此先处理受击方窗口，再按攻击方判定增伤。虎王之蔑的延迟结算不计入。</p>
     */
    @SubscribeEvent
    public static void onIrritable(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }
        DamageSource source = event.getSource();
        if (isDelayedDamage(source)) {
            return;
        }
        long now = target.level().getGameTime();

        if (target.hasEffect(ModEffects.IRRITABLE.get())) {
            IRRITABLE_WINDOWS.put(target.getUUID(), now + IRRITABLE_WINDOW_TICKS);
        }

        Entity attacker = resolveAttacker(source);
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }
        MobEffectInstance instance = livingAttacker.getEffect(ModEffects.IRRITABLE.get());
        if (instance == null) {
            return;
        }
        Long expiry = IRRITABLE_WINDOWS.get(livingAttacker.getUUID());
        if (expiry == null || now > expiry) {
            IRRITABLE_WINDOWS.remove(livingAttacker.getUUID());
            return;
        }
        if (livingAttacker.getRandom().nextFloat() < IRRITABLE_CHANCE) {
            int level = Math.min(instance.getAmplifier() + 1, IRRITABLE_MAX_LEVEL);
            event.setAmount(event.getAmount() * (1.0F + IRRITABLE_PER_LEVEL * level));
            // 上古毛皮：本次增伤触发成功，易怒等级 +1
            escalateAncientPelt(livingAttacker, now);
        }
    }

    /**
     * 暗毒：携带者的一切生命恢复只保留 25%（即恢复量 -75%）。
     *
     * <p>原版自然回血、生命恢复效果、治疗药水等都会调用 {@code LivingEntity#heal}，
     * Forge 在该方法中触发 LivingHealEvent，因此一处即可覆盖全部回血途径。
     * 伤害吸收（伤害吸收血量）不属于生命恢复，不受影响。</p>
     */
    @SubscribeEvent
    public static void onDarkPoisonHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        if (entity.hasEffect(ModEffects.DARK_POISON.get())) {
            event.setAmount(event.getAmount() * 0.25F);
        }
    }

    /**
     * 暗灵毒牙：佩戴者（玩家）攻击命中任意实体时，为其附加 3 秒「暗毒」。
     * 近战与弹射物攻击都通过发射者追溯生效；虎王之蔑的延迟结算不计入。
     */
    @SubscribeEvent
    public static void onDarkSpiritFangHurt(LivingHurtEvent event) {
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

    /**
     * 天青轮盘：佩戴者（玩家）攻击命中时，50% 概率本次伤害翻倍、50% 概率减半。
     * 近战与弹射物攻击均生效；虎王之蔑的延迟结算不重复判定。
     */
    @SubscribeEvent
    public static void onAzureWheelHurt(LivingHurtEvent event) {
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
                .map(handler -> handler.isEquipped(ModItems.AZURE_WHEEL.get()))
                .orElse(false);
        if (!equipped) {
            return;
        }
        float multiplier = player.getRandom().nextBoolean() ? 2.0F : 0.5F;
        event.setAmount(event.getAmount() * multiplier);
    }

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
     * 平时仅在剩余时长不足 1 秒时刷新，避免逐 tick 重建效果。</p>
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
        if (current == null || current.getDuration() < 20) {
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

    /**
     * 创造飞行（creative_flight）处理：拥有该效果时授予 mayfly，效果结束后撤销。
     *
     * <p>用 {@code persistentData} 记录「mayfly 是否由本效果授予」，避免误撤创造模式
     * 或飞行盔甲（{@link ArmorPieceEffects#grantsFlight}）提供的飞行能力。</p>
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }
        // 旁观者模式的飞行由原版接管，不能修改其 mayfly/flying，否则会持续下坠
        if (player.isSpectator()) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        if (player.hasEffect(ModEffects.CREATIVE_FLIGHT.get())) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            data.putBoolean(FOOD_FLIGHT_FLAG, true);
        } else if (data.getBoolean(FOOD_FLIGHT_FLAG)) {
            if (!player.getAbilities().instabuild && !wearsFlightArmor(player)) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
            data.putBoolean(FOOD_FLIGHT_FLAG, false);
        }
    }

    /** 玩家是否穿着任一提供创造飞行的盔甲部件。 */
    private static boolean wearsFlightArmor(Player player) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (player.getItemBySlot(slot).getItem() instanceof ArmorPieceEffects piece
                    && piece.grantsFlight(slot)) {
                return true;
            }
        }
        return false;
    }
}
