package com.mimiworld.mimiworld.item;

import net.minecraft.world.item.Item;

/**
 * 米米图腾（Mimi Totem）：与原版不死图腾行为一致的免死道具。
 *
 * <p>本类本身只是与原版 {@code TotemOfUndyingItem} 对应的空壳子类；
 * 免死触发逻辑原版硬编码了 {@link net.minecraft.world.item.Items#TOTEM_OF_UNDYING}，
 * 因此由 {@code LivingEntityMixin} 在 {@code checkTotemDeathProtection} 中复刻：
 * 手持（主手/副手）米米图腾受到致命伤害时，恢复 1 点生命并清除原有效果，
 * 附加生命恢复 II 45s、伤害吸收 II 5s、抗火 I 40s，同时播放图腾粒子/音效/第一人称激活动画。
 * 默认堆叠数 64，与原版不死图腾一致。
 */
public class MimiTotemItem extends Item {
    public MimiTotemItem(Properties properties) {
        super(properties);
    }
}
