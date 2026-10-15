package com.mimiworld.mimiworld.block;

import com.mimiworld.mimiworld.block.entity.FallenCrossBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 堕落十字架（fallen_cross）：带基座的华丽十字架装饰方块。
 *
 * <p>世界中的外观完全由 GeckoLib BlockEntityRenderer 渲染
 * （geo/block/fallen_cross.geo.json：三层基座 + 紫黑十字杆身 + 金箍与金端板 +
 * 前后紫色宝石，几何高 27px），BER 内以 16/27 缩放使整座十字架恰好收进一格、
 * 贴地居中放置；方块自身 JSON 模型不渲染（{@link RenderShape#ENTITYBLOCK_ANIMATED}），
 * 仅提供破坏粒子纹理；物品栏图标仍走 models/item 的 2D 图标，不受影响。</p>
 *
 * <p>暗下界合金质地、硬度 3.0、抗爆 6.0，需铁镐以上才掉落，破坏掉落本身
 * （loot_tables/blocks/fallen_cross.json）。无 GUI、无动画、不 tick。</p>
 *
 * <p>传送门激活：十字架被放置在堕落台上时会检查 4×4 仪式（四角＝堕落台+其正上方十字架），
 * 满足条件则在同层生成 8 个残堕深渊岩与 4 个残堕界传送门方块，详见 {@link #onPlace}。</p>
 */
public class FallenCrossBlock extends Block implements EntityBlock {

    /**
     * 与 BER 缩放后的可见模型占地一致：模型 x[2,14]、z[5,11]、y[0,27]，
     * 缩放 s=16/27 后 x∈[5/18,13/18]、z∈[7/18,11/18]、y 满一格，取十字柱整体包围盒。
     */
    private static final VoxelShape SHAPE = Shapes.box(5.0 / 18.0, 0.0, 7.0 / 18.0,
            13.0 / 18.0, 1.0, 11.0 / 18.0);

    public FallenCrossBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FallenCrossBlockEntity(pos, state);
    }

    /**
     * 方块被放置时尝试激活残堕界传送门。
     *
     * <p>若本十字架正下方是堕落台，则以该堕落台为 4×4 正方形的一个角点，检查四种可能的展开方向；
     * 当四角均为「堕落台 + 其正上方十字架」且 12 个非角落位置均可替换时，在堕落台同一 Y 层生成：
     * 方形八条边 → 8 个残堕深渊岩，方形中心 2×2 → 4 个残堕界传送门方块。堕落台与十字架保留、不消耗。</p>
     */
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level.isClientSide) {
            return;
        }
        BlockPos altarPos = pos.below();
        if (!level.getBlockState(altarPos).is(ModBlocks.FALLEN_ALTAR.get())) {
            return;
        }
        for (int dirX = -1; dirX <= 1; dirX += 2) {
            for (int dirZ = -1; dirZ <= 1; dirZ += 2) {
                if (tryFormPortal(level, altarPos, dirX, dirZ)) {
                    return;
                }
            }
        }
    }

    /** 4×4 网格中 (i, j) 是否为四角之一（即堕落台所在位置）。 */
    private static boolean isCorner(int i, int j) {
        return (i == 0 || i == 3) && (j == 0 || j == 3);
    }

    /** 4×4 网格中 (i, j) 是否为中心 2×2（即残堕界传送门方块所在位置）。 */
    private static boolean isCenter(int i, int j) {
        return (i == 1 || i == 2) && (j == 1 || j == 2);
    }

    /**
     * 以 corner 为角点、朝 (dirX, dirZ) 方向展开 4×4 方形。
     * 条件全部满足时生成传送门并返回 true，否则不做任何改动并返回 false。
     */
    private static boolean tryFormPortal(Level level, BlockPos corner, int dirX, int dirZ) {
        for (int i = 0; i <= 3; i++) {
            for (int j = 0; j <= 3; j++) {
                BlockPos p = corner.offset(i * dirX, 0, j * dirZ);
                if (isCorner(i, j)) {
                    if (!level.getBlockState(p).is(ModBlocks.FALLEN_ALTAR.get())
                            || !level.getBlockState(p.above()).is(ModBlocks.FALLEN_CROSS.get())) {
                        return false;
                    }
                } else if (!level.getBlockState(p).canBeReplaced()) {
                    // 方形内部必须是可替换方块（空气等），避免破坏已有建筑
                    return false;
                }
            }
        }
        for (int i = 0; i <= 3; i++) {
            for (int j = 0; j <= 3; j++) {
                if (isCorner(i, j)) {
                    continue;
                }
                BlockPos p = corner.offset(i * dirX, 0, j * dirZ);
                Block toPlace = isCenter(i, j) ? ModBlocks.REMNANT_FALLEN_PORTAL.get()
                        : ModBlocks.REMNANT_FALLEN_ABYSS_ROCK.get();
                level.setBlock(p, toPlace.defaultBlockState(), 3);
            }
        }
        return true;
    }
}
