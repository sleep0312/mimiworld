package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.IronWokBlockEntity;
import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 铁锅（iron_wok）：4 个输入槽呈菱形布置 + 右上单独输入 + 燃料 + 输出的熔炉式炒锅。
 * 燃料/进度逻辑与网格煮锅基类完全一致（4 输入即基类 2x2 的 4 格，仅 GUI 摆放为菱形）。
 * 外观由 GeckoLib 平底锅模型渲染（geo/block/iron_wok.geo.json），碰撞箱/选框贴合模型实际占位。
 * 放置时记录水平朝向：锅柄（模型 +Z）指向放置者。
 */
public class IronWokBlock extends GridCookingPotBlock {

    /** 平底锅模型占位（未旋转基准）：x 2~14px（锅体宽 12px），y 0~3px（底 1px + 锅沿 2px），z 0~16px（含锅柄）。 */
    private static final VoxelShape SHAPE_NS = Shapes.box(
            2.0 / 16.0, 0.0, 0.0, 14.0 / 16.0, 3.0 / 16.0, 1.0);
    /** 锅柄转到东西向时的占位（锅体 x/z 互换）。模型关于东西轴对称，南/北共用同一形状。 */
    private static final VoxelShape SHAPE_EW = Shapes.box(
            0.0, 0.0, 2.0 / 16.0, 1.0, 3.0 / 16.0, 14.0 / 16.0);

    /** 水平朝向：锅柄指向的方向（放置时 = 玩家朝向的反方向，即朝向放置者）。 */
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public IronWokBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // 基准模型锅柄朝南（+Z），选框/碰撞箱随 FACING 旋转，与渲染器旋转保持一致
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_EW;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // 世界中的外观完全由 GeckoLib BlockEntityRenderer 渲染（与煮锅系列一致），
        // 不渲染方块自身的 JSON 模型（物品栏图标仍走 models/item，不受影响）
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new IronWokBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType blockEntityType() {
        return ModBlockEntities.IRON_WOK.get();
    }
}
