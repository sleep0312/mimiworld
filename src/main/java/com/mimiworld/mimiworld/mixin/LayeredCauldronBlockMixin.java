package com.mimiworld.mimiworld.mixin;

import com.mimiworld.mimiworld.ModDimensions;
import com.mimiworld.mimiworld.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 米米维度雨即米汤的一致性补丁：玩家手动放过水的水锅在雨中续接的也必须是米汤，
 * 于是米米维度的雨永远不会让任何炼药锅增加水。命中概率与原版相同（RAIN 5%）。
 *
 * <p>米汤锅自身的液位循环由原版 {@code LayeredCauldronBlock.RAIN} 谓词处理，无需介入；
 * 非米米维度不介入。
 */
@Mixin(LayeredCauldronBlock.class)
public abstract class LayeredCauldronBlockMixin {

    @Inject(method = "handlePrecipitation", at = @At("HEAD"), cancellable = true)
    private void mimiworld$fillRiceSoup(BlockState state, Level level, BlockPos pos,
                                        Biome.Precipitation precipitation, CallbackInfo ci) {
        if (precipitation == Biome.Precipitation.RAIN
                && !level.isClientSide
                && level.dimension().equals(ModDimensions.MIMI_DIMENSION)
                && state.is(Blocks.WATER_CAULDRON)
                && level.getRandom().nextFloat() < 0.05F) {
            int nextLevel = Math.min(3, state.getValue(LayeredCauldronBlock.LEVEL) + 1);
            level.setBlockAndUpdate(pos, ModFluids.RICE_SOUP_CAULDRON.get().defaultBlockState()
                    .setValue(LayeredCauldronBlock.LEVEL, nextLevel));
            level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            ci.cancel();
        }
    }
}
