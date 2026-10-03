package com.mimiworld.mimiworld.tags;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * 米米世界自定义方块标签。
 */
public final class ModBlockTags {

    private ModBlockTags() {
    }

    /**
     * 炉灶（加热源）：放置在煮锅正下方时，锅无需燃料即可持续烹煮，
     * GUI 燃料火焰条保持满格。
     *
     * <p>炉灶方块当前尚未注册；将来注册炉灶时，只需把它加入本标签
     * （data/mimiworld/tags/blocks/stoves.json 或 datagen），
     * 煮锅侧无需任何改动即可识别。</p>
     */
    public static final TagKey<Block> STOVES =
            TagKey.create(Registries.BLOCK, new ResourceLocation(MimiWorld.MODID, "stoves"));
}
