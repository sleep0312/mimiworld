package com.mimiworld.mimiworld.enchantment;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 米米世界自定义附魔注册类。
 *
 * <p>目前注册：</p>
 * <ul>
 *   <li>weeping_blood（泣血）：剑类武器专属，造成伤害后按伤害值百分比回复生命。</li>
 *   <li>flawless（完美无缺）：可附魔任意有耐久的物品，使其不再消耗耐久。</li>
 * </ul>
 */
public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MimiWorld.MODID);

    /** 泣血 —— 剑类武器专属附魔，最高 5 级。 */
    public static final RegistryObject<Enchantment> WEEPING_BLOOD = ENCHANTMENTS.register(
            "weeping_blood",
            WeepingBloodEnchantment::new);

    /** 完美无缺 —— 任意有耐久的物品可用，最高 1 级，物品不再消耗耐久。 */
    public static final RegistryObject<Enchantment> FLAWLESS = ENCHANTMENTS.register(
            "flawless",
            FlawlessEnchantment::new);

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}
