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
 * 米米世界方块注册类。方块在此注册，其 BlockItem 在 ModItems 中注册。
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
    // 战利品表同原版矿石（loot_tables/blocks/crystallized_rice_ore.json）：普通开采掉结晶米
    // （时运按 ore_drops 增产），精准采集才掉矿石方块本身。
    // 继承原版 DropExperienceBlock + UniformInt(3,7)：用正确工具且非精准采集破坏时另掉 3~7 经验球，
    // 精准采集不掉经验（与原版煤矿/钻石矿等掉经验矿石行为一致）。
    public static final RegistryObject<Block> CRYSTALLIZED_RICE_ORE =
            BLOCKS.register("crystallized_rice_ore", () -> new DropExperienceBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 3.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE),
                    UniformInt.of(3, 7)));

    // 重结晶米矿石（，原 deepslate_crystallized_rice_ore 已按中文名整体改名）：
    // 深板岩质地矿石，硬度/抗爆照抄深层铁矿 4.5/3.0，深板岩声；挖掘等级 4 = 下界合金镐。
    // 战利品表同原版矿石：普通开采掉重结晶米（时运增产），精准采集掉矿石本身，非精准另掉 3~7 经验；
    // 矿石也可烧炼得重结晶米。
    public static final RegistryObject<Block> RECRYSTALLIZED_RICE_ORE =
            BLOCKS.register("recrystallized_rice_ore", () -> new DropExperienceBlock(
                    BlockBehaviour.Properties.of()
                            .strength(4.5F, 3.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE),
                    UniformInt.of(3, 7)));

    // 可燃米矿石：类似原版铁矿石的石头类矿石。硬度/抗爆照抄铁矿 3.0/3.0，石声，需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 4 = 下界合金镐（needs_netherite_tool 标签）。
    // 战利品表同原版矿石：普通开采掉可燃米（时运增产），精准采集掉矿石本身，非精准另掉 3~7 经验；
    // 矿石也可烧炼得可燃米。
    public static final RegistryObject<Block> COMBUSTIBLE_RICE_ORE =
            BLOCKS.register("combustible_rice_ore", () -> new DropExperienceBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 3.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE),
                    UniformInt.of(3, 7)));

    // 泥米（）：泥巴质地方块，属性照抄原版泥巴（硬度 0.5、泥声）；
    // 铲可快速挖掘（mineable/shovel 标签）；挖掘等级 2 = 铁铲（needs_iron_tool 标签），低级铲慢挖不掉落。
    public static final RegistryObject<Block> MUD_RICE =
            BLOCKS.register("mud_rice", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.MUD)));

    // 粗米块（）：石头类普通全立方块，属性参照原版石头（硬度 1.5、抗爆 6.0、石声、需正确工具才掉落）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下只能慢挖且不掉落。
    public static final RegistryObject<Block> COARSE_RICE_BLOCK =
            BLOCKS.register("coarse_rice_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 造米机/造米台（ = rice_maker）：石头类机器方块，属性参照原版石头（硬度 1.5、抗爆 6.0、石声、需正确工具才掉落）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 绑定方块实体 RiceMakerBlockEntity：右键打开 2 输入槽 + 1 输出槽的加工 GUI（见 menu.RiceMakerMenu）。
    public static final RegistryObject<Block> RICE_MAKER =
            BLOCKS.register("rice_maker", () -> new RiceMakerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 米块（）：石头类普通全立方块，属性参照原版石头（硬度 1.5、抗爆 6.0、石声、需正确工具才掉落）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    public static final RegistryObject<Block> RICE_BLOCK =
            BLOCKS.register("rice_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 锤米台（）：石头类机器方块，属性参照原版石头（硬度 1.5、抗爆 6.0、石声、需正确工具才掉落）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 绑定方块实体 RicePoundingTableBlockEntity：右键打开 2 输入槽 + 1 输出槽的工作台式合成 GUI
    // （见 menu.RicePoundingTableMenu，当前暂无配方）。
    public static final RegistryObject<Block> RICE_POUNDING_TABLE =
            BLOCKS.register("rice_pounding_table", () -> new RicePoundingTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 压饭机（）：石头类机器方块，属性参照原版石头（硬度 1.5、抗爆 6.0、石声、需正确工具才掉落）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 绑定方块实体 RicePressBlockEntity：右键打开 9 输入槽 + 1 输出槽的工作台式合成 GUI
    // （见 menu.RicePressMenu，当前暂无配方）。
    public static final RegistryObject<Block> RICE_PRESS =
            BLOCKS.register("rice_press", () -> new RicePressBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 高级锤米台（）：锤米台的合金强化版，石头类机器方块，属性参照原版石头
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

    // 米矿石（）：类似原版铁矿石的石头类矿石。硬度/抗爆照抄铁矿 3.0/3.0，石声，需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 战利品表同原版矿石（loot_tables/blocks/rice_ore.json）：普通开采掉米石（时运增产），
    // 精准采集才掉矿石方块本身；继承原版 DropExperienceBlock + UniformInt(3,7)：非精准采集另掉 3~7 经验球，
    // 精准采集不掉经验；矿石也可烧炼成米石。
    public static final RegistryObject<Block> RICE_ORE =
            BLOCKS.register("rice_ore", () -> new DropExperienceBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 3.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE),
                    UniformInt.of(3, 7)));

    // 超结晶块（）：超结晶的压缩存储块，金属块类全立方块（硬度照铁块 5.0、抗爆 6.0、紫晶声）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/super_crystal_block.json）。
    public static final RegistryObject<Block> SUPER_CRYSTAL_BLOCK =
            BLOCKS.register("super_crystal_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(5.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.AMETHYST)));

    // 电离台（）：双输入定时加工机器方块（右键开 GUI，无燃料，倒三角加工进度），金属质地、硬度 3.0、抗爆 6.0。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/ionization_table.json）及槽内物品。
    public static final RegistryObject<Block> IONIZATION_TABLE =
            BLOCKS.register("ionization_table", () -> new IonizationTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 沉淀台（）：双输入定时加工机器方块（右键开 GUI，无燃料，正三角加工进度），金属质地、硬度 3.0、抗爆 6.0。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/precipitation_table.json）及槽内物品。
    public static final RegistryObject<Block> PRECIPITATION_TABLE =
            BLOCKS.register("precipitation_table", () -> new PrecipitationTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 离解台（）：工艺机器方块（绑定方块实体），右键打开熔炉式加工 GUI（左上输入→右上输出，正下第二输入，无燃料）。
    // 金属质地、硬度 3.0、抗爆 6.0。镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/dissociation_table.json）；方块实体见 block.entity.DissociationTableBlockEntity。
    public static final RegistryObject<Block> DISSOCIATION_TABLE =
            BLOCKS.register("dissociation_table", () -> new DissociationTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 米石块（）：米石的压缩存储块，石头类全立方块（硬度 3.0、抗爆 6.0、石声）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/rice_stone_block.json）。
    public static final RegistryObject<Block> RICE_STONE_BLOCK =
            BLOCKS.register("rice_stone_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 铸剑台（）：装备锻造机器方块（SwordForgingTableBlock），右键打开原版工作台外观的 3x3 即时合成 GUI，使用独立配方表（与其他锻造台/原版工作台不通用）。
    // 金属质地、硬度 3.0、抗爆 6.0。镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落网格内物品（loot_tables/blocks/sword_forging_table.json 掉方块本身）。
    public static final RegistryObject<Block> SWORD_FORGING_TABLE =
            BLOCKS.register("sword_forging_table", () -> new SwordForgingTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 铸甲台（）：装备锻造机器方块（ArmorForgingTableBlock），右键打开原版工作台外观的 3x3 即时合成 GUI，使用独立配方表（与其他锻造台/原版工作台不通用）。
    // 金属质地、硬度 3.0、抗爆 6.0。镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落网格内物品（loot_tables/blocks/armor_forging_table.json 掉方块本身）。
    public static final RegistryObject<Block> ARMOR_FORGING_TABLE =
            BLOCKS.register("armor_forging_table", () -> new ArmorForgingTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 铸弓台（）：装备锻造机器方块（BowForgingTableBlock），右键打开原版工作台外观的 3x3 即时合成 GUI，使用独立配方表（与其他锻造台/原版工作台不通用）。
    // 金属质地、硬度 3.0、抗爆 6.0。镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落网格内物品（loot_tables/blocks/bow_forging_table.json 掉方块本身）。
    public static final RegistryObject<Block> BOW_FORGING_TABLE =
            BLOCKS.register("bow_forging_table", () -> new BowForgingTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 淬剑台：剑类淬火机器方块（SwordQuenchingTableBlock），右键打开 3 入 1 出加工 GUI，右侧"开始"按钮按下才加工一轮。
    // 金属质地、硬度 3.0、抗爆 6.0。镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落槽内物品（loot_tables/blocks/sword_quenching_table.json 掉方块本身）。
    public static final RegistryObject<Block> SWORD_QUENCHING_TABLE =
            BLOCKS.register("sword_quenching_table", () -> new SwordQuenchingTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 汲血台：汲取血液的机器方块（BloodlettingTableBlock），右键打开 2 入 1 出熔炉式加工 GUI（左列两输入、右侧输出）。
    // 金属质地、硬度 3.0、抗爆 6.0。镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落槽内物品（loot_tables/blocks/bloodletting_table.json 掉方块本身）。
    public static final RegistryObject<Block> BLOODLETTING_TABLE =
            BLOCKS.register("bloodletting_table", () -> new BloodlettingTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 堕落台（）：堕落力量相关的仪式台方块（当前为装饰性全立方块，无 GUI/交互逻辑），
    // 暗下界合金质地、硬度 3.0、抗爆 6.0，需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/fallen_altar.json）。
    public static final RegistryObject<Block> FALLEN_ALTAR =
            BLOCKS.register("fallen_altar", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.NETHERITE_BLOCK)));

    // 堕落十字架（fallen_cross）：带基座的华丽十字架装饰方块（FallenCrossBlock），
    // 世界外观由 GeckoLib BlockEntityRenderer 渲染（紫黑本体 + 金纹 + 双面紫宝石，BER 内缩放收进一格贴地居中）。
    // 暗下界合金质地、硬度 3.0、抗爆 6.0，需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/fallen_cross.json）。
    public static final RegistryObject<Block> FALLEN_CROSS =
            BLOCKS.register("fallen_cross", () -> new FallenCrossBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.NETHERITE_BLOCK)));

    // 碾磨台：石质碾磨工作台（GrindingTableBlock），右键打开 6 入 1 出熔炉式加工 GUI（左右两列各 3 输入、底部输出，面板加高），
    // 硬度照原版砂轮 2.0、抗爆 6.0、石声、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落槽内物品（loot_tables/blocks/grinding_table.json 掉方块本身）。
    public static final RegistryObject<Block> GRINDING_TABLE =
            BLOCKS.register("grinding_table", () -> new GrindingTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // 残堕深渊岩（）：深渊层原生岩石，深板岩质地全立方块（硬度 3.0、抗爆 6.0、深板岩声）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/remnant_fallen_abyss_rock.json）。
    // 同时是粗制残堕世界精华、碾磨台的合成材料。
    public static final RegistryObject<Block> REMNANT_FALLEN_ABYSS_ROCK =
            BLOCKS.register("remnant_fallen_abyss_rock", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE)));

    // 残堕基岩（）：深渊层致密基岩，硬度 4.0、抗爆 9.0、深板岩声，比深渊岩更坚固。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/remnant_fallen_bedrock.json）。
    public static final RegistryObject<Block> REMNANT_FALLEN_BEDROCK =
            BLOCKS.register("remnant_fallen_bedrock", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(4.0F, 9.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE)));

    // 残堕界传送门方块（remnant_fallen_portal）：由「四个堕落台 + 四个堕落十字架」的 4×4 仪式
    // 自动生成的贴地传送门（RemnantFallenPortalBlock），外观为薄薄一层紫色星空平面，
    // 行为参照原版末地门：无碰撞、不遮挡、发光 11、不可破坏（生存模式无法挖掘，无 BlockItem、无战利品表），
    // 实体踩入即与残堕界双向传送（落点见 world/RemnantFallenTeleporter）。
    public static final RegistryObject<Block> REMNANT_FALLEN_PORTAL =
            BLOCKS.register("remnant_fallen_portal", () -> new RemnantFallenPortalBlock(
                    BlockBehaviour.Properties.of()
                            .noCollission()
                            .noOcclusion()
                            .lightLevel(state -> 11)
                            .strength(-1.0F, 3600000.0F)
                            .sound(SoundType.GLASS)));

    // 磨制残堕深渊岩（）：残堕深渊岩石的磨制变体，深板岩质地全立方块（硬度 3.0、抗爆 6.0、深板岩声）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/polished_remnant_fallen_abyss_rock.json）。
    public static final RegistryObject<Block> POLISHED_REMNANT_FALLEN_ABYSS_ROCK =
            BLOCKS.register("polished_remnant_fallen_abyss_rock", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE)));

    // 杵界台（）：杵界仪式工作台方块（PestleRealmTableBlock），右键打开 2x2 网格（4 输入）+右下输出槽即时合成 GUI，
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/pestle_realm_table.json）。
    public static final RegistryObject<Block> PESTLE_REALM_TABLE =
            BLOCKS.register("pestle_realm_table", () -> new PestleRealmTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 凝锭台：熔融金属冷凝成锭的工艺机器方块（IngotCondensationTableBlock），右键打开 5x5 网格（25 输入）+下方中央输出槽即时合成 GUI，
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 2 = 铁镐（needs_iron_tool 标签），木/石镐慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/ingot_condensation_table.json）。
    public static final RegistryObject<Block> INGOT_CONDENSATION_TABLE =
            BLOCKS.register("ingot_condensation_table", () -> new IngotCondensationTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 搅碎器（BlockItem）：食材加工机器方块（CrusherBlock），右键打开 3 入 1 出熔炉式加工 GUI（左上/左下/右上输入、右下输出），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落槽内物品（loot_tables/blocks/crusher.json 掉方块本身）。
    public static final RegistryObject<Block> CRUSHER =
            BLOCKS.register("crusher", () -> new CrusherBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 搅拌器（BlockItem）：9 输入 + 1 输出工作台式即时合成机器方块（右键开 GUI），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/mixer.json）。
    public static final RegistryObject<Block> MIXER =
            BLOCKS.register("mixer", () -> new MixerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 大煮锅（ BlockItem）：3x3 共 9 个网格输入槽 + 右上输入 + 燃料 + 输出的熔炉式煮锅（右键开 GUI），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/large_cooking_pot.json）。
    public static final RegistryObject<Block> LARGE_COOKING_POT =
            BLOCKS.register("large_cooking_pot", () -> new LargeCookingPotBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 超大煮锅（ BlockItem）：4x4 共 16 个网格输入槽 + 右上输入 + 燃料 + 输出的熔炉式煮锅（右键开 GUI），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/extra_large_cooking_pot.json）。
    public static final RegistryObject<Block> EXTRA_LARGE_COOKING_POT =
            BLOCKS.register("extra_large_cooking_pot", () -> new ExtraLargeCookingPotBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 铁锅（BlockItem）：菱形 4 输入 + 右上输入 + 燃料 + 输出的熔炉式炒锅（右键开 GUI），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/iron_wok.json）。
    public static final RegistryObject<Block> IRON_WOK =
            BLOCKS.register("iron_wok", () -> new IronWokBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 烤箱（ BlockItem）：GUI 同大煮锅（3x3 网格 9 入 + 右上输入 + 燃料 + 输出）但配方独立（右键开 GUI），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/oven.json）。
    public static final RegistryObject<Block> OVEN =
            BLOCKS.register("oven", () -> new OvenBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 锅（ BlockItem）：基础铁制烹饪锅方块（当前为装饰性全立方块，无 GUI/交互逻辑），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/pot.json）。
    public static final RegistryObject<Block> POT =
            BLOCKS.register("pot", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 大锅（ BlockItem）：铜箍加固的大型烹饪锅方块（当前为装饰性全立方块，无 GUI/交互逻辑），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/big_pot.json）。
    public static final RegistryObject<Block> BIG_POT =
            BLOCKS.register("big_pot", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 小煮锅（BlockItem）：带支脚的小型烹饪锅方块，1 输入 + 1 燃料 + 1 输出熔炉式定时加工（右键开 GUI），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/small_cooking_pot.json）。
    public static final RegistryObject<Block> SMALL_COOKING_POT =
            BLOCKS.register("small_cooking_pot", () -> new SmallCookingPotBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 煮锅（ BlockItem）：2x2 共 4 个网格输入槽 + 右上输入 + 燃料 + 输出的熔炉式煮锅（右键开 GUI），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/cooking_pot.json）。
    public static final RegistryObject<Block> COOKING_POT =
            BLOCKS.register("cooking_pot", () -> new CookingPotMachineBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 龙鳞块（ BlockItem）：龙鳞的压缩存储块，致密龙鳞质地全立方块
    // （硬度照铁块 5.0、抗爆 6.0、下界合金块声、需正确工具才掉落）。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签），铁镐及以下慢挖不掉落。
    // 破坏掉落本身（loot_tables/blocks/dragon_scale_block.json）。
    public static final RegistryObject<Block> DRAGON_SCALE_BLOCK =
            BLOCKS.register("dragon_scale_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(5.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.NETHERITE_BLOCK)));

    // 巨无霸大大大煮锅（ BlockItem）：5x5 共 25 个网格输入槽 + 右上输入 + 燃料 + 输出的熔炉式煮锅（右键开 GUI），
    // 金属质地、硬度 3.0、抗爆 6.0、需正确工具才掉落。
    // 镐可快速挖掘（mineable/pickaxe 标签）；挖掘等级 3 = 钻石镐（needs_diamond_tool 标签）。
    // 破坏掉落本身（loot_tables/blocks/jumbo_mega_cooking_pot.json）。
    public static final RegistryObject<Block> JUMBO_MEGA_COOKING_POT =
            BLOCKS.register("jumbo_mega_cooking_pot", () -> new JumboMegaCookingPotBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    // 米米猴头（BlockItem，沿用物品 id mimi_monkey_head）：米米猴王掉落的头颅摆件方块，
    // 外观由 GeckoLib 渲染（猴头+金冠+双雉鸡翎），木质音效、硬度 1.0、无工具要求，空手即可采集。
    // 破坏掉落本身（loot_tables/blocks/mimi_monkey_head.json）。
    public static final RegistryObject<Block> MIMI_MONKEY_HEAD =
            BLOCKS.register("mimi_monkey_head", () -> new MimiMonkeyHeadBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    // 饭晶虎王头（BlockItem，沿用物品 id rice_crystal_tiger_king_head）：饭晶虎王掉落的头颅摆件方块，
    // 外观由 GeckoLib 渲染（虎头+双耳+冠+下颌），木质音效、硬度 1.0、空手可采，破坏掉落本身。
    public static final RegistryObject<Block> RICE_CRYSTAL_TIGER_KING_HEAD =
            BLOCKS.register("rice_crystal_tiger_king_head", () -> new RiceCrystalTigerKingHeadBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    // 超晶牛头（BlockItem，沿用物品 id super_crystal_ox_head）：超晶牛王掉落的头颅摆件方块，
    // 外观由 GeckoLib 渲染（牛头+双角+双耳），木质音效、硬度 1.0、空手可采，破坏掉落本身。
    public static final RegistryObject<Block> SUPER_CRYSTAL_OX_HEAD =
            BLOCKS.register("super_crystal_ox_head", () -> new SuperCrystalOxHeadBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    // 重晶兔王头（BlockItem，沿用物品 id heavy_crystal_rabbit_king_head）：重晶兔王掉落的头颅摆件方块，
    // 外观由 GeckoLib 渲染（兔头+长双耳），木质音效、硬度 1.0、空手可采，破坏掉落本身。
    public static final RegistryObject<Block> HEAVY_CRYSTAL_RABBIT_KING_HEAD =
            BLOCKS.register("heavy_crystal_rabbit_king_head", () -> new HeavyCrystalRabbitKingHeadBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    // 复晶天青蟒头（BlockItem，沿用物品 id compound_crystal_azure_python_head）：复晶天青蟒掉落的头颅摆件方块，
    // 外观由 GeckoLib 渲染（蟒头+下颌+舌+角），木质音效、硬度 1.0、空手可采，破坏掉落本身。
    public static final RegistryObject<Block> COMPOUND_CRYSTAL_AZURE_PYTHON_HEAD =
            BLOCKS.register("compound_crystal_azure_python_head", () -> new CompoundCrystalAzurePythonHeadBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    // 清洁长命夔头（BlockItem，沿用物品 id purified_longevity_kui_head）：清洁长命夔掉落的头颅摆件方块，
    // 外观由 GeckoLib 渲染（夔头+长颌），木质音效、硬度 1.0、空手可采，破坏掉落本身。
    public static final RegistryObject<Block> PURIFIED_LONGEVITY_KUI_HEAD =
            BLOCKS.register("purified_longevity_kui_head", () -> new PurifiedLongevityKuiHeadBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    // 最懒终末龙头（BlockItem，沿用物品 id laziest_end_dragon_head）：最懒终末龙掉落的头颅摆件方块，
    // 外观由 GeckoLib 渲染（Promax 龙头+下颌，双角獠牙紫眼），木质音效、硬度 1.0、空手可采，破坏掉落本身。
    public static final RegistryObject<Block> LAZIEST_END_DRAGON_HEAD =
            BLOCKS.register("laziest_end_dragon_head", () -> new LaziestEndDragonHeadBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
