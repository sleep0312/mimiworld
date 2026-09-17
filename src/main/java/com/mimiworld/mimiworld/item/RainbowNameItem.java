package com.mimiworld.mimiworld.item;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 163 号「超懒神剑·残缺·壹」专用：普通物品（非装备），但显示名称为动态彩色渐变。
 *
 * <p>原理：每次渲染物品名都会调用 {@link #getName}，按系统时间计算基础色相并逐字错开，
 * 每个字符单独 withColor，从而形成持续流动的彩虹渐变。服务端调用时同样安全（仅影响显示文本）。</p>
 */
public class RainbowNameItem extends Item {

    /** 一个完整色相循环的毫秒数 */
    private static final long CYCLE_MS = 3000L;
    /** 相邻字符的色相差，形成横向渐变 */
    private static final float CHAR_HUE_STEP = 0.07F;

    public RainbowNameItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        // 先按当前语言解析出最终名称文本（与普通翻译名表现一致），再逐字上色
        String text = Component.translatable(getDescriptionId(stack)).getString();
        float baseHue = (Util.getMillis() % CYCLE_MS) / (float) CYCLE_MS;
        MutableComponent result = Component.empty();
        for (int i = 0; i < text.length(); i++) {
            float hue = baseHue + i * CHAR_HUE_STEP;
            hue -= (float) Math.floor(hue); // 取小数部分，限制在 [0,1)
            int rgb = hsbToRgb(hue, 0.75F, 1.0F) | 0xFF000000;
            result.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(style -> style.withColor(rgb)));
        }
        return result;
    }

    /** 不依赖 java.awt 的 HSB→RGB 转换，返回 0xRRGGBB（无 alpha，由调用方补 0xFF） */
    private static int hsbToRgb(float hue, float saturation, float brightness) {
        int h = (int) (hue * 6.0F);
        float f = hue * 6.0F - h;
        float p = brightness * (1.0F - saturation);
        float q = brightness * (1.0F - f * saturation);
        float t = brightness * (1.0F - (1.0F - f) * saturation);
        float r;
        float g;
        float b;
        switch (h % 6) {
            case 0 -> { r = brightness; g = t; b = p; }
            case 1 -> { r = q; g = brightness; b = p; }
            case 2 -> { r = p; g = brightness; b = t; }
            case 3 -> { r = p; g = q; b = brightness; }
            case 4 -> { r = t; g = p; b = brightness; }
            default -> { r = brightness; g = p; b = q; }
        }
        return ((int) (r * 255.0F) << 16) | ((int) (g * 255.0F) << 8) | (int) (b * 255.0F);
    }
}
