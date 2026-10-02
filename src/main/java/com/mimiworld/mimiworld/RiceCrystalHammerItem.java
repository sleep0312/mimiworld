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
 * 饭晶锤（Rice Crystal Hammer）——自定义工具类物品。
 *
 * <p>设计要点：用户要求该工具最大堆叠 1（与普通工具一致）。本类直接继承 {@link Item}
 * （而非 DiggerItem/PickaxeItem），自行实现镐类工具的挖掘行为（挖掘速度 + 正确工具掉落判定），
 * 不使用耐久系统。</p>
 *
 * <p>挖掘等级/速度参考 RICE_CRYSTAL_TIER（钻石级，level=3，speed=8.0），
 * 修复材料为饭晶合金锭（保留 Tier 以便未来需要时复用）。
 * 近战定位为重锤：基础攻击伤害 +7（含玩家自身共 8 点），攻速 -3.2（慢速高伤）。</p>
 */
public class RiceCrystalHammerItem extends Item {

    /** 饭晶合金工具等级：参照钻石（耐久 1561 / 速度 8 / 伤害加成 3 / 等级 3 / 附魔值 15），修复材料为饭晶合金锭。 */
    public static final Tier RICE_CRYSTAL_TIER = new Tier() {
        @Override
        public int getUses() {
            return 1561;
        }

        @Override
        public float getSpeed() {
            return 8.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 3.0F;
        }

        @Override
        public int getLevel() {
            return 3;
        }

        @Override
        public int getEnchantmentValue() {
            return 15;
        }

        @Override
        public Ingredient getRepairIngredient() {
            // 运行期才调用，此时 ModItems 已完成注册
            return Ingredient.of(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get());
        }
    };

    /** 重锤近战修饰符：+7 攻击伤害（总计 8 点）、-3.2 攻速。 */
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public RiceCrystalHammerItem() {
        super(new Item.Properties().stacksTo(1));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 7.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(
                BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.2D, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    /** 对所有“镐可挖掘”方块给予饭晶合金等级的挖掘速度，其余方块为徒手速度。 */
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) ? RICE_CRYSTAL_TIER.getSpeed() : 1.0F;
    }

    /**
     * 正确工具掉落判定：仅镐类方块可被正确采集；挖掘等级门槛按标签手工比较
     * （钻石级 tier，因此石/铁/钻石门槛全部满足）。
     */
    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return false;
        }
        int level = RICE_CRYSTAL_TIER.getLevel();
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
