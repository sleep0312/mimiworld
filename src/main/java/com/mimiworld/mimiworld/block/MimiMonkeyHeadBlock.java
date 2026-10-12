package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.MimiMonkeyHeadBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 米米猴头（mimi_monkey_head）：米米猴王掉落的头颅摆件方块。
 *
 * <p>世界中的外观完全由 GeckoLib BlockEntityRenderer 渲染
 * （geo/block/mimi_monkey_head.geo.json：猴头+金冠+双雉鸡翎，BER 内缩放摆正后
 * 约 1 格宽、0.56 格高，贴地居中放置），方块自身 JSON 模型不渲染
 * （{@link RenderShape#ENTITYBLOCK_ANIMATED}），仅提供破坏粒子纹理；
 * 物品栏图标仍走 models/item 的 2D 图标，不受影响。</p>
 *
 * <p>无 GUI、无动画、空手可破坏（硬度 1.0），破坏掉落本身
 * （loot_tables/blocks/mimi_monkey_head.json）。</p>
 */
public class MimiMonkeyHeadBlock extends Block implements EntityBlock {

    /** 与 BER 缩放后的可见模型占地一致：宽整格、高约半格、前后居中的扁座。 */
    private static final VoxelShape SHAPE = Shapes.box(0.0, 0.0, 0.3125, 1.0, 0.625, 0.6875);

    public MimiMonkeyHeadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MimiMonkeyHeadBlockEntity(pos, state);
    }
}
