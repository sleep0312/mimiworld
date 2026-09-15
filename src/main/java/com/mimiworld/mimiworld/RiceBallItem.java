package com.mimiworld.mimiworld;

import com.mimiworld.mimiworld.entity.ModEntities;
import com.mimiworld.mimiworld.entity.RiceBallProjectile;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 米团：可投掷物品。右键掷出米团投掷物，投掷冷却 2 秒（40 tick），生存模式消耗 1 个。
 */
public class RiceBallItem extends Item {
    public static final int THROW_COOLDOWN_TICKS = 40; // 2 秒

    public RiceBallItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 投掷冷却由原版物品冷却系统统一管理（与末影珍珠同款，动作栏显示冷却遮罩）
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            RiceBallProjectile projectile = new RiceBallProjectile(ModEntities.RICE_BALL.get(), player, level);
            projectile.setItem(stack);
            // 参数：抛射者、俯仰角、偏航角、固定初速度偏置、初速度倍率、散布
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(projectile);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, THROW_COOLDOWN_TICKS);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
