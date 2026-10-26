package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.ModItems;
import com.mimiworld.mimiworld.block.entity.MimiTeleporterBlockEntity;
import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 米米传送器（mimi_teleporter）：米米维度传送门的框架方块。
 *
 * <p>激活方式（7×7 网格，框架方块共 12 个，见摆放示意图）：</p>
 * <ol>
 *   <li>按图案摆放 12 个米米传送器（相对中心的偏移见 {@link #RING_OFFSETS}）；</li>
 *   <li>手持米米之眼右键任意一个传送器：消耗 1 个米米之眼，该传送器永久标记为已插眼
 *       （{@link #EYE} 状态），一个传送器只能插一次；</li>
 *   <li>已插眼的传送器会在其正上方 1 格渲染一只悬浮且缓慢旋转的米米之眼
 *       （客户端 BER，见 MimiTeleporterBlockRenderer）；</li>
 *   <li>12 个全部插眼后，在中心依次生成 2 格高米米传送门方块（mimi_portal）
 *       与顶上的泥米（mud_rice），位置见摆放示意图第二/三层。</li>
 * </ol>
 *
 * <p>若最后一次插眼时中心被占用，服务端方块实体 ticker 会每秒重试一次，
 * 待中心腾空后自动补生成，无需重新插眼。</p>
 */
public class MimiTeleporterBlock extends Block implements EntityBlock {

    /** 是否已插眼（米米之眼）：插上后永久为 true，仅消耗式插眼一次。 */
    public static final BooleanProperty EYE = BooleanProperty.create("eye");

    /**
     * 12 个框架方块相对中心的水平偏移 (dx, dz)，与摆放示意图第一层一一对应：
     * 上下边各 3 个（距中心 3 格、横向 -2/0/+2），左右边各 3 个（距中心 3 格、纵向 -2/0/+2）。
     */
    private static final int[][] RING_OFFSETS = {
            {-2, -3}, {0, -3}, {2, -3},
            {-3, -2}, {3, -2},
            {-3, 0}, {3, 0},
            {-3, 2}, {3, 2},
            {-2, 3}, {0, 3}, {2, 3},
    };

    public MimiTeleporterBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(EYE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EYE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MimiTeleporterBlockEntity(pos, state);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.MIMI_TELEPORTER.get()) {
            return null;
        }
        // 运行时该类型必然绑定 MimiTeleporterBlockEntity，强转安全
        BlockEntityTicker<MimiTeleporterBlockEntity> ticker = MimiTeleporterBlockEntity::serverTick;
        return (BlockEntityTicker<T>) ticker;
    }

    /**
     * 框架方块被破坏（挖掘、爆炸、活塞推动等）时，其对应传送门的中心传送门方块
     * 与顶上的泥米盖随之即刻碎掉——地狱门式联动（客户端不主动改，等服务端同步）。
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                         boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            breakPortalFor(level, pos);
        }
    }

    /**
     * 以被破坏的框架方块反推传送门中心（同激活时的反推逻辑），
     * 中心确有传送门则整柱碎掉：2 格传送门方块 + 顶上的泥米盖。
     * 不属于任何已激活传送门环的框架被破坏时不做任何事。
     */
    private static void breakPortalFor(Level level, BlockPos framePos) {
        for (int[] offset : RING_OFFSETS) {
            BlockPos center = framePos.offset(-offset[0], 0, -offset[1]);
            if (!level.getBlockState(center).is(ModBlocks.MIMI_PORTAL.get())) {
                continue;
            }
            level.removeBlock(center, false);
            BlockPos second = center.above();
            if (level.getBlockState(second).is(ModBlocks.MIMI_PORTAL.get())) {
                level.removeBlock(second, false);
            }
            BlockPos cap = center.above(2);
            if (level.getBlockState(cap).is(ModBlocks.MUD_RICE.get())) {
                level.removeBlock(cap, false);
            }
        }
    }

    /**
     * 手持米米之眼右键：每个传送器仅可插一次，插上即消耗一个米米之眼（创造模式不消耗），
     * 插完后立即尝试激活；若中心被占用则由 ticker 稍后重试。
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.MIMI_EYE.get())) {
            return InteractionResult.PASS;
        }
        if (state.getValue(EYE)) {
            // 已插过眼：不消耗、不重复，仅摆手
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.setBlock(pos, state.setValue(EYE, true), 3);
            tryActivate(level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * 以 clicked 为插眼中任意一个框架方块，反推中心并校验完整图案，
     * 全部 12 个传送器均为已插眼状态时生成传送门。
     *
     * @return 本次是否生成了传送门
     */
    public static boolean tryActivate(Level level, BlockPos clicked) {
        if (level.isClientSide) {
            return false;
        }
        for (int[] offset : RING_OFFSETS) {
            BlockPos center = clicked.offset(-offset[0], 0, -offset[1]);
            if (isPatternComplete(level, center) && formPortal(level, center)) {
                return true;
            }
        }
        return false;
    }

    /** 中心 12 个框架位置是否全部为「米米传送器 + 已插眼」。 */
    private static boolean isPatternComplete(Level level, BlockPos center) {
        for (int[] offset : RING_OFFSETS) {
            BlockState state = level.getBlockState(center.offset(offset[0], 0, offset[1]));
            if (!state.is(ModBlocks.MIMI_TELEPORTER.get()) || !state.getValue(EYE)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 在中心生成传送门：中心柱自下而上 2 格米米传送门方块 + 1 格泥米（示意图第二/三层）。
     * 中心三格必须均可被替换（空气等），避免破坏已有建筑；不满足时不做任何改动。
     *
     * @return 是否已生成
     */
    private static boolean formPortal(Level level, BlockPos center) {
        BlockPos second = center.above();
        BlockPos third = center.above(2);
        if (!level.getBlockState(center).canBeReplaced()
                || !level.getBlockState(second).canBeReplaced()
                || !level.getBlockState(third).canBeReplaced()) {
            return false;
        }
        BlockState portal = ModBlocks.MIMI_PORTAL.get().defaultBlockState();
        level.setBlock(center, portal, 3);
        level.setBlock(second, portal, 3);
        level.setBlock(third, ModBlocks.MUD_RICE.get().defaultBlockState(), 3);
        return true;
    }
}
