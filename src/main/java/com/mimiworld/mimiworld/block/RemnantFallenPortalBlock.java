package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.ModDimensions;
import com.mimiworld.mimiworld.world.RemnantFallenTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 残堕界传送门方块（remnant_fallen_portal）。
 *
 * <p>由四个堕落台 + 四个堕落十字架组成的 4×4 仪式自动生成，铺在地面（与堕落台同一 Y 层），
 * 外观是一块贴地、薄薄一层的紫色星空平面，行为参照原版末地门：无碰撞、不可破坏、
 * 发光，实体踩入（坠入）其中即被传送到另一侧维度。</p>
 *
 * <p>传送为双向：主世界/其他维度 → 残堕界，残堕界 → 主世界。
 * 落点由 {@link RemnantFallenTeleporter} 决定：去程落在残堕界固定「降临平台」的角落；
 * 返程则点对点回到进入时那座传送门旁边（入口位置在去程时记在实体上），同原版地狱门手感。</p>
 */
public class RemnantFallenPortalBlock extends Block {

    /** 贴地薄层：占据方块底部 1/4 高（4px），视觉上即「躺在地上的门」。 */
    private static final VoxelShape SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D);

    public RemnantFallenPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel serverLevel)
                || entity.isRemoved()
                || entity.level() != level
                || !entity.canChangeDimensions()) {
            return;
        }
        // 参照末地门：仅当实体碰撞箱真正与传送门薄层重叠时才触发，避免站在门旁边被误传。
        boolean overlapping = Shapes.joinIsNotEmpty(
                Shapes.create(entity.getBoundingBox().move(-pos.getX(), -pos.getY(), -pos.getZ())),
                state.getShape(level, pos), BooleanOp.AND);
        if (!overlapping) {
            return;
        }
        ResourceKey<Level> target = serverLevel.dimension().equals(ModDimensions.REMNANT_FALLEN_REALM)
                ? Level.OVERWORLD
                : ModDimensions.REMNANT_FALLEN_REALM;
        ServerLevel dest = serverLevel.getServer().getLevel(target);
        if (dest == null) {
            return;
        }
        if (!serverLevel.dimension().equals(ModDimensions.REMNANT_FALLEN_REALM)) {
            // 去程：把入口传送门的维度与坐标记在实体上，返程即可点对点回到这座门旁（原版地狱门手感）
            RemnantFallenTeleporter.rememberEntry(entity, serverLevel.dimension(), pos);
        }
        entity.changeDimension(dest, RemnantFallenTeleporter.INSTANCE);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + 0.3D;
        double z = pos.getZ() + random.nextDouble();
        level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0.0D, 0.02D, 0.0D);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }
}
