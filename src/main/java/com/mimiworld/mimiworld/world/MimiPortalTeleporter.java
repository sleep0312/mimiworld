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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * 米米传送门方块（mimi_portal）的维度传送器。
 *
 * <p>去程：进入米米维度时落在维度出生点地表（MOTION_BLOCKING_NO_LEAVES 高度图），
 * 并把入口传送门的维度与坐标记在实体持久化数据里（{@link #rememberEntry}）。</p>
 * <p>返程：点对点回到进入时那座传送门旁边（同原版地狱门手感）——在入口周围找安全落点
 * （{@link #findSafeLanding}），入口记录缺失、维度不符或传送门已被拆除时，
 * 回退到世界出生点地表。结构参照残堕界传送门 {@link RemnantFallenTeleporter} 实现。</p>
 */
public class MimiPortalTeleporter implements ITeleporter {

    /** 唯一实例（无状态，可全局复用）。 */
    public static final MimiPortalTeleporter INSTANCE = new MimiPortalTeleporter();

    /** 实体持久化数据：入口传送门所在方块坐标（BlockPos.asLong）。 */
    private static final String TAG_ENTRY_POS = "mimiworld:mimi_entry_pos";
    /** 实体持久化数据：入口所在维度 id（用于校验返程目标维度一致）。 */
    private static final String TAG_ENTRY_DIM = "mimiworld:mimi_entry_dim";

    /** 返程时在入口传送门周围搜索安全落点的最大水平半径（格）。 */
    private static final int LANDING_SEARCH_RADIUS = 3;

    private MimiPortalTeleporter() {
    }

    /**
     * 去程（即将进入米米维度）时调用：把入口传送门所在的维度与方块坐标记到实体的持久化数据上。
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
        if (destWorld.dimension().equals(ModDimensions.MIMI_DIMENSION)) {
            // 落入米米维度：出生点列的地表之上（非传送门方块，不会落地即被传回）。
            return new PortalInfo(surfaceSpawn(destWorld), Vec3.ZERO, entity.getYRot(), entity.getXRot());
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
        // 兜底：入口记录缺失 / 传送门已拆除 → 出生点列地表之上（同原版末地门回主世界）。
        return new PortalInfo(surfaceSpawn(destWorld), Vec3.ZERO, entity.getYRot(), entity.getXRot());
    }

    /**
     * 出生点列的地表落点（首个可站立的 Y，实体脚部位置）。
     *
     * <p>两个坑都要绕开：</p>
     * <ol>
     *   <li>刻意不用 {@code getHeightmapPos}：它返回 {@code min(地表高度, pos.getY() + 1)}，
     *       会把传入坐标自身的 Y 当作上限——旧世界 level.dat 的 SpawnY 异常为 -60 时
     *       会把玩家截断到世界底部（虚空）；</li>
     *   <li>{@link Level#getHeight} 在目标区块<b>未加载</b>时会直接返回
     *       {@code getMinBuildHeight()}（米米维度 = -64）——新世界首次进入米米维度时
     *       出生区块必然未加载，玩家会被放到底界处坠入虚空。因此先强制同步加载/生成
     *       出生区块，查出的高度若仍是底界值则自顶向下扫描兜底。</li>
     * </ol>
     */
    private static Vec3 surfaceSpawn(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        int x = spawn.getX();
        int z = spawn.getZ();
        // 强制同步加载（或生成）出生区块，否则 getHeight 走「未加载」分支返回世界底界
        level.getChunk(x >> 4, z >> 4);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y <= level.getMinBuildHeight() + 1) {
            // 高度图仍未就绪的兜底：自顶向下找首个碰撞非空的方块，落点取其顶面
            BlockPos.MutableBlockPos cursor =
                    new BlockPos.MutableBlockPos(x, level.getMaxBuildHeight() - 1, z);
            while (cursor.getY() > level.getMinBuildHeight()
                    && level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()) {
                cursor.move(Direction.DOWN);
            }
            y = cursor.getY() + 1;
        }
        return new Vec3(x + 0.5D, y, z + 0.5D);
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
     * 该方块不是传送门方块且顶面结实，其上方两格无碰撞、也不是传送门方块。
     *
     * <p>入口记录的是实体踩中的传送门柱（中心 2 格高），先沿柱向下归一到柱脚所在层
     * 再搜索，使落点在框架内部地面或框架方块顶面，均不与传送门柱重叠，
     * 避免落地即再次触发传送。</p>
     *
     * @return 可站立方块的位置；找不到返回 null
     */
    @Nullable
    private static BlockPos findSafeLanding(ServerLevel level, BlockPos entry) {
        if (!level.getBlockState(entry).is(ModBlocks.MIMI_PORTAL.get())) {
            // 入口传送门已被拆除（如创造模式破坏），记录不再可用
            return null;
        }
        // 入口可能记在传送门柱的下格或上格：归一到柱脚之下（框架内地面层）
        BlockPos base = entry;
        int guard = 0;
        while (level.getBlockState(base).is(ModBlocks.MIMI_PORTAL.get()) && guard++ < 16) {
            base = base.below();
        }
        for (int radius = 1; radius <= LANDING_SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue; // 只扫当前这一环，保证由近及远
                    }
                    BlockPos candidate = base.offset(dx, 0, dz);
                    if (isSafeStand(level, candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 判断 standOn 是否为可站立方块：顶面结实，其上方两格（落脚处）无碰撞、非传送门，
     * 且落脚处的 3×3 水平邻域内没有传送门——避免宽碰撞箱实体（马、船）落地时
     * 仍与传送门柱重叠，立即被再次触发传送。
     */
    private static boolean isSafeStand(ServerLevel level, BlockPos standOn) {
        BlockState ground = level.getBlockState(standOn);
        if (ground.is(ModBlocks.MIMI_PORTAL.get())
                || !ground.isFaceSturdy(level, standOn, Direction.UP)) {
            return false;
        }
        if (!isClear(level, standOn.above()) || !isClear(level, standOn.above(2))) {
            return false;
        }
        BlockPos feet = standOn.above();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (level.getBlockState(feet.offset(dx, 0, dz)).is(ModBlocks.MIMI_PORTAL.get())) {
                    return false;
                }
            }
        }
        return true;
    }

    /** 判断位置是否可容纳实体（无碰撞且不是传送门方块）。 */
    private static boolean isClear(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.is(ModBlocks.MIMI_PORTAL.get()) && state.getCollisionShape(level, pos).isEmpty();
    }
}
