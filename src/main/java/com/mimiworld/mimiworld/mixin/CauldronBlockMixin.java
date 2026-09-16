package com.mimiworld.mimiworld.mixin;

import com.mimiworld.mimiworld.ModDimensions;
import com.mimiworld.mimiworld.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 米米维度「雨即米汤」的接锅逻辑。
 *
 * <p>原版链路：{@code ServerLevel.advanceWeatherCycle} 每 section tick 以 1/16 概率取
 * {@code biome.getPrecipitationAt(pos)}，非 NONE 时调 {@code Block#handlePrecipitation}。
 * {@link CauldronBlock#handlePrecipitation} 对空锅在 RAIN 时以 5% 概率硬编码变成水锅。
 * 1.20.1 Forge 没有任何降水相关事件钩子，只能在 HEAD 处注入：米米维度下雨时空锅接成米汤锅
 * （液位 1，与原版水锅首接一致），并取消原逻辑使其绝不会变成水锅。
 *
 * <p>非米米维度完全不介入，原版行为不变。
 */
@Mixin(CauldronBlock.class)
public abstract class CauldronBlockMixin {

    @Inject(method = "handlePrecipitation", at = @At("HEAD"), cancellable = true)
    private void mimiworld$fillRiceSoup(BlockState state, Level level, BlockPos pos,
                                        Biome.Precipitation precipitation, CallbackInfo ci) {
        if (precipitation == Biome.Precipitation.RAIN
                && !level.isClientSide
                && level.dimension().equals(ModDimensions.MIMI_DIMENSION)
                && level.getRandom().nextFloat() < 0.05F) {
            level.setBlockAndUpdate(pos, ModFluids.RICE_SOUP_CAULDRON.get().defaultBlockState()
                    .setValue(LayeredCauldronBlock.LEVEL, 1));
            level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            ci.cancel();
        }
    }
}
