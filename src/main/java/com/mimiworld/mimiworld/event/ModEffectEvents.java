package com.mimiworld.mimiworld.event;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.ModItems;
import com.mimiworld.mimiworld.effect.ModEffects;
import com.mimiworld.mimiworld.entity.ModEntities;
import com.mimiworld.mimiworld.item.ArmorPieceEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 药水效果与饰品状态相关事件处理器（Forge EVENT_BUS）。
 *
 * <p>伤害数值/伤害结算相关的监听器已全部迁到 {@link DamagePipeline}，本类只保留非伤害逻辑：</p>
 * <ul>
 *   <li>{@code creative_flight}（创造飞行）：PlayerTickEvent 中拥有该效果时授予 mayfly，
 *       效果结束后撤销（创造模式或穿戴飞行盔甲时保留）。</li>
 *   <li>火眼金睛：拦截新的失明/黑暗效果施加。</li>
 *   <li>残堕化效果赋予：第一至第八 Boss 生成/载入时永久获得对应等级。</li>
 *   <li>坚韧虎皮：按总护甲动态维护护甲/攻击力属性修饰符。</li>
 *   <li>暗毒：LivingHealEvent 中把一切生命恢复削减到 25%。</li>
 *   <li>山海恩泽：佩戴期间持续刷新「跳跃提升 V」。</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEffectEvents {

    /** 记录玩家当前的 mayfly 是否由「食用创造飞行料理」授予，用于效果结束后精确撤销。 */
    private static final String FOOD_FLIGHT_FLAG = "mimiworld.food_creative_flight";

    private static final EquipmentSlot[] ARMOR_SLOTS =
            {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /** 坚韧虎皮：基础护甲加成、按总护甲换算攻击/扣减护甲的比例。 */
    private static final double TOUGH_HIDE_ARMOR_BONUS = 12.0D;
    private static final double TOUGH_HIDE_RATIO = 0.1D;
    private static final UUID TOUGH_HIDE_ARMOR_UUID =
            UUID.fromString("8b0a1d1e-6f2e-4c54-9a44-3d1e6f2b7c01");
    private static final UUID TOUGH_HIDE_PENALTY_UUID =
            UUID.fromString("9c1b2e2f-7a3f-4d65-8b55-4e2f7a3c8d02");
    private static final UUID TOUGH_HIDE_ATTACK_UUID =
            UUID.fromString("ad2c3f30-8b40-4e76-9c66-5f3a8b4d9e03");

    /** 每名装备坚韧虎皮的玩家缓存「不含本饰品修正的基础护甲」与当前罚值，用于判断是否需要重算。 */
    private static final Map<UUID, ToughHideState> TOUGH_HIDE_STATES = new HashMap<>();

    /** 坚韧虎皮的动态属性快照。 */
    private static final class ToughHideState {
        private double baseArmor;
        private double bonus;
    }

    /** 山海恩泽：跳跃提升 V 对应 amplifier 4；刷新时长 8 秒，剩余 ≤3 秒时重置（即每 5 秒一次）。 */
    private static final int SHANHAI_JUMP_AMPLIFIER = 4;
    private static final int SHANHAI_EFFECT_DURATION = 160;
    private static final int SHANHAI_REFRESH_REMAINING = 60;

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

    /** 玩家退出时丢弃坚韧虎皮缓存，避免下次登录首 tick 误算（属性修饰符随玩家实体消失）。 */
    @SubscribeEvent
    public static void onToughHideLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        TOUGH_HIDE_STATES.remove(event.getEntity().getUUID());
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
     * 山海恩泽：佩戴期间持续维持「跳跃提升 V」，每 5 秒重置回 8 秒。
     * 与上古毛皮一致，用 PlayerTickEvent + Curios 查询判定，不依赖物品自身的 curioTick；
     * 效果以非 ambient 方式施加，避免被盔甲效果的脱下清理逻辑误删。
     */
    @SubscribeEvent
    public static void onShanhaiGraceTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        Player player = event.player;
        boolean equipped = CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.SHANHAI_GRACE.get()))
                .orElse(false);
        if (!equipped) {
            // 脱下后清掉本饰品灌注的跳跃提升，不误删其它来源的效果
            MobEffectInstance jump = player.getEffect(MobEffects.JUMP);
            if (jump != null && jump.getAmplifier() == SHANHAI_JUMP_AMPLIFIER
                    && jump.getDuration() <= SHANHAI_EFFECT_DURATION) {
                player.removeEffect(MobEffects.JUMP);
            }
            return;
        }
        MobEffectInstance jump = player.getEffect(MobEffects.JUMP);
        if (jump == null || jump.getAmplifier() < SHANHAI_JUMP_AMPLIFIER
                || (jump.getAmplifier() == SHANHAI_JUMP_AMPLIFIER
                    && jump.getDuration() <= SHANHAI_REFRESH_REMAINING)) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, SHANHAI_EFFECT_DURATION,
                    SHANHAI_JUMP_AMPLIFIER, false, false, true));
        }
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
