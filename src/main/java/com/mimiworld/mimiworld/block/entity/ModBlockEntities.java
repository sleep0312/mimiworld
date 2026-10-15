package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 米米世界方块实体类型注册。
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MimiWorld.MODID);

    // 造米台（方块 rice_maker）：3 槽加工台，GUI 见 menu.RiceMakerMenu
    public static final RegistryObject<BlockEntityType<RiceMakerBlockEntity>> RICE_MAKER =
            BLOCK_ENTITIES.register("rice_maker",
                    () -> BlockEntityType.Builder.of(RiceMakerBlockEntity::new, ModBlocks.RICE_MAKER.get()).build(null));

    // 锤米台（方块 rice_pounding_table）：2 输入 + 1 输出，工作台式即时合成，GUI 见 menu.RicePoundingTableMenu
    public static final RegistryObject<BlockEntityType<RicePoundingTableBlockEntity>> RICE_POUNDING_TABLE =
            BLOCK_ENTITIES.register("rice_pounding_table",
                    () -> BlockEntityType.Builder.of(RicePoundingTableBlockEntity::new,
                            ModBlocks.RICE_POUNDING_TABLE.get()).build(null));

    // 压饭机（方块 rice_press）：9 输入 + 1 输出，工作台式无序即时合成，GUI 见 menu.RicePressMenu
    public static final RegistryObject<BlockEntityType<RicePressBlockEntity>> RICE_PRESS =
            BLOCK_ENTITIES.register("rice_press",
                    () -> BlockEntityType.Builder.of(RicePressBlockEntity::new,
                            ModBlocks.RICE_PRESS.get()).build(null));

    // 高级锤米台（方块 advanced_rice_pounding_table）：功能同锤米台（2 输入 + 1 输出即时合成），仅 GUI 外观不同
    public static final RegistryObject<BlockEntityType<AdvancedRicePoundingTableBlockEntity>> ADVANCED_RICE_POUNDING_TABLE =
            BLOCK_ENTITIES.register("advanced_rice_pounding_table",
                    () -> BlockEntityType.Builder.of(AdvancedRicePoundingTableBlockEntity::new,
                            ModBlocks.ADVANCED_RICE_POUNDING_TABLE.get()).build(null));

    // 电离台（方块 ionization_table）：双输入定时加工（无燃料），GUI 见 menu.IonizationTableMenu
    public static final RegistryObject<BlockEntityType<IonizationTableBlockEntity>> IONIZATION_TABLE =
            BLOCK_ENTITIES.register("ionization_table",
                    () -> BlockEntityType.Builder.of(IonizationTableBlockEntity::new,
                            ModBlocks.IONIZATION_TABLE.get()).build(null));

    // 沉淀台（方块 precipitation_table）：双输入定时加工（无燃料），GUI 见 menu.PrecipitationTableMenu
    public static final RegistryObject<BlockEntityType<PrecipitationTableBlockEntity>> PRECIPITATION_TABLE =
            BLOCK_ENTITIES.register("precipitation_table",
                    () -> BlockEntityType.Builder.of(PrecipitationTableBlockEntity::new,
                            ModBlocks.PRECIPITATION_TABLE.get()).build(null));

    // 离解台（方块 dissociation_table）：熔炉式有序双输入定时加工（无燃料），GUI 见 menu.DissociationTableMenu
    public static final RegistryObject<BlockEntityType<DissociationTableBlockEntity>> DISSOCIATION_TABLE =
            BLOCK_ENTITIES.register("dissociation_table",
                    () -> BlockEntityType.Builder.of(DissociationTableBlockEntity::new,
                            ModBlocks.DISSOCIATION_TABLE.get()).build(null));

    // 铸剑台（方块 sword_forging_table）：3x3 工作台式即时合成，独立配方表，GUI 见 menu.SwordForgingTableMenu
    public static final RegistryObject<BlockEntityType<SwordForgingTableBlockEntity>> SWORD_FORGING_TABLE =
            BLOCK_ENTITIES.register("sword_forging_table",
                    () -> BlockEntityType.Builder.of(SwordForgingTableBlockEntity::new,
                            ModBlocks.SWORD_FORGING_TABLE.get()).build(null));

    // 铸甲台（方块 armor_forging_table）：3x3 工作台式即时合成，独立配方表，GUI 见 menu.ArmorForgingTableMenu
    public static final RegistryObject<BlockEntityType<ArmorForgingTableBlockEntity>> ARMOR_FORGING_TABLE =
            BLOCK_ENTITIES.register("armor_forging_table",
                    () -> BlockEntityType.Builder.of(ArmorForgingTableBlockEntity::new,
                            ModBlocks.ARMOR_FORGING_TABLE.get()).build(null));

    // 铸弓台（方块 bow_forging_table）：3x3 工作台式即时合成，独立配方表，GUI 见 menu.BowForgingTableMenu
    public static final RegistryObject<BlockEntityType<BowForgingTableBlockEntity>> BOW_FORGING_TABLE =
            BLOCK_ENTITIES.register("bow_forging_table",
                    () -> BlockEntityType.Builder.of(BowForgingTableBlockEntity::new,
                            ModBlocks.BOW_FORGING_TABLE.get()).build(null));

    // 淬剑台（方块 sword_quenching_table）：3 入 1 出定时加工，开始按钮启动，GUI 见 menu.SwordQuenchingTableMenu
    public static final RegistryObject<BlockEntityType<SwordQuenchingTableBlockEntity>> SWORD_QUENCHING_TABLE =
            BLOCK_ENTITIES.register("sword_quenching_table",
                    () -> BlockEntityType.Builder.of(SwordQuenchingTableBlockEntity::new,
                            ModBlocks.SWORD_QUENCHING_TABLE.get()).build(null));

    // 汲血台（方块 bloodletting_table）：2 入 1 出定时加工，GUI 见 menu.BloodlettingTableMenu
    public static final RegistryObject<BlockEntityType<BloodlettingTableBlockEntity>> BLOODLETTING_TABLE =
            BLOCK_ENTITIES.register("bloodletting_table",
                    () -> BlockEntityType.Builder.of(BloodlettingTableBlockEntity::new,
                            ModBlocks.BLOODLETTING_TABLE.get()).build(null));

    // 碾磨台（方块 grinding_table）：6 入 1 出定时加工（加高面板），GUI 见 menu.GrindingTableMenu
    public static final RegistryObject<BlockEntityType<GrindingTableBlockEntity>> GRINDING_TABLE =
            BLOCK_ENTITIES.register("grinding_table",
                    () -> BlockEntityType.Builder.of(GrindingTableBlockEntity::new,
                            ModBlocks.GRINDING_TABLE.get()).build(null));

    // 搅碎器（方块 crusher）：3 入 1 出定时加工，GUI 见 menu.CrusherMenu
    public static final RegistryObject<BlockEntityType<CrusherBlockEntity>> CRUSHER =
            BLOCK_ENTITIES.register("crusher",
                    () -> BlockEntityType.Builder.of(CrusherBlockEntity::new,
                            ModBlocks.CRUSHER.get()).build(null));

    // 杵界台（方块 pestle_realm_table）：2x2 网格（4 入）+右下输出槽工作台式即时合成，GUI 见 menu.PestleRealmTableMenu
    public static final RegistryObject<BlockEntityType<PestleRealmTableBlockEntity>> PESTLE_REALM_TABLE =
            BLOCK_ENTITIES.register("pestle_realm_table",
                    () -> BlockEntityType.Builder.of(PestleRealmTableBlockEntity::new,
                            ModBlocks.PESTLE_REALM_TABLE.get()).build(null));

    // 凝锭台（方块 ingot_condensation_table）：5x5 网格（25 入）+下方中央输出槽工作台式即时合成（面板加高），GUI 见 menu.IngotCondensationTableMenu
    public static final RegistryObject<BlockEntityType<IngotCondensationTableBlockEntity>> INGOT_CONDENSATION_TABLE =
            BLOCK_ENTITIES.register("ingot_condensation_table",
                    () -> BlockEntityType.Builder.of(IngotCondensationTableBlockEntity::new,
                            ModBlocks.INGOT_CONDENSATION_TABLE.get()).build(null));

    // 搅拌器（方块 mixer）：9 输入 + 1 输出工作台式无序即时合成（加高面板），GUI 见 menu.MixerMenu
    public static final RegistryObject<BlockEntityType<MixerBlockEntity>> MIXER =
            BLOCK_ENTITIES.register("mixer",
                    () -> BlockEntityType.Builder.of(MixerBlockEntity::new,
                            ModBlocks.MIXER.get()).build(null));

    // 小煮锅（方块 small_cooking_pot）：1 输入 + 1 燃料 + 1 输出熔炉式定时加工，GUI 见 menu.SmallCookingPotMenu
    public static final RegistryObject<BlockEntityType<SmallCookingPotBlockEntity>> SMALL_COOKING_POT =
            BLOCK_ENTITIES.register("small_cooking_pot",
                    () -> BlockEntityType.Builder.of(SmallCookingPotBlockEntity::new,
                            ModBlocks.SMALL_COOKING_POT.get()).build(null));

    // 煮锅（方块 cooking_pot）：2x2 网格 + 右上输入 + 燃料 + 输出熔炉式定时加工，GUI 见 menu.CookingPotMenu
    public static final RegistryObject<BlockEntityType<CookingPotBlockEntity>> COOKING_POT =
            BLOCK_ENTITIES.register("cooking_pot",
                    () -> BlockEntityType.Builder.of(CookingPotBlockEntity::new,
                            ModBlocks.COOKING_POT.get()).build(null));

    // 大煮锅（方块 large_cooking_pot）：3x3 网格 + 右上输入 + 燃料 + 输出熔炉式定时加工，GUI 见 menu.LargeCookingPotMenu
    public static final RegistryObject<BlockEntityType<LargeCookingPotBlockEntity>> LARGE_COOKING_POT =
            BLOCK_ENTITIES.register("large_cooking_pot",
                    () -> BlockEntityType.Builder.of(LargeCookingPotBlockEntity::new,
                            ModBlocks.LARGE_COOKING_POT.get()).build(null));

    // 超大煮锅（方块 extra_large_cooking_pot）：4x4 网格 + 右上输入 + 燃料 + 输出熔炉式定时加工，GUI 见 menu.ExtraLargeCookingPotMenu
    public static final RegistryObject<BlockEntityType<ExtraLargeCookingPotBlockEntity>> EXTRA_LARGE_COOKING_POT =
            BLOCK_ENTITIES.register("extra_large_cooking_pot",
                    () -> BlockEntityType.Builder.of(ExtraLargeCookingPotBlockEntity::new,
                            ModBlocks.EXTRA_LARGE_COOKING_POT.get()).build(null));

    // 巨无霸大大大煮锅（方块 jumbo_mega_cooking_pot）：5x5 网格 + 右上输入 + 燃料 + 输出熔炉式定时加工，GUI 见 menu.JumboMegaCookingPotMenu
    public static final RegistryObject<BlockEntityType<JumboMegaCookingPotBlockEntity>> JUMBO_MEGA_COOKING_POT =
            BLOCK_ENTITIES.register("jumbo_mega_cooking_pot",
                    () -> BlockEntityType.Builder.of(JumboMegaCookingPotBlockEntity::new,
                            ModBlocks.JUMBO_MEGA_COOKING_POT.get()).build(null));

    // 铁锅（方块 iron_wok）：菱形 4 输入 + 右上输入 + 燃料 + 输出熔炉式定时加工，GUI 见 menu.IronWokMenu
    public static final RegistryObject<BlockEntityType<IronWokBlockEntity>> IRON_WOK =
            BLOCK_ENTITIES.register("iron_wok",
                    () -> BlockEntityType.Builder.of(IronWokBlockEntity::new,
                            ModBlocks.IRON_WOK.get()).build(null));

    // 烤箱（方块 oven）：GUI 同大煮锅（3x3 网格 9 入）但配方独立，GUI 见 menu.OvenMenu
    public static final RegistryObject<BlockEntityType<OvenBlockEntity>> OVEN =
            BLOCK_ENTITIES.register("oven",
                    () -> BlockEntityType.Builder.of(OvenBlockEntity::new,
                            ModBlocks.OVEN.get()).build(null));

    // 米米猴头（方块 mimi_monkey_head）：纯装饰 GeckoLib 头颅摆件方块，无 GUI 无动画不 tick
    public static final RegistryObject<BlockEntityType<MimiMonkeyHeadBlockEntity>> MIMI_MONKEY_HEAD =
            BLOCK_ENTITIES.register("mimi_monkey_head",
                    () -> BlockEntityType.Builder.of(MimiMonkeyHeadBlockEntity::new,
                            ModBlocks.MIMI_MONKEY_HEAD.get()).build(null));

    // 饭晶虎王头（方块 rice_crystal_tiger_king_head）：纯装饰 GeckoLib 头颅摆件方块，无 GUI 无动画不 tick
    public static final RegistryObject<BlockEntityType<RiceCrystalTigerKingHeadBlockEntity>> RICE_CRYSTAL_TIGER_KING_HEAD =
            BLOCK_ENTITIES.register("rice_crystal_tiger_king_head",
                    () -> BlockEntityType.Builder.of(RiceCrystalTigerKingHeadBlockEntity::new,
                            ModBlocks.RICE_CRYSTAL_TIGER_KING_HEAD.get()).build(null));

    // 超晶牛头（方块 super_crystal_ox_head）：纯装饰 GeckoLib 头颅摆件方块，无 GUI 无动画不 tick
    public static final RegistryObject<BlockEntityType<SuperCrystalOxHeadBlockEntity>> SUPER_CRYSTAL_OX_HEAD =
            BLOCK_ENTITIES.register("super_crystal_ox_head",
                    () -> BlockEntityType.Builder.of(SuperCrystalOxHeadBlockEntity::new,
                            ModBlocks.SUPER_CRYSTAL_OX_HEAD.get()).build(null));

    // 重晶兔王头（方块 heavy_crystal_rabbit_king_head）：纯装饰 GeckoLib 头颅摆件方块，无 GUI 无动画不 tick
    public static final RegistryObject<BlockEntityType<HeavyCrystalRabbitKingHeadBlockEntity>> HEAVY_CRYSTAL_RABBIT_KING_HEAD =
            BLOCK_ENTITIES.register("heavy_crystal_rabbit_king_head",
                    () -> BlockEntityType.Builder.of(HeavyCrystalRabbitKingHeadBlockEntity::new,
                            ModBlocks.HEAVY_CRYSTAL_RABBIT_KING_HEAD.get()).build(null));

    // 复晶天青蟒头（方块 compound_crystal_azure_python_head）：纯装饰 GeckoLib 头颅摆件方块，无 GUI 无动画不 tick
    public static final RegistryObject<BlockEntityType<CompoundCrystalAzurePythonHeadBlockEntity>> COMPOUND_CRYSTAL_AZURE_PYTHON_HEAD =
            BLOCK_ENTITIES.register("compound_crystal_azure_python_head",
                    () -> BlockEntityType.Builder.of(CompoundCrystalAzurePythonHeadBlockEntity::new,
                            ModBlocks.COMPOUND_CRYSTAL_AZURE_PYTHON_HEAD.get()).build(null));

    // 清洁长命夔头（方块 purified_longevity_kui_head）：纯装饰 GeckoLib 头颅摆件方块，无 GUI 无动画不 tick
    public static final RegistryObject<BlockEntityType<PurifiedLongevityKuiHeadBlockEntity>> PURIFIED_LONGEVITY_KUI_HEAD =
            BLOCK_ENTITIES.register("purified_longevity_kui_head",
                    () -> BlockEntityType.Builder.of(PurifiedLongevityKuiHeadBlockEntity::new,
                            ModBlocks.PURIFIED_LONGEVITY_KUI_HEAD.get()).build(null));

    // 最懒终末龙头（方块 laziest_end_dragon_head）：纯装饰 GeckoLib 头颅摆件方块，无 GUI 无动画不 tick
    public static final RegistryObject<BlockEntityType<LaziestEndDragonHeadBlockEntity>> LAZIEST_END_DRAGON_HEAD =
            BLOCK_ENTITIES.register("laziest_end_dragon_head",
                    () -> BlockEntityType.Builder.of(LaziestEndDragonHeadBlockEntity::new,
                            ModBlocks.LAZIEST_END_DRAGON_HEAD.get()).build(null));

    // 堕落十字架（方块 fallen_cross）：纯装饰 GeckoLib 华丽十字架方块，无 GUI 无动画不 tick
    public static final RegistryObject<BlockEntityType<FallenCrossBlockEntity>> FALLEN_CROSS =
            BLOCK_ENTITIES.register("fallen_cross",
                    () -> BlockEntityType.Builder.of(FallenCrossBlockEntity::new,
                            ModBlocks.FALLEN_CROSS.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
