package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.block.MimiTeleporterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 米米传送器（mimi_teleporter）方块实体。
 *
 * <p>本身不保存数据（是否已插眼存在方块状态里，随方块状态自动同步与存档）：
 * 客户端作为悬浮米米之眼的渲染载体（MimiTeleporterBlockRenderer），
 * 服务端 ticker 每秒检查一次图案——已插眼但因中心被占用而未能生成传送门时，
 * 待中心腾空自动补生成（MimiTeleporterBlock#tryActivate）。</p>
 */
public class MimiTeleporterBlockEntity extends BlockEntity {

    /** 服务端重试计数（无需存档，重新加载后最多延迟 1 秒）。 */
    private int tickCounter;

    public MimiTeleporterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIMI_TELEPORTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MimiTeleporterBlockEntity be) {
        if (!state.getValue(MimiTeleporterBlock.EYE)) {
            return;
        }
        if (++be.tickCounter >= 20) {
            be.tickCounter = 0;
            MimiTeleporterBlock.tryActivate(level, pos);
        }
    }
}
