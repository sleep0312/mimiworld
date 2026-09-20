package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 米米世界菜单类型（容器 GUI）注册。
 */
public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MimiWorld.MODID);

    // 造米台菜单
    public static final RegistryObject<MenuType<RiceMakerMenu>> RICE_MAKER_MENU =
            MENUS.register("rice_maker",
                    () -> IForgeMenuType.create(RiceMakerMenu::new));

    // 锤米台菜单
    public static final RegistryObject<MenuType<RicePoundingTableMenu>> RICE_POUNDING_TABLE_MENU =
            MENUS.register("rice_pounding_table",
                    () -> IForgeMenuType.create(RicePoundingTableMenu::new));

    // 压饭机菜单
    public static final RegistryObject<MenuType<RicePressMenu>> RICE_PRESS_MENU =
            MENUS.register("rice_press",
                    () -> IForgeMenuType.create(RicePressMenu::new));

    // 高级锤米台菜单
    public static final RegistryObject<MenuType<AdvancedRicePoundingTableMenu>> ADVANCED_RICE_POUNDING_TABLE_MENU =
            MENUS.register("advanced_rice_pounding_table",
                    () -> IForgeMenuType.create(AdvancedRicePoundingTableMenu::new));

    // 电离台菜单
    public static final RegistryObject<MenuType<IonizationTableMenu>> IONIZATION_TABLE_MENU =
            MENUS.register("ionization_table",
                    () -> IForgeMenuType.create(IonizationTableMenu::new));

    // 沉淀台菜单
    public static final RegistryObject<MenuType<PrecipitationTableMenu>> PRECIPITATION_TABLE_MENU =
            MENUS.register("precipitation_table",
                    () -> IForgeMenuType.create(PrecipitationTableMenu::new));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
