package com.mimiworld.mimiworld.event;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.ModConfig;
import com.mimiworld.mimiworld.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Comparator;
import java.util.List;

/**
 * 猩红竖眼（crimson_vertical_eye）事件：21×21×21 范围内怪物发光，
 * 以及客户端按键请求触发的「传送到发光怪物身后」。
 */
@Mod.EventBusSubscriber(modid = MimiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CrimsonEyeEvents {

    /** 范围半径：21×21×21 立方体即每轴 ±10.5 格。 */
    private static final double RANGE = 10.5D;
    /** 每 1 秒刷新一次发光，效果持续 2 秒，脱下后自然衰减。 */
    private static final int GLOW_REFRESH_TICKS = 20;
    private static final int GLOW_DURATION_TICKS = 40;

    /** 佩戴期间周期性给范围内怪物施加发光效果。 */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        Player player = event.player;
        if (!isEquipped(player) || player.tickCount % GLOW_REFRESH_TICKS != 0) {
            return;
        }
        for (LivingEntity monster : findMonsters(player.level(), glowArea(player))) {
            monster.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING, GLOW_DURATION_TICKS, 0, false, false, false));
        }
    }

    /**
     * 「怪物」判定：原版与模组中继承 Monster 的生物都实现了 Enemy；
     * 少数自定义敌对生物只继承 PathfinderMob 并手动实现 Enemy（如八爪嘴龙、终极懒龙），
     * 另有一些生物虽未实现 Enemy 但实体类别登记为 MONSTER，一并按怪物处理。
     * 这样不会把玩家、友好动物（如米米羊、嗥米狼）算进来。
     */
    private static boolean isMonster(LivingEntity entity) {
        return entity.isAlive()
                && (entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER);
    }

    private static List<LivingEntity> findMonsters(Level level, AABB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, CrimsonEyeEvents::isMonster);
    }

    /** 玩家是否佩戴猩红竖眼（服务端权威判定）。 */
    private static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .map(handler -> handler.isEquipped(ModItems.CRIMSON_VERTICAL_EYE.get()))
                .orElse(false);
    }

    /** 以玩家为中心、边长 21 格的立方体范围。 */
    private static AABB glowArea(Player player) {
        return new AABB(
                player.getX() - RANGE, player.getY() - RANGE, player.getZ() - RANGE,
                player.getX() + RANGE, player.getY() + RANGE, player.getZ() + RANGE);
    }

    /**
     * 按键请求入口：挑选最近的发光怪物，逐只尝试传送到其身后。
     * 任一目标视线被方块挡住或落脚点被占用则跳过，全部失败则不移动（不可穿墙）。
     * 成功/失败原因可在配置 [messages] 的 crimson_eye_teleport_hint 开关下显示在快捷栏上方。
     */
    public static void teleportToGlowingMonster(ServerPlayer player) {
        if (player.isDeadOrDying() || player.isSpectator() || player.isPassenger()) {
            return;
        }
        if (!isEquipped(player)) {
            sendHint(player, "message.mimiworld.crimson_vertical_eye.not_equipped", ChatFormatting.RED);
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, glowArea(player),
                monster -> isMonster(monster) && monster.hasEffect(MobEffects.GLOWING));
        if (candidates.isEmpty()) {
            sendHint(player, "message.mimiworld.crimson_vertical_eye.no_target", ChatFormatting.RED);
            return;
        }
        candidates.sort(Comparator.comparingDouble(monster -> monster.distanceToSqr(player)));
        for (LivingEntity monster : candidates) {
            if (teleportBehind(player, level, monster)) {
                sendHint(player, "message.mimiworld.crimson_vertical_eye.success", ChatFormatting.GREEN);
                return;
            }
        }
        sendHint(player, "message.mimiworld.crimson_vertical_eye.blocked", ChatFormatting.RED);
    }

    /** 快捷栏上方的操作反馈；默认关闭，由配置 [messages].crimson_eye_teleport_hint 控制。 */
    private static void sendHint(ServerPlayer player, String key, ChatFormatting color) {
        if (!ModConfig.crimsonEyeTeleportHint()) {
            return;
        }
        player.displayClientMessage(Component.translatable(key).withStyle(color), true);
    }

    /** 传送到怪物身后并面向它；成功返回 true。 */
    private static boolean teleportBehind(ServerPlayer player, ServerLevel level, LivingEntity monster) {
        // 身后 = 怪物朝向的反方向，偏移量随怪物宽度增大，避免落进大体型怪物的碰撞箱中心
        double offset = 1.0D + monster.getBbWidth() * 0.5D;
        Vec3 dest = monster.position().subtract(monster.getLookAngle().scale(offset));
        Vec3 destEye = dest.add(0.0D, player.getEyeHeight(), 0.0D);

        // 不可穿墙：玩家眼睛到目标落点眼睛的连线之间不能有方块阻挡
        BlockHitResult hit = level.clip(new ClipContext(player.getEyePosition(), destEye,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.MISS) {
            return false;
        }
        // 落点自身也不能卡在方块里
        AABB destBox = player.getDimensions(player.getPose()).makeBoundingBox(dest.x, dest.y, dest.z);
        if (!level.noCollision(player, destBox)) {
            return false;
        }

        Vec3 facing = monster.getEyePosition().subtract(destEye);
        if (facing.lengthSqr() < 1.0E-4D) {
            return false;
        }
        float yaw = (float) (Mth.atan2(facing.z, facing.x) * (180.0D / Math.PI)) - 90.0F;
        float pitch = (float) -(Mth.atan2(facing.y,
                Math.sqrt(facing.x * facing.x + facing.z * facing.z)) * (180.0D / Math.PI));
        player.teleportTo(level, dest.x, dest.y, dest.z, yaw, pitch);
        player.setDeltaMovement(Vec3.ZERO);
        return true;
    }
}
