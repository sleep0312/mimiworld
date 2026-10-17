package com.mimiworld.mimiworld.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * 弓的客户端渲染共用工具：判断「谁正在拉这把弓」以及「他会射出哪支箭」。
 *
 * <p>判定思路与原版拉弓贴图（bow_pulling_*）一致——正在被渲染的这个 ItemStack 与某个玩家的
 * {@code getUseItem()} 是同一个对象引用。玩家的使用状态由原版实体数据同步，
 * 因此对本地玩家、以及别的玩家看你的弓都成立。</p>
 */
public final class BowArrowHelper {

    private BowArrowHelper() {}

    /**
     * 找出正在拉这把弓的玩家。拿不到持有者（物品栏图标、地上掉落物、展示框等）时返回 null。
     *
     * @param rendered 当前正在渲染的弓物品堆（{@code GeoItemRenderer#getCurrentItemStack()}）
     */
    @Nullable
    public static LivingEntity findHolder(@Nullable ItemStack rendered) {
        if (rendered == null || rendered.isEmpty()) {
            return null;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            for (Player player : level.players()) {
                if (player.isUsingItem() && player.getUseItem() == rendered) {
                    return player;
                }
            }
        }
        LocalPlayer self = Minecraft.getInstance().player;
        if (self != null && self.isUsingItem() && self.getUseItem() == rendered) {
            return self;
        }
        return null;
    }

    /**
     * 持有者拉弓时会射出的那支箭——即背包/副手里实际的那一摞箭（创造模式下原版会给出普通箭），
     * 因此 8 种箭各有自己的贴图。没在拉弓或没有箭时返回 {@link ItemStack#EMPTY}。
     */
    public static ItemStack getNockedArrow(@Nullable ItemStack rendered) {
        LivingEntity holder = findHolder(rendered);
        return holder == null ? ItemStack.EMPTY : holder.getProjectile(rendered);
    }
}
