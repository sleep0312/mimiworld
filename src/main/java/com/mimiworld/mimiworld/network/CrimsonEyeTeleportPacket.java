package com.mimiworld.mimiworld.network;

import com.mimiworld.mimiworld.event.CrimsonEyeEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 猩红竖眼按键请求（C2S）：无负载，收到后由服务端重新校验饰品与目标再执行传送，
 * 避免客户端伪造坐标。
 */
public class CrimsonEyeTeleportPacket {

    public CrimsonEyeTeleportPacket() {
    }

    public CrimsonEyeTeleportPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        ServerPlayer player = supplier.get().getSender();
        if (player != null) {
            CrimsonEyeEvents.teleportToGlowingMonster(player);
        }
        supplier.get().setPacketHandled(true);
    }
}
