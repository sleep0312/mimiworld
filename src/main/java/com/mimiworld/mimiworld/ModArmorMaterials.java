package com.mimiworld.mimiworld;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

/**
 * 米米世界自定义防具材料（共 5 套装甲引用本类）。
 *
 * <p>装备贴图走原版渲染约定：assets/mimiworld/textures/models/armor/&lt;name&gt;_layer_1.png
 * （头盔/胸甲/靴子）与 &lt;name&gt;_layer_2.png（护腿），getName() 返回 mimiworld: 命名空间。</p>
 *
 * <p>防御点数组顺序为 {脚, 腿, 胸, 头}；耐久系数乘以原版基准 {13,15,16,11}。</p>
 */
public enum ModArmorMaterials implements ArmorMaterial {
    /** 饭晶合金套：钻石同级防御，韧性 1（修复饭晶合金锭） */
    RICE_CRYSTAL_ALLOY("rice_crystal_alloy", 26, new int[]{3, 6, 8, 3}, 14,
            SoundEvents.ARMOR_EQUIP_DIAMOND, 1.0F, 0.0F,
            () -> Ingredient.of(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get())),
    /** 超重结晶套：高防高韧重甲，带微弱击退抗性（修复超重结晶） */
    SUPER_HEAVY_CRYSTAL("super_heavy_crystal", 34, new int[]{4, 7, 9, 4}, 12,
            SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.05F,
            () -> Ingredient.of(ModItems.SUPER_HEAVY_CRYSTAL.get())),
    /** 复合米饭套：钻石同级防御、韧性 2、高附魔（修复复合米饭锭） */
    COMPOUND_RICE_MEAL("compound_rice_meal", 36, new int[]{3, 6, 8, 3}, 18,
            SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F,
            () -> Ingredient.of(ModItems.COMPOUND_RICE_MEAL_INGOT.get())),
    /** 清洁复合套：下界合金同级、韧性 3.5、击退抗性 0.1（修复清洁复合锭） */
    CLEANSING_COMPOUND("cleansing_compound", 42, new int[]{4, 7, 9, 4}, 20,
            SoundEvents.ARMOR_EQUIP_NETHERITE, 3.5F, 0.1F,
            () -> Ingredient.of(ModItems.CLEANSING_COMPOUND_INGOT.get())),
    /** 超懒合金套：终极套装，10 点总防御、韧性 5、击退抗性 0.15（修复超懒合金锭） */
    SUPER_LAZY_ALLOY("super_lazy_alloy", 50, new int[]{4, 8, 10, 4}, 22,
            SoundEvents.ARMOR_EQUIP_NETHERITE, 5.0F, 0.15F,
            () -> Ingredient.of(ModItems.SUPER_LAZY_ALLOY_INGOT.get())),
    /** 米米神翼（仅胸甲）：钻石同级胸甲防御、韧性 2（修复米米幻翼膜） */
    MIMI_WINGS("mimi_wings", 30, new int[]{0, 0, 8, 0}, 18,
            SoundEvents.ARMOR_EQUIP_ELYTRA, 2.0F, 0.0F,
            () -> Ingredient.of(ModItems.MIMI_PHANTOM_MEMBRANE.get())),
    /** 超懒神翼（仅胸甲）：终极翼甲，10 点胸甲防御、韧性 5、击退抗性 0.15（修复超懒合金锭） */
    SUPER_LAZY_WINGS("super_lazy_wings", 50, new int[]{0, 0, 10, 0}, 22,
            SoundEvents.ARMOR_EQUIP_NETHERITE, 5.0F, 0.15F,
            () -> Ingredient.of(ModItems.SUPER_LAZY_ALLOY_INGOT.get())),
    /** 最懒天堕套（胸甲/头盔/护腿/靴子）：堕落系终极甲，韧性 4、击退抗性 0.12（修复堕落之心） */
    HEAVENFALL("heavenfall", 46, new int[]{4, 7, 9, 4}, 20,
            SoundEvents.ARMOR_EQUIP_NETHERITE, 4.0F, 0.12F,
            () -> Ingredient.of(ModItems.FALLEN_HEART.get())),
    /** 残堕宗罪套（头盔/胸甲/护腿/靴子）：龙系终末甲，韧性 5.5、击退抗性 0.18（修复宗罪龙头冠） */
    REMNANT_FALLEN_CARDINAL_SIN("remnant_fallen_cardinal_sin", 54, new int[]{5, 8, 11, 5}, 24,
            SoundEvents.ARMOR_EQUIP_NETHERITE, 5.5F, 0.18F,
            () -> Ingredient.of(ModItems.CARDINAL_SIN_DRAGON_CROWN.get()));

    /** 各槽位耐久基准，顺序与 EquipmentSlot.values() 一致（脚/腿/胸/头） */
    private static final int[] BASE_DURABILITY = {13, 15, 16, 11};

    private final String name;
    private final int durabilityMultiplier;
    private final int[] defenseForSlot;
    private final int enchantmentValue;
    private final net.minecraft.sounds.SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyIngredient repairIngredient;

    ModArmorMaterials(String name, int durabilityMultiplier, int[] defenseForSlot, int enchantmentValue,
                      net.minecraft.sounds.SoundEvent equipSound, float toughness, float knockbackResistance,
                      Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.defenseForSlot = defenseForSlot;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = new LazyIngredient(repairIngredient);
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        ModConfig.ArmorStats stats = ModConfig.armor(name);
        int multiplier = stats == null ? durabilityMultiplier : ModConfig.read(stats.durabilityMultiplier());
        return BASE_DURABILITY[type.getSlot().getIndex()] * multiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        int slot = type.getSlot().getIndex();
        ModConfig.ArmorStats stats = ModConfig.armor(name);
        return stats == null ? defenseForSlot[slot] : ModConfig.read(stats.defense().get(slot));
    }

    @Override
    public int getEnchantmentValue() {
        ModConfig.ArmorStats stats = ModConfig.armor(name);
        return stats == null ? enchantmentValue : ModConfig.read(stats.enchantmentValue());
    }

    @Override
    public net.minecraft.sounds.SoundEvent getEquipSound() {
        return equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public String getName() {
        // mimiworld: 命名空间，解析到 assets/mimiworld/textures/models/armor/<name>_layer_{1,2}.png
        return MimiWorld.MODID + ":" + name;
    }

    @Override
    public float getToughness() {
        ModConfig.ArmorStats stats = ModConfig.armor(name);
        return stats == null ? toughness : (float) (double) ModConfig.read(stats.toughness());
    }

    @Override
    public float getKnockbackResistance() {
        ModConfig.ArmorStats stats = ModConfig.armor(name);
        return stats == null ? knockbackResistance
                : (float) (double) ModConfig.read(stats.knockbackResistance());
    }

    String id() {
        return name;
    }

    int rawDefense(int slot) {
        return defenseForSlot[slot];
    }

    int rawDurabilityMultiplier() {
        return durabilityMultiplier;
    }

    int rawEnchantmentValue() {
        return enchantmentValue;
    }

    float rawToughness() {
        return toughness;
    }

    float rawKnockbackResistance() {
        return knockbackResistance;
    }

    /** 延迟解析修复材料：枚举加载时 ModItems 字段可能尚未初始化，首次调用时才取值 */
    private static final class LazyIngredient {
        private final Supplier<Ingredient> supplier;
        private Ingredient cached;

        LazyIngredient(Supplier<Ingredient> supplier) {
            this.supplier = supplier;
        }

        Ingredient get() {
            if (cached == null) {
                cached = supplier.get();
            }
            return cached;
        }
    }
}
