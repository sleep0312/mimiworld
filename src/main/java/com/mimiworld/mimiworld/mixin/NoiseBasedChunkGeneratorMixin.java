package com.mimiworld.mimiworld.mixin;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.fluid.ModFluids;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 米米维度（噪声设置 mimiworld:mimi_overworld）把原版深层岩浆海替换成饭海。
 *
 * <p>原版 {@link NoiseBasedChunkGenerator#createFluidPicker(NoiseGeneratorSettings)} 硬编码了一个
 * {@code FluidStatus(-54, LAVA)}：当 y &lt; min(-54, sea_level) 时流体选择器无条件返回熔岩，
 * 与 {@code default_fluid} 无关，因此仅靠数据包无法改掉深层熔岩。这里对米米维度返回与原版同构的
 * 分层流体选择器：y &lt; -54 用饭汤（rice_broth）构成「饭海」，其上是海平面高度的默认流体（米汤）；
 * 其他维度（主世界等）保持原版行为。</p>
 *
 * <p>岩浆噪声（noise_router.lava）已在 mimi_overworld.json 中改为常量 0，
 * 同时 mimi_plains 的洞穴雕刻器改用 mimiworld 命名空间下 lava_level 为 -1000 的版本，
 * 从「含水层」「岩浆噪声」「洞穴雕刻」三条路径共同保证米米维度没有熔岩。</p>
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {

    /** 原版深层岩浆海的高度（y &lt; -54 为熔岩层），饭海沿用同一深度。 */
    private static final int MIMIWORLD_RICE_SEA_LEVEL = -54;

    @Inject(method = "createFluidPicker", at = @At("HEAD"), cancellable = true)
    private static void mimiworld$replaceLavaAquiferWithRiceFluids(NoiseGeneratorSettings settings,
                                                                   CallbackInfoReturnable<Aquifer.FluidPicker> cir) {
        // 只处理米米维度：该维度噪声设置的默认方块是硬化米
        if (!settings.defaultBlock().is(ModBlocks.HARDENED_RICE.get())) {
            return;
        }
        BlockState riceBroth = ModFluids.RICE_BROTH_BLOCK.get().defaultBlockState();
        Aquifer.FluidStatus riceSeaStatus = new Aquifer.FluidStatus(MIMIWORLD_RICE_SEA_LEVEL, riceBroth);
        Aquifer.FluidStatus riceSoupStatus = new Aquifer.FluidStatus(settings.seaLevel(), settings.defaultFluid());
        cir.setReturnValue((x, y, z) -> y < Math.min(MIMIWORLD_RICE_SEA_LEVEL, settings.seaLevel())
                ? riceSeaStatus : riceSoupStatus);
    }
}
