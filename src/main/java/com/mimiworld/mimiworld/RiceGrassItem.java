package com.mimiworld.mimiworld;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * 米草物品：既是可放置的植物方块物品（BlockItem），又是可食用物品。
 *
 * 注意 1.20.1 的坑：BlockItem 重写了 use()，默认右键只尝试放置方块，
 * 即使 Item.Properties 配了 .food() 也永远不会触发食用（原版没有可食用的 BlockItem）。
 * 因此这里手动补回 Item.use() 的食用分支：饥饿时右键 = 吃；否则走 BlockItem 原逻辑 = 放草。
 * 游戏处理顺序是先 useOn（点中方块表面）再 use，所以对着可种植位置右键仍然优先种草，
 * 想吃的时候对着空气/天空右键即可（与原版「可放置+可使用」类物品的通用交互一致）。
 */
public class RiceGrassItem extends BlockItem {
    public RiceGrassItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // canEat(false)：生存模式下只有饥饿条不满才能吃，创造模式随时可吃（与普通食物一致）
        if (this.isEdible() && player.canEat(false)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 10; // 食用耗时 0.5 秒（20 tick = 1 秒）
    }
}
