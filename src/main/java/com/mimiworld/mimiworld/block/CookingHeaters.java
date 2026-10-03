package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.tags.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

/**
 * 煮锅加热源（炉灶）检测工具。
 *
 * <p>当前以方块标签 {@link ModBlockTags#STOVES} 作为唯一判定依据：
 * 炉灶以后注册时加入该标签即可，煮锅与炉灶之间不产生代码耦合。</p>
 */
public final class CookingHeaters {

    private CookingHeaters() {
    }

    /** 锅位正下方是否为炉灶。 */
    public static boolean hasStoveBelow(LevelReader level, BlockPos potPos) {
        return level.getBlockState(potPos.below()).is(ModBlockTags.STOVES);
    }
}
