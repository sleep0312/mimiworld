package com.mimiworld.mimiworld.item;

import com.mimiworld.mimiworld.ModDimensions;
import com.mimiworld.mimiworld.world.MimiPortalTeleporter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 米米之眼（Eye of Mimi）：激活米米传送器用的插眼道具，同时是米米维度内的返程钥匙。
 *
 * <p>在米米维度手持右键：传送回主世界中进入米米维度时踩过的那座米米传送门旁
 * （入口坐标记在玩家持久化数据里，落点复用 {@link MimiPortalTeleporter#findReturnLanding}），
 * 入口传送门已被拆除或记录缺失时回退到主世界出生点地表并给出提示。</p>
 *
 * <p>只在米米维度生效：其他维度右键仍是普通物品行为（不消耗、不进冷却）。
 * 对着米米传送器右键时，方块自身的插眼逻辑优先，不会触发返程。</p>
 */
public class MimiEyeItem extends Item {

    public MimiEyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.dimension().equals(ModDimensions.MIMI_DIMENSION)) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack); // 客户端播放挥手动画，真正传送在服务端
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }
        ServerLevel overworld = serverPlayer.server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return InteractionResultHolder.fail(stack);
        }
        Vec3 landing = MimiPortalTeleporter.findReturnLanding(serverPlayer, overworld);
        boolean atPortal = landing != null;
        if (!atPortal) {
            landing = MimiPortalTeleporter.spawnLanding(overworld);
        }
        // 两端各播放一次传送音效，手感同原版末影人传送
        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        overworld.playSound(null, landing.x, landing.y, landing.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        serverPlayer.teleportTo(overworld, landing.x, landing.y, landing.z,
                serverPlayer.getYRot(), serverPlayer.getXRot());
        if (!atPortal) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.mimiworld.mimi_eye.no_portal"), true);
        }
        return InteractionResultHolder.success(stack);
    }
}
