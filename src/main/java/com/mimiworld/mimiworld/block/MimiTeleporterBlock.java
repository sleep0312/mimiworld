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

    // ==================== 激活序列时间轴（游戏刻，供服务端生成与客户端动画共用） ====================
    /** 第一段：全部插眼后，眼睛转速用 3 秒（60 刻）平滑提升到原速的 3 倍。 */
    public static final int SEQ_WARMUP = 60;
    /** 第二段：从 60 刻起每 1 秒（20 刻）向泥米位置发射一束激光，共 {@link #SEQ_SHOTS} 束。 */
    public static final int SEQ_INTERVAL = 20;
    /** 激光总束数（= 框架数 12）。 */
    public static final int SEQ_SHOTS = 12;
    /** 生成时刻：最后一束（第 12 束，280 刻）发射后隔 1 秒生成传送门与泥米（300 刻）。 */
    public static final int SEQ_FORM_AT = SEQ_WARMUP + SEQ_INTERVAL * SEQ_SHOTS;
    /** 第三段：生成瞬间起用 3 秒（60 刻）把眼睛转速平滑降回原速，激光同时瞬间消失。 */
    public static final int SEQ_SPIN_DOWN = 60;

    /**
     * 12 个框架方块相对中心的水平偏移 (dx, dz)。
     * 下标即激活动画中该框架发射激光的先后顺序：从上边（北侧）起顺时针绕环一周
     * —— 上边左→右、右边上→下、下边右→左、左边下→上（俯视 +x 向右、+z 向下）。
     */
    public static final int[][] RING_OFFSETS = {
            {-2, -3}, {0, -3}, {2, -3},   // 上边 z=-3：左→右
            {3, -2}, {3, 0}, {3, 2},      // 右边 x=3：上→下
            {2, 3}, {0, 3}, {-2, 3},      // 下边 z=3：右→左
            {-3, 2}, {-3, 0}, {-3, -2},   // 左边 x=-3：下→上
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
            clearSequence(level, pos);
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
     * 以 clicked 为插眼中任意一个框架方块，反推中心并校验完整图案。
     *
     * <p>全部 12 个传送器均为已插眼时的三段式激活序列（不再立即生成）：</p>
     * <ol>
     *   <li>0–60 刻：眼睛转速平滑升至 3 倍；</li>
     *   <li>60 刻起每 20 刻发射一束激光（共 12 束，280 刻发射完毕）；</li>
     *   <li>300 刻：生成 2 格传送门 + 泥米盖，激光瞬间消失、转速 60 刻内降回原速
     *       （时间轴常量见 {@link #SEQ_WARMUP} 等，动画由客户端渲染器播放）。</li>
     * </ol>
     *
     * <p>序列进度记录在 12 个框架方块实体上（{@code seqStart}/{@code seqCenter}）并同步客户端。
     * 框架在序列中被打断时由 {@link #clearSequence} 清除；区块重载后按存档的游戏刻续播。</p>
     *
     * @return 本次是否推进了激活流程（含“序列进行中/中心占用等待”）
     */
    public static boolean tryActivate(Level level, BlockPos clicked) {
        if (level.isClientSide) {
            return false;
        }
        long now = level.getGameTime();
        for (int[] offset : RING_OFFSETS) {
            BlockPos center = clicked.offset(-offset[0], 0, -offset[1]);
            if (level.getBlockState(center).is(ModBlocks.MIMI_PORTAL.get())) {
                continue; // 该中心已成型
            }
            if (!isPatternComplete(level, center)) {
                continue;
            }
            if (!(level.getBlockEntity(center.offset(RING_OFFSETS[0][0], 0, RING_OFFSETS[0][1]))
                    instanceof MimiTeleporterBlockEntity anchor)) {
                continue;
            }
            if (anchor.seqStart == MimiTeleporterBlockEntity.SEQ_NONE) {
                // 第 12 只眼落位、序列尚未开始：启动激活动画
                startSequence(level, center, now);
                return true;
            }
            if (now - anchor.seqStart >= SEQ_FORM_AT && formPortal(level, center)) {
                return true;
            }
            // 序列进行中（或中心暂被占用）：交由方块实体 ticker 逐 tick 继续推进
            return true;
        }
        return false;
    }

    /** 启动激活动画：把序列起点刻与中心写入 12 个框架方块实体并同步客户端。 */
    private static void startSequence(Level level, BlockPos center, long start) {
        for (int[] offset : RING_OFFSETS) {
            BlockPos framePos = center.offset(offset[0], 0, offset[1]);
            if (level.getBlockEntity(framePos) instanceof MimiTeleporterBlockEntity tele) {
                syncSequence(level, framePos, tele, start, center);
            }
        }
    }

    /**
     * 清除以 framePos 为环成员的所有已启动序列（框架被破坏导致图案中断时调用）：
     * 反推候选中心，凡该框架属于其序列环的，重置 12 个框架上的序列状态并同步客户端。
     */
    private static void clearSequence(Level level, BlockPos framePos) {
        for (int[] offset : RING_OFFSETS) {
            BlockPos center = framePos.offset(-offset[0], 0, -offset[1]);
            boolean active = false;
            for (int[] ring : RING_OFFSETS) {
                if (level.getBlockEntity(center.offset(ring[0], 0, ring[1]))
                        instanceof MimiTeleporterBlockEntity tele
                        && tele.seqStart != MimiTeleporterBlockEntity.SEQ_NONE) {
                    active = true;
                    break;
                }
            }
            if (!active) {
                continue;
            }
            for (int[] ring : RING_OFFSETS) {
                BlockPos frame = center.offset(ring[0], 0, ring[1]);
                if (level.getBlockEntity(frame) instanceof MimiTeleporterBlockEntity tele) {
                    syncSequence(level, frame, tele, MimiTeleporterBlockEntity.SEQ_NONE, null);
                }
            }
        }
    }

    /** 写入并同步单个框架方块实体的序列状态（setChanged + sendBlockUpdated → 客户端收更新包）。 */
    private static void syncSequence(Level level, BlockPos pos, MimiTeleporterBlockEntity tele,
                                     long start, @Nullable BlockPos center) {
        tele.seqStart = start;
        tele.seqCenter = center;
        tele.setChanged();
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 3);
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
        // 三格生成处播放方块碎裂粒子（2001 = 原版破坏效果），连发两次 = 普通碎裂的 2 倍数量
        for (BlockPos pos : new BlockPos[]{center, second, third}) {
            int blockId = Block.getId(level.getBlockState(pos));
            level.levelEvent(2001, pos, blockId);
            level.levelEvent(2001, pos, blockId);
        }
        return true;
    }
}
