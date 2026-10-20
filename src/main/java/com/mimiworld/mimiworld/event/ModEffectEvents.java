package com.mimiworld.mimiworld.event;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.effect.ModEffects;
import com.mimiworld.mimiworld.entity.CompoundAzurePythonEntity;
import com.mimiworld.mimiworld.item.ArmorPieceEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 药水效果相关事件处理器（Forge EVENT_BUS）。
 *
 * <p>目前处理：</p>
 * <ul>
 *   <li>{@code crimson_candlelight}（腥红烛照）：LivingHurtEvent 中检查伤害源的攻击方是否拥有该效果，
 *       若有则将最终伤害乘以 (amplifier + 2)。覆盖近战、远程（弹射物追溯发射者）等攻击类伤害。</li>
 *   <li>{@code python_constriction}（蟒蛇缠绕）：拥有该效果的攻击方对复晶天青蟒造成的伤害 ×1.5。</li>
 *   <li>{@code remnant_corruption}（残堕化）：受击者每级减少 10% 受到的伤害。</li>
 *   <li>{@code creative_flight}（创造飞行）：PlayerTickEvent 中拥有该效果时授予 mayfly，
 *       效果结束后撤销（创造模式或穿戴飞行盔甲时保留）。</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEffectEvents {

    /** 记录玩家当前的 mayfly 是否由「食用创造飞行料理」授予，用于效果结束后精确撤销。 */
    private static final String FOOD_FLIGHT_FLAG = "mimiworld.food_creative_flight";

    private static final EquipmentSlot[] ARMOR_SLOTS =
            {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};


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
     * 最终伤害乘以 {@code 1 - 0.1 × (amplifier + 1)}（每级 -10%），最低为 0。
     */
    @SubscribeEvent
    public static void onRemnantCorruption(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }

        var effectInstance = target.getEffect(ModEffects.REMNANT_CORRUPTION.get());
        if (effectInstance == null) {
            return;
        }

        float reduction = 0.1F * (effectInstance.getAmplifier() + 1);
        float multiplier = Math.max(0.0F, 1.0F - reduction);
        event.setAmount(event.getAmount() * multiplier);
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
