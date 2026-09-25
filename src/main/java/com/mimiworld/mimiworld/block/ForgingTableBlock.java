package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.ForgingTableBlockEntity;
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
 * 装备锻造台机器方块基类（铸剑台 / 铸弓台 / 铸甲台共用）。
 * <p>
 * 外观为普通全立方块（模型/贴图/挖掘标签沿用方块自身资源），右键打开与原版工作台
 * 外观一致的 3x3 即时合成 GUI；各子类绑定独立的方块实体与独立配方表（配方互不通用）。
 * 服务端每 tick 刷新合成结果预览，破坏时掉落网格内物品（结果预览自动丢弃）。
 */
public abstract class ForgingTableBlock extends BaseEntityBlock {

    protected ForgingTableBlock(Properties properties) {
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
        // 运行时该类型必然绑定 ForgingTableBlockEntity 子类，强转安全
        return createTickerHelper(type, blockEntityType(), ForgingTableBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof ForgingTableBlockEntity blockEntity) {
            NetworkHooks.openScreen(serverPlayer, blockEntity, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                         boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof ForgingTableBlockEntity blockEntity) {
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
