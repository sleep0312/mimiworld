package com.mimiworld.mimiworld.effect;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 米米世界自定义药水效果注册类。
 *
 * <p>注册两个效果：</p>
 * <ul>
 *   <li>crimson_candlelight（腥红烛照）：正面效果，拥有该效果的实体造成的所有攻击伤害倍率 = amplifier + 2
 *       （I=2倍、II=3倍、III=4倍……）。</li>
 *   <li>python_constriction（蟒蛇缠绕）：负面效果，每秒造成 10×等级 点魔法伤害
 *       （I 级 10/秒、II 级 20/秒……，无视护甲、可致死）；同时拥有该效果的攻击方
 *       对复晶天青蟒造成的伤害 ×1.5（由 ModEffectEvents 在 LivingHurtEvent 中处理）。</li>
 * </ul>
 */
public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MimiWorld.MODID);

    /**
     * 腥红烛照 —— 正面增益。颜色 #E63946 暗红。
     * 伤害倍率由 {@link com.mimiworld.mimiworld.event.ModEffectEvents} 在 LivingHurtEvent 中处理。
     */
    public static final RegistryObject<MobEffect> CRIMSON_CANDLELIGHT = MOB_EFFECTS.register(
            "crimson_candlelight",
            () -> new CrimsonCandlelightEffect(MobEffectCategory.BENEFICIAL, 0xFFE63946));

    /**
     * 蟒蛇缠绕 —— 负面效果。颜色 #2D6A4F 深绿。
     * 自身每秒造成 10×(amplifier+1) 点魔法伤害（I 级 10/秒、II 级 20/秒……）；
     * 对复晶天青蟒 ×1.5 易伤由 {@link com.mimiworld.mimiworld.event.ModEffectEvents} 处理。
     */
    public static final RegistryObject<MobEffect> PYTHON_CONSTRICTION = MOB_EFFECTS.register(
            "python_constriction",
            () -> new PythonConstrictionEffect(MobEffectCategory.HARMFUL, 0xFF2D6A4F));

    /**
     * 创造飞行 —— 正面效果。食用特定料理获得，效果本身不做任何事，
     * 玩家的 mayfly 能力由 {@link com.mimiworld.mimiworld.event.ModEffectEvents}
     * 在 PlayerTickEvent 中逐 tick 授予，效果结束后撤销。颜色 #79E0F5 天蓝。
     */
    public static final RegistryObject<MobEffect> CREATIVE_FLIGHT = MOB_EFFECTS.register(
            "creative_flight",
            () -> new CreativeFlightEffect(MobEffectCategory.BENEFICIAL, 0xFF79E0F5));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
