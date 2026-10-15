package com.mimiworld.mimiworld.world;

import com.mimiworld.mimiworld.ModDimensions;
import com.mimiworld.mimiworld.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * 残堕界传送门（remnant_fallen_portal）的维度传送器。
 *
 * <p>去程：进入残堕界时确保「降临平台」存在，并把实体放在平台角落。</p>
 * <p>返程：点对点回到进入时那座传送门旁边（同原版地狱门的手感）——入口位置在去程时记在实体的
 * 持久化数据里（{@link #rememberEntry}），返程时读回并在该传送门周围找一个安全落点
 * （{@link #findSafeLanding}）；记录缺失、维度不符或传送门已被拆除时，回退到世界出生点地表。</p>
 */
public class RemnantFallenTeleporter implements ITeleporter {

    /** 唯一实例（无状态，可全局复用）。 */
    public static final RemnantFallenTeleporter INSTANCE = new RemnantFallenTeleporter();

    /** 残堕界「降临平台」的基准角点：4×4 平台占据 (0..3, 64, 0..3)。 */
    private static final BlockPos PLATFORM_CORNER = new BlockPos(0, 64, 0);
    /** 降临平台上方需要清空为空气的格数，保证玩家与实体不被方块窒息。 */
    private static final int PLATFORM_CLEAR_HEIGHT = 4;

    /** 实体持久化数据：入口传送门所在方块坐标（BlockPos.asLong）。 */
    private static final String TAG_ENTRY_POS = "mimiworld:remnant_fallen_entry_pos";
    /** 实体持久化数据：入口所在维度 id（用于校验返程目标维度一致）。 */
    private static final String TAG_ENTRY_DIM = "mimiworld:remnant_fallen_entry_dim";

    /** 返程时在入口传送门周围搜索安全落点的最大水平半径（格）。 */
    private static final int LANDING_SEARCH_RADIUS = 3;

    private RemnantFallenTeleporter() {
    }

    /**
     * 去程（即将进入残堕界）时调用：把入口传送门所在的维度与方块坐标记到实体的持久化数据上。
     * 玩家跨维度为同一实体实例，生物经 {@code restoreFrom} 的 save/load 也会带过去，无需额外持久化。
     */
    public static void rememberEntry(Entity entity, ResourceKey<Level> fromDimension, BlockPos portalPos) {
        CompoundTag data = entity.getPersistentData();
        data.putLong(TAG_ENTRY_POS, portalPos.asLong());
        data.putString(TAG_ENTRY_DIM, fromDimension.location().toString());
    }

    @Nullable
    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld,
                                    Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        if (destWorld.dimension().equals(ModDimensions.REMNANT_FALLEN_REALM)) {
            // 落入残堕界：落在降临平台的一个角落（非传送门方块），避免刚落地就被立刻传回。
            BlockPos arrival = ensureArrivalPlatform(destWorld);
            return new PortalInfo(
                    new Vec3(arrival.getX() + 0.5D, arrival.getY() + 1.0D, arrival.getZ() + 0.5D),
                    Vec3.ZERO, entity.getYRot(), entity.getXRot());
        }
        // 返程：点对点落回入口那座传送门旁（原版地狱门手感）。
        BlockPos entry = recallEntry(entity, destWorld);
        if (entry != null) {
            BlockPos landing = findSafeLanding(destWorld, entry);
            if (landing != null) {
                return new PortalInfo(
                        new Vec3(landing.getX() + 0.5D, landing.getY() + 1.0D, landing.getZ() + 0.5D),
                        Vec3.ZERO, entity.getYRot(), entity.getXRot());
            }
        }
        // 兜底：入口记录缺失 / 传送门已拆除 → 世界出生点地表之上（同原版末地门回主世界）。
        BlockPos spawn = destWorld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                destWorld.getSharedSpawnPos());
        return new PortalInfo(
                new Vec3(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D),
                Vec3.ZERO, entity.getYRot(), entity.getXRot());
    }

    /** 读回入口传送门坐标；要求记录的维度与本次返程目标维度一致，否则视为无记录。 */
    @Nullable
    private static BlockPos recallEntry(Entity entity, ServerLevel destWorld) {
        CompoundTag data = entity.getPersistentData();
        if (!data.contains(TAG_ENTRY_POS, Tag.TAG_LONG) || !data.contains(TAG_ENTRY_DIM, Tag.TAG_STRING)) {
            return null;
        }
        if (!data.getString(TAG_ENTRY_DIM).equals(destWorld.dimension().location().toString())) {
            return null;
        }
        return BlockPos.of(data.getLong(TAG_ENTRY_POS));
    }

    /**
     * 在入口传送门周围逐环（半径 1..{@link #LANDING_SEARCH_RADIUS}）寻找一个安全落脚方块：
     * 该方块不是传送门方块且顶面结实（如深渊岩边块），其上方两格无碰撞、也不是传送门方块。
     *
     * <p>刻意不落在传送门正上方：传送门薄层无碰撞又贴地，落上去必然再次落入薄层被弹回残堕界。</p>
     *
     * @return 可站立方块的位置；找不到返回 null
     */
    @Nullable
    private static BlockPos findSafeLanding(ServerLevel level, BlockPos entry) {
        if (!level.getBlockState(entry).is(ModBlocks.REMNANT_FALLEN_PORTAL.get())) {
            // 入口传送门已被拆除（如创造模式破坏），记录不再可用
            return null;
        }
        for (int radius = 1; radius <= LANDING_SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue; // 只扫当前这一环，保证由近及远
                    }
                    BlockPos candidate = entry.offset(dx, 0, dz);
                    if (isSafeStand(level, candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    /** 判断 standOn 是否为可站立方块（非传送门、顶面结实，且上方两格无碰撞、非传送门）。 */
    private static boolean isSafeStand(ServerLevel level, BlockPos standOn) {
        BlockState ground = level.getBlockState(standOn);
        if (ground.is(ModBlocks.REMNANT_FALLEN_PORTAL.get())
                || !ground.isFaceSturdy(level, standOn, Direction.UP)) {
            return false;
        }
        return isClear(level, standOn.above()) && isClear(level, standOn.above(2));
    }

    /** 判断位置是否可容纳实体（无碰撞且不是传送门方块）。 */
    private static boolean isClear(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.is(ModBlocks.REMNANT_FALLEN_PORTAL.get()) && state.getCollisionShape(level, pos).isEmpty();
    }

    /**
     * 确保残堕界的降临平台存在（首次传送时构建，之后幂等复用，同时会修复被破坏的地块）：
     * 4×4 区域的四角与八条边为残堕深渊岩，中心 2×2 为残堕界传送门方块（供返程使用），
     * 并把整块平台上方 4 格清空为空气。
     *
     * @return 平台基准角点（玩家落点所在角落方块的位置）
     */
    private static BlockPos ensureArrivalPlatform(ServerLevel level) {
        BlockState rock = ModBlocks.REMNANT_FALLEN_ABYSS_ROCK.get().defaultBlockState();
        BlockState portal = ModBlocks.REMNANT_FALLEN_PORTAL.get().defaultBlockState();
        for (int i = 0; i <= 3; i++) {
            for (int j = 0; j <= 3; j++) {
                BlockPos pos = PLATFORM_CORNER.offset(i, 0, j);
                boolean center = (i == 1 || i == 2) && (j == 1 || j == 2);
                BlockState target = center ? portal : rock;
                if (!level.getBlockState(pos).equals(target)) {
                    level.setBlock(pos, target, 3);
                }
                for (int dy = 1; dy <= PLATFORM_CLEAR_HEIGHT; dy++) {
                    BlockPos above = pos.offset(0, dy, 0);
                    if (!level.getBlockState(above).isAir()) {
                        level.setBlock(above, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        return PLATFORM_CORNER;
    }
}
