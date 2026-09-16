package com.mimiworld.mimiworld;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 饭晶合金锤（编码 47，Rice Crystal Alloy Hammer）——自定义工具类物品，36 号饭晶锤的合金强化版。
 *
 * <p>设计要点与 {@link RiceCrystalHammerItem} 一致：最大堆叠 1（与普通工具一致）。本类直接继承
 * {@link Item}（而非 DiggerItem/PickaxeItem），自行实现镐类工具的挖掘行为，不使用耐久系统。</p>
 *
 * <p>属性在饭晶锤基础上整体提升一档（参照下界合金：耐久 2031 / 速度 9 / 等级 3 / 附魔值 18），
 * 修复材料仍为 35 号饭晶合金锭。近战为更重的重锤：基础攻击伤害 +9（含玩家自身共 10 点），
 * 攻速 -3.1（慢速高伤）。</p>
 */
public class RiceCrystalAlloyHammerItem extends Item {

    /** 饭晶合金强化等级：参照下界合金（耐久 2031 / 速度 9 / 伤害加成 4 / 等级 3 / 附魔值 18），修复材料为饭晶合金锭。 */
    public static final Tier RICE_CRYSTAL_ALLOY_TIER = new Tier() {
        @Override
        public int getUses() {
            return 2031;
        }

        @Override
        public float getSpeed() {
            return 9.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 4.0F;
        }

        @Override
        public int getLevel() {
            return 3;
        }

        @Override
        public int getEnchantmentValue() {
            return 18;
        }

        @Override
        public Ingredient getRepairIngredient() {
            // 运行期才调用，此时 ModItems 已完成注册
            return Ingredient.of(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get());
        }
    };

    /** 重锤近战修饰符：+9 攻击伤害（总计 10 点）、-3.1 攻速。 */
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public RiceCrystalAlloyHammerItem() {
        super(new Item.Properties().stacksTo(1));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 9.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(
                BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.1D, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    /** 对所有“镐可挖掘”方块给予饭晶合金等级的挖掘速度，其余方块为徒手速度。 */
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) ? RICE_CRYSTAL_ALLOY_TIER.getSpeed() : 1.0F;
    }

    /**
     * 正确工具掉落判定：仅镐类方块可被正确采集；挖掘等级门槛按标签手工比较
     * （等级 3，因此石/铁/钻石门槛全部满足）。
     */
    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return false;
        }
        int level = RICE_CRYSTAL_ALLOY_TIER.getLevel();
        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return level >= 3;
        }
        if (state.is(BlockTags.NEEDS_IRON_TOOL)) {
            return level >= 2;
        }
        if (state.is(BlockTags.NEEDS_STONE_TOOL)) {
            return level >= 1;
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }
}
