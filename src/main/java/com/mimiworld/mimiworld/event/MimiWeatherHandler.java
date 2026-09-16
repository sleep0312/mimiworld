package com.mimiworld.mimiworld.event;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 米米维度天气规则：只有晴天和雨天，永不雷暴。
 *
 * <p>昼夜与晴雨周期由 dimension_type（照抄主世界）和原版天气循环驱动，这里只压制雷暴：
 * 原版 {@code ServerLevel.advanceWeatherCycle} 在 thunderTime 倒数到 0 时翻转 isThundering。
 * 因此在每 tick 末尾把米米维度的雷暴剩余时间钳在正数，翻转永远不会发生；
 * 万一其他来源已经置为雷暴（如 /weather thunder 指令），立即清除并重置计时。
 * 雨天（下的是米汤雨）完全保留。
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MimiWeatherHandler {

    /** 雷暴计时低于该值就续上，保证 advanceWeatherCycle 永远走不到翻转分支。 */
    private static final int THUNDER_TIME_FLOOR = 100;
    private static final int THUNDER_TIME_RESET = 12000;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!serverLevel.dimension().equals(ModDimensions.MIMI_DIMENSION)) {
            return;
        }
        if (!(serverLevel.getLevelData() instanceof ServerLevelData levelData)) {
            return;
        }
        if (levelData.isThundering()) {
            levelData.setThundering(false);
            levelData.setThunderTime(THUNDER_TIME_RESET);
        } else if (levelData.getThunderTime() <= THUNDER_TIME_FLOOR) {
            levelData.setThunderTime(THUNDER_TIME_RESET);
        }
    }
}
