package com.mimiworld.mimiworld;

import net.minecraft.world.item.Item;

/**
 * 米桶（9 号物品）：定位为类似原版水桶的「流体容器」，最大堆叠 1。
 *
 * 当前状态：对应的自定义流体待定，因此本类先继承普通 {@link Item}，不实现右键放置/盛装逻辑。
 * 待流体确定后，推荐按以下两种方式之一接入（均只需小改本类与 ModFluids）：
 *
 * 方式一（推荐，与水桶完全同款，流体可放在世界中形成流体方块）：
 *   1. 新建 fluid/ModFluids（DeferredRegister&lt;ForgeFlowingFluid&gt;），注册源流体/流动流体/流体方块/桶用物品引用；
 *   2. 把本类改为 extends net.minecraft.world.item.BucketItem，
 *      构造器 super(fluidSupplier, properties)，ModItems 工厂传入该流体 Supplier；
 *   3. 流体注册时把其 defaultBucket 设为本物品（Fluids.buildAttributes 或 FluidType 的 bucket 属性）。
 *
 * 方式二（不需要世界流体，只是右键在方块上产生效果）：
 *   直接 override use(...) / useOn(...) 编写自定义交互，空桶/满桶可用两个物品或 NBT 区分。
 */
public class RiceBucketItem extends Item {
    public RiceBucketItem(Properties properties) {
        super(properties);
    }

    // TODO: 流体确定后在此接入放置/盛装逻辑（见类注释方式一/二）。
}
