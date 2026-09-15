package com.mimiworld.mimiworld.world.tree;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

/**
 * 米树生长器（1.20.1 官方映射抽象类为 AbstractTreeGrower，1.20.4+ 才改名 TreeGrower）。
 * 无巨型变种、无花树变种，普通生长与骨粉催熟都返回 rice_tree（形态参照白桦树）。
 *
 * 1.20.1 注册要点（已用 javap + runClient 核实，勿按高版本教程写）：
 * - configured_feature 是「动态/数据包注册表」，BuiltInRegistries 中不存在该注册表，
 *   Forge 不会为它触发 RegisterEvent，因此 DeferredRegister.create(Registries.CONFIGURED_FEATURE, ...)
 *   在 1.20.1 运行时直接崩溃（Unable to find registry with key ...，该写法 1.20.4+ 才支持）；
 * - 正确方式：用数据包 JSON 定义 data/mimiworld/worldgen/configured_feature/rice_tree.json
 *   （内容照抄原版 data/minecraft/worldgen/configured_feature/birch.json，仅替换原木/树叶），
 *   世界加载时动态注册表自动收录；
 * - AbstractTreeGrower 只有无参构造器；getConfiguredFeature 返回 ResourceKey（非 Holder/Supplier），
 *   生长时 SaplingBlock 用该 key 从 ServerLevel 的 registryAccess 解析 Holder。
 */
public class RiceTreeGrower extends AbstractTreeGrower {

    // 与数据包文件 data/mimiworld/worldgen/configured_feature/rice_tree.json 一一对应
    public static final ResourceKey<ConfiguredFeature<?, ?>> RICE_TREE_KEY =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath("mimiworld", "rice_tree"));

    @Nullable
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
        return RICE_TREE_KEY;
    }
}
