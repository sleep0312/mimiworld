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

    // 造米台（27 号方块 rice_maker）：3 槽加工台，GUI 见 menu.RiceMakerMenu
    public static final RegistryObject<BlockEntityType<RiceMakerBlockEntity>> RICE_MAKER =
            BLOCK_ENTITIES.register("rice_maker",
                    () -> BlockEntityType.Builder.of(RiceMakerBlockEntity::new, ModBlocks.RICE_MAKER.get()).build(null));

    // 锤米台（29 号方块 rice_pounding_table）：2 输入 + 1 输出，工作台式即时合成，GUI 见 menu.RicePoundingTableMenu
    public static final RegistryObject<BlockEntityType<RicePoundingTableBlockEntity>> RICE_POUNDING_TABLE =
            BLOCK_ENTITIES.register("rice_pounding_table",
                    () -> BlockEntityType.Builder.of(RicePoundingTableBlockEntity::new,
                            ModBlocks.RICE_POUNDING_TABLE.get()).build(null));

    // 压饭机（31 号方块 rice_press）：9 输入 + 1 输出，工作台式无序即时合成，GUI 见 menu.RicePressMenu
    public static final RegistryObject<BlockEntityType<RicePressBlockEntity>> RICE_PRESS =
            BLOCK_ENTITIES.register("rice_press",
                    () -> BlockEntityType.Builder.of(RicePressBlockEntity::new,
                            ModBlocks.RICE_PRESS.get()).build(null));

    // 高级锤米台（49 号方块 advanced_rice_pounding_table）：功能同锤米台（2 输入 + 1 输出即时合成），仅 GUI 外观不同
    public static final RegistryObject<BlockEntityType<AdvancedRicePoundingTableBlockEntity>> ADVANCED_RICE_POUNDING_TABLE =
            BLOCK_ENTITIES.register("advanced_rice_pounding_table",
                    () -> BlockEntityType.Builder.of(AdvancedRicePoundingTableBlockEntity::new,
                            ModBlocks.ADVANCED_RICE_POUNDING_TABLE.get()).build(null));

    // 电离台（56 号方块 ionization_table）：双输入定时加工（无燃料），GUI 见 menu.IonizationTableMenu
    public static final RegistryObject<BlockEntityType<IonizationTableBlockEntity>> IONIZATION_TABLE =
            BLOCK_ENTITIES.register("ionization_table",
                    () -> BlockEntityType.Builder.of(IonizationTableBlockEntity::new,
                            ModBlocks.IONIZATION_TABLE.get()).build(null));

    // 沉淀台（57 号方块 precipitation_table）：双输入定时加工（无燃料），GUI 见 menu.PrecipitationTableMenu
    public static final RegistryObject<BlockEntityType<PrecipitationTableBlockEntity>> PRECIPITATION_TABLE =
            BLOCK_ENTITIES.register("precipitation_table",
                    () -> BlockEntityType.Builder.of(PrecipitationTableBlockEntity::new,
                            ModBlocks.PRECIPITATION_TABLE.get()).build(null));

    // 离解台（64 号方块 dissociation_table）：熔炉式有序双输入定时加工（无燃料），GUI 见 menu.DissociationTableMenu
    public static final RegistryObject<BlockEntityType<DissociationTableBlockEntity>> DISSOCIATION_TABLE =
            BLOCK_ENTITIES.register("dissociation_table",
                    () -> BlockEntityType.Builder.of(DissociationTableBlockEntity::new,
                            ModBlocks.DISSOCIATION_TABLE.get()).build(null));

    // 铸剑台（130 号方块 sword_forging_table）：3x3 工作台式即时合成，独立配方表，GUI 见 menu.SwordForgingTableMenu
    public static final RegistryObject<BlockEntityType<SwordForgingTableBlockEntity>> SWORD_FORGING_TABLE =
            BLOCK_ENTITIES.register("sword_forging_table",
                    () -> BlockEntityType.Builder.of(SwordForgingTableBlockEntity::new,
                            ModBlocks.SWORD_FORGING_TABLE.get()).build(null));

    // 铸甲台（131 号方块 armor_forging_table）：3x3 工作台式即时合成，独立配方表，GUI 见 menu.ArmorForgingTableMenu
    public static final RegistryObject<BlockEntityType<ArmorForgingTableBlockEntity>> ARMOR_FORGING_TABLE =
            BLOCK_ENTITIES.register("armor_forging_table",
                    () -> BlockEntityType.Builder.of(ArmorForgingTableBlockEntity::new,
                            ModBlocks.ARMOR_FORGING_TABLE.get()).build(null));

    // 铸弓台（132 号方块 bow_forging_table）：3x3 工作台式即时合成，独立配方表，GUI 见 menu.BowForgingTableMenu
    public static final RegistryObject<BlockEntityType<BowForgingTableBlockEntity>> BOW_FORGING_TABLE =
            BLOCK_ENTITIES.register("bow_forging_table",
                    () -> BlockEntityType.Builder.of(BowForgingTableBlockEntity::new,
                            ModBlocks.BOW_FORGING_TABLE.get()).build(null));

    // 淬剑台（164 号方块 sword_quenching_table）：3 入 1 出定时加工，开始按钮启动，GUI 见 menu.SwordQuenchingTableMenu
    public static final RegistryObject<BlockEntityType<SwordQuenchingTableBlockEntity>> SWORD_QUENCHING_TABLE =
            BLOCK_ENTITIES.register("sword_quenching_table",
                    () -> BlockEntityType.Builder.of(SwordQuenchingTableBlockEntity::new,
                            ModBlocks.SWORD_QUENCHING_TABLE.get()).build(null));

    // 汲血台（166 号方块 bloodletting_table）：2 入 1 出定时加工，GUI 见 menu.BloodlettingTableMenu
    public static final RegistryObject<BlockEntityType<BloodlettingTableBlockEntity>> BLOODLETTING_TABLE =
            BLOCK_ENTITIES.register("bloodletting_table",
                    () -> BlockEntityType.Builder.of(BloodlettingTableBlockEntity::new,
                            ModBlocks.BLOODLETTING_TABLE.get()).build(null));

    // 碾磨台（287 号方块 grinding_table）：6 入 1 出定时加工（加高面板），GUI 见 menu.GrindingTableMenu
    public static final RegistryObject<BlockEntityType<GrindingTableBlockEntity>> GRINDING_TABLE =
            BLOCK_ENTITIES.register("grinding_table",
                    () -> BlockEntityType.Builder.of(GrindingTableBlockEntity::new,
                            ModBlocks.GRINDING_TABLE.get()).build(null));

    // 搅碎器（365 号方块 crusher）：3 入 1 出定时加工，GUI 见 menu.CrusherMenu
    public static final RegistryObject<BlockEntityType<CrusherBlockEntity>> CRUSHER =
            BLOCK_ENTITIES.register("crusher",
                    () -> BlockEntityType.Builder.of(CrusherBlockEntity::new,
                            ModBlocks.CRUSHER.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
