package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.ModDimensions;
import com.mimiworld.mimiworld.world.MimiPortalTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 米米传送门方块（mimi_portal）：米米传送门的「块状」本体。
 *
 * <p>由米米传送器仪式激活后生成在框架中心（下 2 格，顶上盖 1 格泥米，见示意图），
 * 外观为青色旋涡发光全立方块，行为参照原版地狱门：无碰撞、发光、生存不可破坏
 * （无 BlockItem、无战利品表，创造可拆除），实体踩入即被传送。</p>
 *
 * <p>传送为双向：任何维度 → 米米维度（落在维度出生点地表安全处）；
 * 米米维度 → 回到进入时那座传送门旁（入口记在实体上，见 {@link MimiPortalTeleporter}）。</p>
 */
public class MimiPortalBlock extends Block {

    public MimiPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel serverLevel)
                || entity.isRemoved()
                || entity.level() != level
                || !entity.canChangeDimensions()) {
            return;
        }
        ResourceKey<Level> target = serverLevel.dimension().equals(ModDimensions.MIMI_DIMENSION)
                ? Level.OVERWORLD
                : ModDimensions.MIMI_DIMENSION;
        ServerLevel dest = serverLevel.getServer().getLevel(target);
        if (dest == null) {
            return;
        }
        if (!serverLevel.dimension().equals(ModDimensions.MIMI_DIMENSION)) {
            // 去程（任意维度 → 米米维度）：把入口传送门的维度与坐标记在实体上，返程点对点回门旁
            MimiPortalTeleporter.rememberEntry(entity, serverLevel.dimension(), pos);
        }
        entity.changeDimension(dest, MimiPortalTeleporter.INSTANCE);
    }

    /**
     * 地狱门式级联：任一传送门方块被移除（创造拆柱、框架联动碎裂等），
     * 与之相连的传送门方块随之即刻碎掉，不会留下悬空的半截柱。
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                         boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbour = pos.relative(direction);
                if (level.getBlockState(neighbour).is(ModBlocks.MIMI_PORTAL.get())) {
                    level.removeBlock(neighbour, false);
                }
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }
}
