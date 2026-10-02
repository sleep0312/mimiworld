package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.block.entity.RicePressBlockEntity;
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
 * 压饭机（rice_press）：绑定 RicePressBlockEntity。
 * 外观仍是普通全立方块（模型/贴图/挖掘标签沿用原方块资源），右键打开压饭 GUI：
 * 上方横排 9 个输入槽 + 下方居中 1 个输出槽（工作台式即时合成，当前无配方）。
 */
public class RicePressBlock extends BaseEntityBlock {

    public RicePressBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RicePressBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // 输出预览刷新只在服务端跑；客户端不需要 ticker
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.RICE_PRESS.get(),
                RicePressBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof RicePressBlockEntity blockEntity) {
            NetworkHooks.openScreen(serverPlayer, blockEntity, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof RicePressBlockEntity blockEntity) {
                blockEntity.dropContents(level, pos);
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
