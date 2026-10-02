package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.block.entity.RicePoundingTableBlockEntity;
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
 * 锤米台（rice_pounding_table）：绑定 RicePoundingTableBlockEntity。
 * 外观仍是普通全立方块（模型/贴图/挖掘标签沿用原方块资源），右键打开锤米 GUI：
 * 左列两个输入槽 + 右上输出槽（工作台式即时合成，当前无配方）。
 */
public class RicePoundingTableBlock extends BaseEntityBlock {

    public RicePoundingTableBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RicePoundingTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // 输出预览刷新只在服务端跑；客户端不需要 ticker
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.RICE_POUNDING_TABLE.get(),
                RicePoundingTableBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof RicePoundingTableBlockEntity blockEntity) {
            NetworkHooks.openScreen(serverPlayer, blockEntity, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof RicePoundingTableBlockEntity blockEntity) {
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
