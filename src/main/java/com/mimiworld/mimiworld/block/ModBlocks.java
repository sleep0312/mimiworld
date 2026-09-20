package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.world.tree.RiceTreeGrower;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 米米世界方块注册类。方块本身不参与「物品编码」，但其 BlockItem 参与（见 ModItems 编码表）。
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MimiWorld.MODID);

    // 米原木：轴向原木方块（RotatedPillarBlock），硬度/音效参照原版橡木原木。
    // 可被斧快速挖掘、需要石制工具（挖掘等级 1）由数据标签控制：
    // data/minecraft/tags/blocks/mineable/axe.json 与 needs_stone_tool.json。
    public static final RegistryObject<Block> RICE_LOG =
            BLOCKS.register("rice_log", () -> new RotatedPillarBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    // 米木板：普通全立方块，硬度/音效参照原版橡木木板。
    // 斧可快速挖掘（mineable/axe 标签）；挖掘等级 0 = 不需要 needs_*_tool 标签，空手也能掉落。
    public static final RegistryObject<Block> RICE_PLANKS =
            BLOCKS.register("rice_planks", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    // 米树叶：使用原版 LeavesBlock（带 distance/persistent 状态，远离原木会随机刻腐烂）。
    // 属性参照原版橡木树叶：硬度 0.2、草声、不遮挡光照/不窒息、可被熔岩点燃、被活塞推动时破坏。
    // 剪刀快速采集由 minecraft:leaves 方块标签触发（ShearsItem 硬编码加速），挖掘等级 0。
    public static final RegistryObject<Block> RICE_LEAVES =
            BLOCKS.register("rice_leaves", () -> new LeavesBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.2F)
                            .randomTicks()
                            .sound(SoundType.GRASS)
                            .noOcclusion()
                            .isValidSpawn((state, level, pos, type) -> type == EntityType.OCELOT || type == EntityType.PARROT)
                            .isSuffocating((state, level, pos) -> false)
                            .isViewBlocking((state, level, pos) -> false)
                            .ignitedByLava()
                            .pushReaction(PushReaction.DESTROY)));

    // 米树苗：原版 SaplingBlock（自带随机刻生长 + 骨粉催熟），通过 RiceTreeGrower 长成米树（参照白桦形态）。
    // 属性照抄原版树苗：无碰撞、瞬爆、草声、可随机刻、活塞推动即破坏；只能种在 minecraft:dirt 标签方块上（SaplingBlock 内置检查）。
    public static final RegistryObject<Block> RICE_SAPLING =
            BLOCKS.register("rice_sapling", () -> new SaplingBlock(new RiceTreeGrower(),
                    BlockBehaviour.Properties.of()
                            .noCollission()
                            .randomTicks()
                            .instabreak()
                            .sound(SoundType.GRASS)
                            .pushReaction(PushReaction.DESTROY)));

    // 硬化米：石头类普通全立方块，属性照抄原版石头（硬度 1.5、抗爆 6.0、石声、需正确工具才掉落）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐只能挖得慢且不掉落。
    public static final RegistryObject<Block> HARDENED_RICE =
            BLOCKS.register("hardened_rice", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 深层硬化米：石头类普通全立方块，属性参照原版深板岩（硬度 3.0、抗爆 6.0、深板岩声、需正确工具才掉落）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐只能挖得慢且不掉落。
    public static final RegistryObject<Block> DEEPSLATE_HARDENED_RICE =
            BLOCKS.register("deepslate_hardened_rice", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE)));

    // 基米：基岩类普通全立方块，属性完全照抄原版基岩。
    // strength(-1.0F, 3600000.0F)：硬度 -1 = 生存模式不可破坏，抗爆 3600000 = 爆炸无法摧毁；
    // 因此不配任何 mineable/needs_*_tool 标签（没有任何工具能挖掘它，原版基岩同样如此）。
    // isValidSpawn 禁止生物自然生成在其上，与原版基岩行为一致。
    public static final RegistryObject<Block> RICE_BEDROCK =
            BLOCKS.register("rice_bedrock", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(-1.0F, 3600000.0F)
                            .sound(SoundType.STONE)
                            .isValidSpawn((state, level, pos, type) -> false)));

    // 米草：小型十字植物方块（直接用原版 BushBlock，原版草 Blocks.GRASS 也是它），属性照抄原版草：
    // 瞬爆、无碰撞、可被同类植物替换、草声、活塞推动即破坏、可被熔岩点燃、XZ 随机偏移；只能种在 minecraft:dirt 标签上。
    // 空手破坏不掉任何东西，只有剪刀采集才掉落米草物品（由 loot table 的 match_tool 条件控制）。
    public static final RegistryObject<Block> RICE_GRASS =
            BLOCKS.register("rice_grass", () -> new BushBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .replaceable()
                            .noCollission()
                            .instabreak()
                            .sound(SoundType.GRASS)
                            .offsetType(BlockBehaviour.OffsetType.XZ)
                            .ignitedByLava()
                            .pushReaction(PushReaction.DESTROY)));

    // 结晶米矿石：类似原版铁矿石的石头类矿石。硬度/抗爆照抄铁矿 3.0/3.0，石声，需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 4 = 下界合金镐（needs_netherite_tool 标签）。
    // 继承原版 DropExperienceBlock + UniformInt(3,7)：用正确工具且非精准采集破坏时掉落 3~7 经验球，
    // 精准采集不掉经验（与原版煤矿/钻石矿等掉经验矿石行为一致）。
    public static final RegistryObject<Block> CRYSTALLIZED_RICE_ORE =
            BLOCKS.register("crystallized_rice_ore", () -> new DropExperienceBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 3.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE),
                    UniformInt.of(3, 7)));

    // 重结晶米矿石（18 号，原 deepslate_crystallized_rice_ore 已按中文名整体改名）：
    // 深板岩质地矿石，硬度/抗爆照抄深层铁矿 4.5/3.0，深板岩声；挖掘等级 4 = 下界合金镐；破坏掉 3~7 经验，精准采集不掉；
    // 烧炼得 19 号重结晶米。
    public static final RegistryObject<Block> RECRYSTALLIZED_RICE_ORE =
            BLOCKS.register("recrystallized_rice_ore", () -> new DropExperienceBlock(
                    BlockBehaviour.Properties.of()
                            .strength(4.5F, 3.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE),
                    UniformInt.of(3, 7)));

    // 可燃米矿石：类似原版铁矿石的石头类矿石。硬度/抗爆照抄铁矿 3.0/3.0，石声，需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 4 = 下界合金镐（needs_netherite_tool 标签）。
    // DropExperienceBlock + UniformInt(3,7)：用正确工具且非精准采集破坏时掉 3~7 经验；烧炼得 20 号可燃米。
    public static final RegistryObject<Block> COMBUSTIBLE_RICE_ORE =
            BLOCKS.register("combustible_rice_ore", () -> new DropExperienceBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 3.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE),
                    UniformInt.of(3, 7)));

    // 泥米（22 号）：泥巴质地方块，属性照抄原版泥巴（硬度 0.5、泥声）；
    // 铲可快速挖掘（mineable/shovel 标签）；挖掘等级 2 = 铁铲（needs_iron_tool 标签），低级铲慢挖不掉落。
    public static final RegistryObject<Block> MUD_RICE =
            BLOCKS.register("mud_rice", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.MUD)));

    // 粗米块（26 号）：石头类普通全立方块，属性参照原版石头（硬度 1.5、抗爆 6.0、石声、需正确工具才掉落）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下只能慢挖且不掉落。
    public static final RegistryObject<Block> COARSE_RICE_BLOCK =
            BLOCKS.register("coarse_rice_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 造米机/造米台（27 号 = rice_maker）：石头类机器方块，属性参照原版石头（硬度 1.5、抗爆 6.0、石声、需正确工具才掉落）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 绑定方块实体 RiceMakerBlockEntity：右键打开 2 输入槽 + 1 输出槽的加工 GUI（见 menu.RiceMakerMenu）。
    public static final RegistryObject<Block> RICE_MAKER =
            BLOCKS.register("rice_maker", () -> new RiceMakerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 米块（28 号）：石头类普通全立方块，属性参照原版石头（硬度 1.5、抗爆 6.0、石声、需正确工具才掉落）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    public static final RegistryObject<Block> RICE_BLOCK =
            BLOCKS.register("rice_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 锤米台（29 号）：石头类机器方块，属性参照原版石头（硬度 1.5、抗爆 6.0、石声、需正确工具才掉落）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 绑定方块实体 RicePoundingTableBlockEntity：右键打开 2 输入槽 + 1 输出槽的工作台式合成 GUI
    // （见 menu.RicePoundingTableMenu，当前暂无配方）。
    public static final RegistryObject<Block> RICE_POUNDING_TABLE =
            BLOCKS.register("rice_pounding_table", () -> new RicePoundingTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 压饭机（31 号）：石头类机器方块，属性参照原版石头（硬度 1.5、抗爆 6.0、石声、需正确工具才掉落）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 绑定方块实体 RicePressBlockEntity：右键打开 9 输入槽 + 1 输出槽的工作台式合成 GUI
    // （见 menu.RicePressMenu，当前暂无配方）。
    public static final RegistryObject<Block> RICE_PRESS =
            BLOCKS.register("rice_press", () -> new RicePressBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 高级锤米台（49 号）：29 号锤米台的合金强化版，石头类机器方块，属性参照原版石头
    // （硬度 1.5、抗爆 6.0、石声、需正确工具才掉落）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/advanced_rice_pounding_table.json）。
    // 绑定方块实体 AdvancedRicePoundingTableBlockEntity：功能与锤米台完全一致（2 输入 + 1 输出，
    // 工作台式即时合成，当前无配方），仅 GUI 贴图外观不同（右上角金锤装饰）。
    public static final RegistryObject<Block> ADVANCED_RICE_POUNDING_TABLE =
            BLOCKS.register("advanced_rice_pounding_table", () -> new AdvancedRicePoundingTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 米矿石（52 号）：类似原版铁矿石的石头类矿石。硬度/抗爆照抄铁矿 3.0/3.0，石声，需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 继承原版 DropExperienceBlock + UniformInt(3,7)：用正确工具且非精准采集破坏时掉 3~7 经验球（精准采集不掉经验）；
    // 方块本身由 loot_tables/blocks/rice_ore.json 掉落。
    public static final RegistryObject<Block> RICE_ORE =
            BLOCKS.register("rice_ore", () -> new DropExperienceBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 3.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE),
                    UniformInt.of(3, 7)));

    // 超结晶块（54 号）：51 号超结晶的压缩存储块，金属块类全立方块（硬度照铁块 5.0、抗爆 6.0、紫晶声）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/super_crystal_block.json）。
    public static final RegistryObject<Block> SUPER_CRYSTAL_BLOCK =
            BLOCKS.register("super_crystal_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(5.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.AMETHYST)));

    // 电离台（56 号）：双输入定时加工机器方块（右键开 GUI，无燃料，倒三角加工进度），金属质地、硬度 3.0、抗爆 6.0。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/ionization_table.json）及槽内物品。
    public static final RegistryObject<Block> IONIZATION_TABLE =
            BLOCKS.register("ionization_table", () -> new IonizationTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 沉淀台（57 号）：双输入定时加工机器方块（右键开 GUI，无燃料，正三角加工进度），金属质地、硬度 3.0、抗爆 6.0。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/precipitation_table.json）及槽内物品。
    public static final RegistryObject<Block> PRECIPITATION_TABLE =
            BLOCKS.register("precipitation_table", () -> new PrecipitationTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 离解台（64 号）：工艺机器方块（当前为装饰性全立方块，无 GUI/交互逻辑），金属质地、硬度 3.0、抗爆 6.0。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/dissociation_table.json）。
    public static final RegistryObject<Block> DISSOCIATION_TABLE =
            BLOCKS.register("dissociation_table", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 米石块（81 号）：53 号米石的压缩存储块，石头类全立方块（硬度 3.0、抗爆 6.0、石声）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/rice_stone_block.json）。
    public static final RegistryObject<Block> RICE_STONE_BLOCK =
            BLOCKS.register("rice_stone_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 铸剑台（130 号）：装备锻造机器方块（当前为装饰性全立方块，无 GUI/交互逻辑），金属质地、硬度 3.0、抗爆 6.0。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/sword_forging_table.json）。
    public static final RegistryObject<Block> SWORD_FORGING_TABLE =
            BLOCKS.register("sword_forging_table", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 铸甲台（131 号）：装备锻造机器方块（当前为装饰性全立方块，无 GUI/交互逻辑），金属质地、硬度 3.0、抗爆 6.0。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/armor_forging_table.json）。
    public static final RegistryObject<Block> ARMOR_FORGING_TABLE =
            BLOCKS.register("armor_forging_table", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 铸弓台（132 号）：装备锻造机器方块（当前为装饰性全立方块，无 GUI/交互逻辑），金属质地、硬度 3.0、抗爆 6.0。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/bow_forging_table.json）。
    public static final RegistryObject<Block> BOW_FORGING_TABLE =
            BLOCKS.register("bow_forging_table", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 淬剑台（164 号）：剑类淬火机器方块（当前为装饰性全立方块，无 GUI/交互逻辑），金属质地、硬度 3.0、抗爆 6.0。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/sword_quenching_table.json）。
    public static final RegistryObject<Block> SWORD_QUENCHING_TABLE =
            BLOCKS.register("sword_quenching_table", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 汲血台（166 号）：汲取血液的机器方块（当前为装饰性全立方块，无 GUI/交互逻辑），金属质地、硬度 3.0、抗爆 6.0。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/bloodletting_table.json）。
    public static final RegistryObject<Block> BLOODLETTING_TABLE =
            BLOCKS.register("bloodletting_table", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 堕落台（271 号）：堕落力量相关的仪式台方块（当前为装饰性全立方块，无 GUI/交互逻辑），
    // 暗下界合金质地、硬度 3.0、抗爆 6.0，需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/fallen_altar.json）。
    public static final RegistryObject<Block> FALLEN_ALTAR =
            BLOCKS.register("fallen_altar", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.NETHERITE_BLOCK)));

    // 碾磨台（287 号）：石质碾磨工作台（当前为装饰性全立方块，无 GUI/交互逻辑），
    // 硬度照原版砂轮 2.0、抗爆 6.0、石声、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/grinding_table.json）。
    public static final RegistryObject<Block> GRINDING_TABLE =
            BLOCKS.register("grinding_table", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(2.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 磨制残堕深渊岩（288 号）：残堕深渊岩石的磨制变体，深板岩质地全立方块（硬度 3.0、抗爆 6.0、深板岩声）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/polished_remnant_fallen_abyss_rock.json）。
    public static final RegistryObject<Block> POLISHED_REMNANT_FALLEN_ABYSS_ROCK =
            BLOCKS.register("polished_remnant_fallen_abyss_rock", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE)));

    // 杵界台（289 号）：杵界仪式工作台方块（当前为装饰性全立方块，无 GUI/交互逻辑），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/pestle_realm_table.json）。
    public static final RegistryObject<Block> PESTLE_REALM_TABLE =
            BLOCKS.register("pestle_realm_table", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 凝锭台（292 号）：熔融金属冷凝成锭的工艺机器方块（当前为装饰性全立方块，无 GUI/交互逻辑），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/ingot_condensation_table.json）。
    public static final RegistryObject<Block> INGOT_CONDENSATION_TABLE =
            BLOCKS.register("ingot_condensation_table", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 搅碎器（365 号 BlockItem）：食材加工机器方块（当前为装饰性全立方块，无 GUI/交互逻辑），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/crusher.json）。
    public static final RegistryObject<Block> CRUSHER =
            BLOCKS.register("crusher", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 搅拌器（366 号 BlockItem）：食材加工机器方块（当前为装饰性全立方块，无 GUI/交互逻辑），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/mixer.json）。
    public static final RegistryObject<Block> MIXER =
            BLOCKS.register("mixer", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 大煮锅（369 号 BlockItem）：烹饪机器方块（当前为装饰性全立方块，无 GUI/交互逻辑），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/large_cooking_pot.json）。
    public static final RegistryObject<Block> LARGE_COOKING_POT =
            BLOCKS.register("large_cooking_pot", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 超大煮锅（370 号 BlockItem）：369 大煮锅的强化版烹饪机器方块（当前为装饰性全立方块，无 GUI/交互逻辑），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/extra_large_cooking_pot.json）。
    public static final RegistryObject<Block> EXTRA_LARGE_COOKING_POT =
            BLOCKS.register("extra_large_cooking_pot", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 铁锅（372 号 BlockItem）：烹饪机器方块（当前为装饰性全立方块，无 GUI/交互逻辑），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/iron_wok.json）。
    public static final RegistryObject<Block> IRON_WOK =
            BLOCKS.register("iron_wok", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 烤箱（373 号 BlockItem）：烹饪机器方块（当前为装饰性全立方块，无 GUI/交互逻辑），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/oven.json）。
    public static final RegistryObject<Block> OVEN =
            BLOCKS.register("oven", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
