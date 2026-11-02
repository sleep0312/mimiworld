package com.mimiworld.mimiworld.network;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 本模组的网络通道。目前仅承载「猩红竖眼」的按键请求：
 * 客户端按键 → 服务端执行传送校验与位移。
 */
public final class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MimiWorld.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private ModNetwork() {
    }

    /** 注册全部消息，需在模组构造期间调用。 */
    public static void register() {
        INSTANCE.messageBuilder(CrimsonEyeTeleportPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CrimsonEyeTeleportPacket::encode)
                .decoder(CrimsonEyeTeleportPacket::new)
                .consumerMainThread(CrimsonEyeTeleportPacket::handle)
                .add();
    }
}
