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

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
