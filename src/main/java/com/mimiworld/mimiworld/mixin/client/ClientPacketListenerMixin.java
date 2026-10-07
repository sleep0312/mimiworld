package com.mimiworld.mimiworld.mixin.client;

import com.mimiworld.mimiworld.ModItems;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 米米图腾的客户端第一人称激活动画。
 *
 * <p>实体事件 35 触发时（粒子与音效对任何物品都通用，无需改动），客户端在
 * {@code ClientPacketListener#handleEntityEvent} 中调用私有静态方法 findTotem
 * 决定举到屏幕前展示的物品，该方法只认原版不死图腾、找不到还会兜底返回一个原版图腾。
 * 这里重定向该调用：本地玩家手中持有米米图腾时返回米米图腾，其余情况与原版兜底一致。
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Redirect(
            method = "handleEntityEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;findTotem(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack mimiworld$showMimiTotem(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.is(ModItems.MIMI_TOTEM.get())) {
                return held;
            }
        }
        return new ItemStack(Items.TOTEM_OF_UNDYING);
    }
}
