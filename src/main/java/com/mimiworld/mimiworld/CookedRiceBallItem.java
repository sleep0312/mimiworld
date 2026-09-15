package com.mimiworld.mimiworld;

import com.mimiworld.mimiworld.entity.CookedRiceBallProjectile;
import com.mimiworld.mimiworld.entity.ModEntities;
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
 * 熟米团：可投掷物品。右键掷出熟米团投掷物（命中 2.5 伤害 + 缓慢 III/失明 II 各 10s），投掷冷却 2 秒。
 */
public class CookedRiceBallItem extends Item {
    public static final int THROW_COOLDOWN_TICKS = 40; // 2 秒

    public CookedRiceBallItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 投掷冷却由原版物品冷却系统统一管理（与米团/末影珍珠同款，动作栏显示冷却遮罩）
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            CookedRiceBallProjectile projectile =
                    new CookedRiceBallProjectile(ModEntities.COOKED_RICE_BALL.get(), player, level);
            projectile.setItem(stack);
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
