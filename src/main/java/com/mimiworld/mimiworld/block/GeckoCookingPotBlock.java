package com.mimiworld.mimiworld.block;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 使用 GeckoLib geo 模型渲染的网格煮锅基类。
 *
 * <p>仅煮锅/大煮锅/超大煮锅/巨无霸大大大煮锅继承本类；
 * 烤箱、铁锅继续使用 {@link GridCookingPotBlock}（普通 MODEL 渲染），不受影响。</p>
 *
 * <p>碰撞箱/选框由各子类按渲染后的模型高度生成（{@link CookingPotShapes#forRenderedHeight}），
 * 与堕落十字架一样贴合模型；世界中的外观完全由 GeckoLib BlockEntityRenderer 渲染。</p>
 */
public abstract class GeckoCookingPotBlock extends GridCookingPotBlock {

    protected GeckoCookingPotBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // 世界中的外观完全由 GeckoLib BlockEntityRenderer 渲染，
        // 不渲染方块自身的 JSON 模型（物品栏图标仍走 models/item，不受影响）
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
