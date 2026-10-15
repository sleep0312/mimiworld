package com.mimiworld.mimiworld;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * 米米世界自定义维度的 ResourceKey。
 * 维度本身由数据包 JSON 注册：
 * - data/mimiworld/dimension_type/mimi_dimension.json
 * - data/mimiworld/dimension/mimi_dimension.json
 * 此处仅持有 Key，供后续传送/填充物品等代码引用。
 */
public class ModDimensions {
    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MimiWorld.MODID, path);
    }

    // 米米维度（世界维度 Key，用于 Level 查找、传送等）
    public static final ResourceKey<Level> MIMI_DIMENSION =
            ResourceKey.create(Registries.DIMENSION, id("mimi_dimension"));

    // 米米维度类型 Key
    public static final ResourceKey<DimensionType> MIMI_DIMENSION_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, id("mimi_dimension"));

    // 残堕界（Remnant Fallen Realm，世界维度 Key）：生成逻辑参照原版地狱，
    // 由数据包 JSON 注册：data/mimiworld/dimension_type/remnant_fallen_realm.json
    // 与 data/mimiworld/dimension/remnant_fallen_realm.json。
    public static final ResourceKey<Level> REMNANT_FALLEN_REALM =
            ResourceKey.create(Registries.DIMENSION, id("remnant_fallen_realm"));

    // 残堕界维度类型 Key
    public static final ResourceKey<DimensionType> REMNANT_FALLEN_REALM_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, id("remnant_fallen_realm"));
}
