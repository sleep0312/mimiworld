package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.GridCookingPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * NxN 网格煮锅机器方块基类（煮锅 2x2 / 大煮锅 3x3 / 超大煮锅 4x4 / 巨无霸大大大煮锅 5x5 共用）。
 * <p>
 * 外观仍为普通全立方块（模型/贴图/挖掘标签沿用方块自身资源），右键打开熔炉式加工 GUI：
 * 左上 NxN 输入网格、网格下方燃料槽（带火焰）、右上单独输入槽、右下输出槽；
 * 燃料烧炼、定时加工，节奏同原版熔炉（默认 200 tick）。破坏时掉落全部槽内物品。
 */
public abstract class GridCookingPotBlock extends BaseEntityBlock {

    protected GridCookingPotBlock(Properties properties) {
        super(properties);
    }

    /** 子类提供：创建对应的方块实体 */
    protected abstract BlockEntity createMachineBlockEntity(BlockPos pos, BlockState state);

    /** 子类提供：对应的方块实体类型（仅服务端 ticker 注册使用） */
    @SuppressWarnings("rawtypes")
    protected abstract BlockEntityType blockEntityType();

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return createMachineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, blockEntityType(), GridCookingPotBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof GridCookingPotBlockEntity blockEntity) {
            NetworkHooks.openScreen(serverPlayer, blockEntity, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                         boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof GridCookingPotBlockEntity blockEntity) {
            blockEntity.dropContents(level, pos);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
