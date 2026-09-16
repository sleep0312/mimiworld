package com.mimiworld.mimiworld.fluid;

import com.mimiworld.mimiworld.ModDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;

/**
 * 米汤流体的公共实现。流体类型 {@link net.minecraftforge.fluids.FluidType} 上的静态粘度为 6000
 * （非米米世界），而米米世界维度内粘度要求为 1000。1.20.1 没有「按维度变粘度」的现成开关，
 * 粘度在游戏行为上唯一的直接体现就是流体随机刻间隔（原版换算 tickDelay = viscosity / 200：
 * 水 1000→5t，熔岩 6000→30t），因此在此按所在维度重写 {@link #getTickDelay(LevelReader)}：
 * <ul>
 *     <li>米米世界：1000 → 5t（与水同速）</li>
 *     <li>其他维度：6000 → 30t（与熔岩同速）</li>
 * </ul>
 * 其余规则（流动距离、每格衰减等）全部沿用 {@link ForgeFlowingFluid} 的水类默认值。
 */
public abstract class RiceSoupFluid extends ForgeFlowingFluid {
    // 米米世界内：粘度 1000，每 5 tick 流动一次（1000 / 200，同原版水）
    public static final int TICK_DELAY_MIMI_DIMENSION = 1000 / 200;
    // 其他维度：粘度 6000，每 30 tick 流动一次（6000 / 200，同原版熔岩）
    public static final int TICK_DELAY_OTHER = 6000 / 200;

    protected RiceSoupFluid(Properties properties) {
        super(properties);
    }

    @Override
    public int getTickDelay(LevelReader level) {
        // LevelReader 只暴露 dimensionType()，拿不到维度 ResourceKey；实际调用点传入的都是
        // Level/ClientLevel/ServerLevel，用 instanceof 取 Level.dimension() 判定。
        if (level instanceof Level realLevel
                && realLevel.dimension().equals(ModDimensions.MIMI_DIMENSION)) {
            return TICK_DELAY_MIMI_DIMENSION;
        }
        return TICK_DELAY_OTHER;
    }

    /** 米汤源流体（静止）。 */
    public static class Source extends RiceSoupFluid {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    /** 米汤流动流体（带 LEVEL 0~7 状态）。 */
    public static class Flowing extends RiceSoupFluid {
        public Flowing(Properties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
        }

        @Override
        protected void createFluidStateDefinition(
                net.minecraft.world.level.block.state.StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}
