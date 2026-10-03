package com.mimiworld.mimiworld.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 煮锅系列碰撞箱（兼选框）生成器。
 *
 * <p>以小煮锅模型（倍率 1.0）的 12 个方块坐标为唯一基准：
 * 底座、锅底、四面锅壁、四面锅沿、两只耳把。缩放规则与模型完全一致——</p>
 * <ul>
 *   <li>水平方向以方块中心 (8,8) 缩放：coord = 8 + (base - 8) * scale</li>
 *   <li>竖直方向以地面为基准：coord = base * scale</li>
 * </ul>
 * <p>12 个盒子合并后，锅壁之间的内腔天然为空，碰撞形状与模型逐块对应。</p>
 */
public final class CookingPotShapes {

    private CookingPotShapes() {}

    // 每个元素：倍率 1.0 时的 [x1, y1, z1, x2, y2, z2]
    private static final double[][] BOXES = {
            // 底座
            {4.8, 0.0, 4.8, 11.2, 2.0, 11.2},
            // 锅底
            {3.2, 2.0, 3.2, 12.8, 3.6, 12.8},
            // 四面锅壁
            {3.2, 3.6, 3.2, 12.8, 10.8, 4.8},   // 北
            {3.2, 3.6, 11.2, 12.8, 10.8, 12.8},  // 南
            {3.2, 3.6, 4.8, 4.8, 10.8, 11.2},    // 西
            {11.2, 3.6, 4.8, 12.8, 10.8, 11.2},  // 东
            // 四面锅沿
            {1.6, 10.8, 1.6, 14.4, 12.8, 3.2},   // 北
            {1.6, 10.8, 12.8, 14.4, 12.8, 14.4}, // 南
            {1.6, 10.8, 3.2, 3.2, 12.8, 12.8},   // 西
            {12.8, 10.8, 3.2, 14.4, 12.8, 12.8}, // 东
            // 两只耳把
            {5.6, 8.8, 1.6, 10.4, 10.4, 3.2},    // 北
            {5.6, 8.8, 12.8, 10.4, 10.4, 14.4},  // 南
    };

    /** 按倍率生成与模型一致的碰撞形状（单格煮锅用）。 */
    public static VoxelShape forScale(double scale) {
        VoxelShape result = Shapes.empty();
        for (double[] b : BOXES) {
            double x1 = 8.0 + (b[0] - 8.0) * scale;
            double z1 = 8.0 + (b[2] - 8.0) * scale;
            double x2 = 8.0 + (b[3] - 8.0) * scale;
            double z2 = 8.0 + (b[5] - 8.0) * scale;
            double y1 = b[1] * scale;
            double y2 = b[4] * scale;
            result = Shapes.or(result, Block.box(x1, y1, z1, x2, y2, z2));
        }
        return result.optimize();
    }

    private static final double EPS = 1.0e-7;

    /**
     * 按倍率生成逐格切片形状（多方块煮锅用）。
     *
     * <p>返回以主方块为原点、每个占用格子的相对坐标 → 该格子内（局部 0~16）的形状。
     * 12 个模型盒子按主方块坐标系（中心水平 8、贴地）裁剪到各格子；
     * 无几何的格子不在 Map 中；主格键为 (0,0,0)。</p>
     */
    public static Map<BlockPos, VoxelShape> slices(double scale) {
        // 先按格子收集原始盒子（局部坐标 double[]: x1,y1,z1,x2,y2,z2）
        Map<BlockPos, List<double[]>> raw = new HashMap<>();

        for (double[] b : BOXES) {
            double gx1 = 8.0 + (b[0] - 8.0) * scale;
            double gz1 = 8.0 + (b[2] - 8.0) * scale;
            double gx2 = 8.0 + (b[3] - 8.0) * scale;
            double gz2 = 8.0 + (b[5] - 8.0) * scale;
            double gy1 = b[1] * scale;
            double gy2 = b[4] * scale;

            int cx1 = (int) Math.floor(gx1 / 16.0);
            int cx2 = (int) Math.floor((gx2 - EPS) / 16.0);
            int cy1 = (int) Math.floor(gy1 / 16.0);
            int cy2 = (int) Math.floor((gy2 - EPS) / 16.0);
            int cz1 = (int) Math.floor(gz1 / 16.0);
            int cz2 = (int) Math.floor((gz2 - EPS) / 16.0);

            for (int cx = cx1; cx <= cx2; cx++) {
                for (int cy = cy1; cy <= cy2; cy++) {
                    for (int cz = cz1; cz <= cz2; cz++) {
                        double lx1 = clamp(gx1 - cx * 16.0);
                        double ly1 = clamp(gy1 - cy * 16.0);
                        double lz1 = clamp(gz1 - cz * 16.0);
                        double lx2 = clamp(gx2 - cx * 16.0);
                        double ly2 = clamp(gy2 - cy * 16.0);
                        double lz2 = clamp(gz2 - cz * 16.0);
                        if (lx2 <= lx1 || ly2 <= ly1 || lz2 <= lz1) {
                            continue;
                        }
                        raw.computeIfAbsent(new BlockPos(cx, cy, cz), k -> new ArrayList<>())
                                .add(new double[]{lx1, ly1, lz1, lx2, ly2, lz2});
                    }
                }
            }
        }

        Map<BlockPos, VoxelShape> result = new HashMap<>();
        for (Map.Entry<BlockPos, List<double[]>> entry : raw.entrySet()) {
            VoxelShape shape = Shapes.empty();
            for (double[] box : entry.getValue()) {
                shape = Shapes.or(shape, Block.box(box[0], box[1], box[2], box[3], box[4], box[5]));
            }
            result.put(entry.getKey(), shape.optimize());
        }
        return result;
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(16.0, v));
    }
}
