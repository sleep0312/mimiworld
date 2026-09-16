package com.mimiworld.mimiworld;

import com.mimiworld.mimiworld.fluid.ModFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

/**
 * 米桶（9 号物品）：绑定米汤流体，行为与原版水桶完全一致——右键把米汤倒入世界/空炼药锅，
 * 对米汤源方块右键可重新装回（倒出/装填音效来自米汤 FluidType 的 BUCKET_EMPTY/BUCKET_FILL 配置）。
 * 原版空铁桶也能舀起米汤源（Forge 流体经 ForgeFlowingFluid.Properties#bucket 关联本物品，
 * LiquidBlock.pickupBlock 会返回本物品）；装满的米汤炼药锅同样可用空桶舀回本物品。
 * 最大堆叠 1，倒空后返还原版空桶。
 */
public class RiceBucketItem extends BucketItem {
    public RiceBucketItem(Properties properties) {
        super(ModFluids.RICE_SOUP, properties);
    }

    // 保留可显式传流体的构造器，便于后续派生其他米桶（当前使用默认绑定米汤的构造器）
    @SuppressWarnings("unused")
    public RiceBucketItem(Supplier<? extends Fluid> fluid, Properties properties) {
        super(fluid, properties);
    }
}
