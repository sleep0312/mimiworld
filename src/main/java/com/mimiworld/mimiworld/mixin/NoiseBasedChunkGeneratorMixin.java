package com.mimiworld.mimiworld.mixin;

import com.mimiworld.mimiworld.block.ModBlocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 米米维度（噪声设置 mimiworld:mimi_overworld）禁止生成任何熔岩。
 *
 * <p>原版 {@link NoiseBasedChunkGenerator#createFluidPicker(NoiseGeneratorSettings)} 硬编码了一个
 * {@code FluidStatus(-54, LAVA)}：当 y &lt; min(-54, sea_level) 时流体选择器无条件返回熔岩，
 * 与 {@code default_fluid} 无关，因此仅靠数据包（把 default_fluid 改成米汤、关闭岩浆噪声）
 * 无法去掉深层熔岩。这里对米米维度直接返回「海平面 + 默认流体（米汤）」的全局流体状态，
 * 使深层含水层与浅层一样是米汤；其他维度（主世界等）保持原版行为。</p>
 *
 * <p>岩浆噪声（noise_router.lava）已在 mimi_overworld.json 中改为常量 0，
 * 同时 mimi_plains 的洞穴雕刻器改用 mimiworld 命名空间下 lava_level 为 -1000 的版本，
 * 从「含水层」「岩浆噪声」「洞穴雕刻」三条路径共同保证米米维度没有熔岩。</p>
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {

    @Inject(method = "createFluidPicker", at = @At("HEAD"), cancellable = true)
    private static void mimiworld$replaceLavaAquiferWithRiceSoup(NoiseGeneratorSettings settings,
                                                                 CallbackInfoReturnable<Aquifer.FluidPicker> cir) {
        // 只处理米米维度：该维度噪声设置的默认方块是硬化米
        if (!settings.defaultBlock().is(ModBlocks.HARDENED_RICE.get())) {
            return;
        }
        BlockState riceSoup = settings.defaultFluid();
        Aquifer.FluidStatus riceSoupStatus = new Aquifer.FluidStatus(settings.seaLevel(), riceSoup);
        cir.setReturnValue((x, y, z) -> riceSoupStatus);
    }
}
