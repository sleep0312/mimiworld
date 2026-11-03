package com.mimiworld.mimiworld.event;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.ModConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * 玩家栏位数量配置：进入世界（登录 / 重生）时按 config/mimiworld-common.toml 的 {@code [slots]} 调整各 Curios 栏位数。
 *
 * <p>-1 表示沿用数据包默认（不调整）；缩小栏位前会把超出槽位里的饰品退回玩家背包，避免物品丢失。</p>
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CurioSlotSizeEvents {

    private CurioSlotSizeEvents() {
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        apply(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        apply(event.getEntity());
    }

    private static void apply(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        CuriosApi.getCuriosInventory(player).resolve().ifPresent(handler -> handler.getCurios()
                .forEach((identifier, stacks) -> resize(player, stacks, ModConfig.slotSize(identifier))));
    }

    private static void resize(Player player, ICurioStacksHandler handler, int target) {
        if (target < 0) {
            return;
        }
        int current = handler.getSlots();
        if (current == target) {
            return;
        }
        if (current > target) {
            // 先把多余槽位里的饰品退回背包，再缩容
            for (int i = target; i < current; i++) {
                ItemStack stack = handler.getStacks().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(stack);
                    handler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
                }
            }
            handler.shrink(current - target);
        } else {
            handler.grow(target - current);
        }
        handler.update();
    }
}
