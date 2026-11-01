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

    // 离解台菜单
    public static final RegistryObject<MenuType<DissociationTableMenu>> DISSOCIATION_TABLE_MENU =
            MENUS.register("dissociation_table",
                    () -> IForgeMenuType.create(DissociationTableMenu::new));

    // 铸剑台菜单
    public static final RegistryObject<MenuType<SwordForgingTableMenu>> SWORD_FORGING_TABLE_MENU =
            MENUS.register("sword_forging_table",
                    () -> IForgeMenuType.create(SwordForgingTableMenu::new));

    // 铸甲台菜单
    public static final RegistryObject<MenuType<ArmorForgingTableMenu>> ARMOR_FORGING_TABLE_MENU =
            MENUS.register("armor_forging_table",
                    () -> IForgeMenuType.create(ArmorForgingTableMenu::new));

    // 铸弓台菜单
    public static final RegistryObject<MenuType<BowForgingTableMenu>> BOW_FORGING_TABLE_MENU =
            MENUS.register("bow_forging_table",
                    () -> IForgeMenuType.create(BowForgingTableMenu::new));

    // 淬剑台菜单
    public static final RegistryObject<MenuType<SwordQuenchingTableMenu>> SWORD_QUENCHING_TABLE_MENU =
            MENUS.register("sword_quenching_table",
                    () -> IForgeMenuType.create(SwordQuenchingTableMenu::new));

    // 汲血台菜单
    public static final RegistryObject<MenuType<BloodlettingTableMenu>> BLOODLETTING_TABLE_MENU =
            MENUS.register("bloodletting_table",
                    () -> IForgeMenuType.create(BloodlettingTableMenu::new));

    // 碾磨台菜单
    public static final RegistryObject<MenuType<GrindingTableMenu>> GRINDING_TABLE_MENU =
            MENUS.register("grinding_table",
                    () -> IForgeMenuType.create(GrindingTableMenu::new));

    // 搅碎器菜单
    public static final RegistryObject<MenuType<CrusherMenu>> CRUSHER_MENU =
            MENUS.register("crusher",
                    () -> IForgeMenuType.create(CrusherMenu::new));

    // 杵界台菜单
    public static final RegistryObject<MenuType<PestleRealmTableMenu>> PESTLE_REALM_TABLE_MENU =
            MENUS.register("pestle_realm_table",
                    () -> IForgeMenuType.create(PestleRealmTableMenu::new));

    // 凝锭台菜单
    public static final RegistryObject<MenuType<IngotCondensationTableMenu>> INGOT_CONDENSATION_TABLE_MENU =
            MENUS.register("ingot_condensation_table",
                    () -> IForgeMenuType.create(IngotCondensationTableMenu::new));

    // 搅拌器菜单
    public static final RegistryObject<MenuType<MixerMenu>> MIXER_MENU =
            MENUS.register("mixer",
                    () -> IForgeMenuType.create(MixerMenu::new));

    // 小煮锅菜单
    public static final RegistryObject<MenuType<SmallCookingPotMenu>> SMALL_COOKING_POT_MENU =
            MENUS.register("small_cooking_pot",
                    () -> IForgeMenuType.create(SmallCookingPotMenu::new));

    // 煮锅 / 大煮锅 / 超大煮锅 / 巨无霸大大大煮锅菜单
    public static final RegistryObject<MenuType<CookingPotMenu>> COOKING_POT_MENU =
            MENUS.register("cooking_pot",
                    () -> IForgeMenuType.create(CookingPotMenu::new));

    public static final RegistryObject<MenuType<LargeCookingPotMenu>> LARGE_COOKING_POT_MENU =
            MENUS.register("large_cooking_pot",
                    () -> IForgeMenuType.create(LargeCookingPotMenu::new));

    public static final RegistryObject<MenuType<ExtraLargeCookingPotMenu>> EXTRA_LARGE_COOKING_POT_MENU =
            MENUS.register("extra_large_cooking_pot",
                    () -> IForgeMenuType.create(ExtraLargeCookingPotMenu::new));

    public static final RegistryObject<MenuType<JumboMegaCookingPotMenu>> JUMBO_MEGA_COOKING_POT_MENU =
            MENUS.register("jumbo_mega_cooking_pot",
                    () -> IForgeMenuType.create(JumboMegaCookingPotMenu::new));

    // 铁锅菜单（菱形 4 输入）/ 烤箱菜单（几何同大煮锅）
    public static final RegistryObject<MenuType<IronWokMenu>> IRON_WOK_MENU =
            MENUS.register("iron_wok",
                    () -> IForgeMenuType.create(IronWokMenu::new));

    public static final RegistryObject<MenuType<OvenMenu>> OVEN_MENU =
            MENUS.register("oven",
                    () -> IForgeMenuType.create(OvenMenu::new));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
