package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.block.MimiTeleporterBlock;
import com.mimiworld.mimiworld.client.renderer.MimiLaserRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import org.jetbrains.annotations.Nullable;

/**
 * 米米传送器（mimi_teleporter）方块实体。
 *
 * <p>承载两部分状态：</p>
 * <ul>
 *   <li>是否已插眼：存在方块状态里（{@link MimiTeleporterBlock#EYE}），随方块状态自动同步与存档；</li>
 *   <li>激活序列进度（{@link #seqStart} / {@link #seqCenter}）：第 12 只眼插上后启动，
 *       随本方块实体同步给客户端，客户端渲染器据此播放「加速→逐眼发射激光→减速」动画
 *       （MimiTeleporterBlockRenderer）。存档，区块卸载重载后按游戏刻续播。</li>
 * </ul>
 *
 * <p>服务端 ticker：序列进行中逐 tick 调用 {@link MimiTeleporterBlock#tryActivate}
 * 以精确到刻地生成传送门；空闲时每秒检查一次图案（插眼已完成但中心被占用时自动补生成）。</p>
 */
public class MimiTeleporterBlockEntity extends BlockEntity {

    /** 序列状态：未激活（无进行中的激活动画）。 */
    public static final long SEQ_NONE = Long.MIN_VALUE;

    /** 激活序列起始游戏刻（服务端与客户端共用）；{@link #SEQ_NONE} 表示无序列。存档 + 同步。 */
    public long seqStart = SEQ_NONE;

    /** 激活序列对应的传送门中心（激光汇聚目标）；seqStart 有效时非空。存档 + 同步。 */
    @Nullable
    public BlockPos seqCenter;

    /** 服务端空闲重试计数（无需存档，重新加载后最多延迟 1 秒）。 */
    private int tickCounter;

    public MimiTeleporterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIMI_TELEPORTER.get(), pos, state);
    }

    /** 激活序列是否进行中（含生成前的等待窗口）。 */
    public boolean isSequenceRunning() {
        return seqStart != SEQ_NONE;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MimiTeleporterBlockEntity be) {
        if (!state.getValue(MimiTeleporterBlock.EYE)) {
            return;
        }
        if (be.isSequenceRunning()) {
            // 序列进行中逐 tick 推进：生成时刻需精确到刻
            MimiTeleporterBlock.tryActivate(level, pos);
        } else if (++be.tickCounter >= 20) {
            be.tickCounter = 0;
            MimiTeleporterBlock.tryActivate(level, pos);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (seqStart != SEQ_NONE) {
            tag.putLong("mimi_seq_start", seqStart);
            if (seqCenter != null) {
                tag.putLong("mimi_seq_center", seqCenter.asLong());
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("mimi_seq_start")) {
            seqStart = tag.getLong("mimi_seq_start");
            seqCenter = tag.contains("mimi_seq_center")
                    ? BlockPos.of(tag.getLong("mimi_seq_center")) : null;
        } else {
            seqStart = SEQ_NONE;
            seqCenter = null;
        }
        // 客户端据此外推场景级激光渲染的活动序列登记（仅客户端执行；专用服务器不会加载该类）
        net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> MimiLaserRenderer.update(getBlockPos(), seqStart, seqCenter));
    }

    @Override
    public CompoundTag getUpdateTag() {
        // 客户端初次加载与运行时同步都用完整元数据（含 id），直接序列化自身
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        // 运行时同步：配合 Level#sendBlockUpdated 触发（ChunkHolder 广播 getUpdatePacket）
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
