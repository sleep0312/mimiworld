package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.world.tree.RiceTreeGrower;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
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

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
