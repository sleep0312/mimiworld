package com.mimiworld.mimiworld;

import com.mimiworld.mimiworld.fluid.ModFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

/**
 * 饭桶：绑定饭汤流体，行为与原版水桶完全一致——右键把饭汤倒入世界，
 * 对饭汤源方块右键可重新装回（倒出/装填音效来自饭汤 FluidType 的 BUCKET_EMPTY/BUCKET_FILL 配置）。
 * 原版空铁桶也能舀起饭汤源（Forge 流体经 ForgeFlowingFluid.Properties#bucket 关联本物品，
 * LiquidBlock.pickupBlock 会返回本物品）。
 * 最大堆叠 1，倒空后返还原版空桶。
 */
public class RicePailItem extends BucketItem {
    public RicePailItem(Properties properties) {
        super(ModFluids.RICE_BROTH, properties);
    }

    // 保留可显式传流体的构造器，便于后续派生其他流体桶
    @SuppressWarnings("unused")
    public RicePailItem(Supplier<? extends Fluid> fluid, Properties properties) {
        super(fluid, properties);
    }
}
