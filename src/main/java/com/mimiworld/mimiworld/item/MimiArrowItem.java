package com.mimiworld.mimiworld.item;

import com.mimiworld.mimiworld.entity.MimiArrowEntity;
import com.mimiworld.mimiworld.entity.ModEntities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * 米米世界箭矢物品：可被弓／弩发射的 {@link ArrowItem}。
 *
 * <p>发射时生成 {@link MimiArrowEntity}，把本箭的固定伤害与中毒等级写入实体；
 * 命中结算与附加效果见实体类。伤害数值为「命中即结算」的固定值，随后由弓的伤害倍率
 * 与满蓄力暴击进一步修正。</p>
 */
public class MimiArrowItem extends ArrowItem {

    private final double hitDamage;
    private final int poisonLevel;

    /**
     * @param hitDamage   命中固定伤害
     * @param poisonLevel 命中附中毒等级，0 表示不附加中毒
     */
    public MimiArrowItem(double hitDamage, int poisonLevel, Item.Properties properties) {
        super(properties);
        this.hitDamage = hitDamage;
        this.poisonLevel = poisonLevel;
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
        return new MimiArrowEntity(ModEntities.MIMI_ARROW.get(), shooter, level,
                this.hitDamage, this.poisonLevel, this);
    }

    /**
     * 兼容「无限」附魔。
     *
     * <p>原版 {@code ArrowItem#isInfinite} 内部带有 {@code getClass() == ArrowItem.class} 判断，
     * 导致任何自定义子类都无法享受无限附魔；这里覆写为只要弓上有无限附魔即可无限。</p>
     */
    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, Player player) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0;
    }
}
