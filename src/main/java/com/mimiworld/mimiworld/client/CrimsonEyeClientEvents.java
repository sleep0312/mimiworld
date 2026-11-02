package com.mimiworld.mimiworld.client;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.network.CrimsonEyeTeleportPacket;
import com.mimiworld.mimiworld.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端按键监听：按下「自定义」键后发送空包到服务端，
 * 由服务端重新校验饰品与目标并执行传送。
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CrimsonEyeClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        if (CrimsonEyeKeybindings.TELEPORT_TO_GLOWING.consumeClick()) {
            ModNetwork.INSTANCE.sendToServer(new CrimsonEyeTeleportPacket());
        }
    }
}
