package com.mimiworld.mimiworld.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 米米世界箭矢实体：在原版 {@link Arrow} 基础上携带「固定伤害 + 命中中毒」两项自定义属性。
 *
 * <p>本模组 8 种箭共用这一个实体类型（{@code mimiworld:mimi_arrow}），
 * 具体数值由发射它的 {@link com.mimiworld.mimiworld.item.MimiArrowItem} 在生成时写入。
 * 除伤害与附加效果外，飞行、暴击、穿透、插地、回收等全部沿用原版箭逻辑。</p>
 */
public class MimiArrowEntity extends Arrow {

    private static final String TAG_HIT_DAMAGE = "MimiHitDamage";
    private static final String TAG_POISON_LEVEL = "MimiPoisonLevel";
    private static final String TAG_PICKUP_ITEM = "MimiPickupItem";
    private static final String TAG_BOW_MULTIPLIER = "MimiBowMultiplier";

    /** 中毒持续时间：10 秒。 */
    public static final int POISON_DURATION_TICKS = 200;

    /** 固定命中伤害（未指定时为 1，即米箭）。 */
    private double hitDamage = 1.0D;
    /** 中毒等级，0 表示不施加中毒（米箭）。 */
    private int poisonLevel;
    /** 回收/落地时掉落的箭矢物品，即射出它的那一种箭。 */
    private Item pickupItem = Items.ARROW;
    /** 发射它的那把弓附加的伤害倍率，默认 1.0（不放大）。 */
    private double bowDamageMultiplier = 1.0D;

    public MimiArrowEntity(EntityType<? extends MimiArrowEntity> type, Level level) {
        super(type, level);
    }

    public MimiArrowEntity(EntityType<? extends MimiArrowEntity> type, LivingEntity shooter, Level level,
                           double hitDamage, int poisonLevel, Item pickupItem) {
        // 原版 Arrow 没有 EntityType+LivingEntity 的构造器，这里按 AbstractArrow 的同名构造器逻辑初始化
        super(type, level);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
        this.setOwner(shooter);
        if (shooter instanceof Player) {
            this.pickup = AbstractArrow.Pickup.ALLOWED;
        }
        this.hitDamage = hitDamage;
        this.poisonLevel = poisonLevel;
        this.pickupItem = pickupItem;
    }

    /**
     * 固定伤害结算。
     *
     * <p>原版 {@code AbstractArrow#onHitEntity} 的伤害公式是「速度长度 × baseDamage」向上取整，
     * 因此直接把 baseDamage 设成目标伤害会被拉弓力度放大到约 3 倍。
     * 这里在命中前按当前速度反解 baseDamage = (目标伤害 - 0.5) / 速度，
     * 使原版公式恰好算出目标伤害，同时完整保留暴击、穿透、击退、
     * 以及腥红烛照等基于 LivingHurtEvent 的伤害倍率逻辑。</p>
     */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        double target = this.hitDamage * this.bowDamageMultiplier;
        double speed = this.getDeltaMovement().length();
        this.setBaseDamage(speed > 1.0E-4D ? (target - 0.5D) / speed : target);
        super.onHitEntity(result);
    }

    /**
     * 设置发射它的弓附加的伤害倍率，最终命中伤害 = 箭的固定伤害 × 该倍率。
     *
     * <p>供自定义弓（{@code BowItem} 子类）在 {@code customArrow} 中注入，
     * 使 8 种箭 × 8 把弓的组合各自产生不同伤害。</p>
     */
    public void setBowDamageMultiplier(double multiplier) {
        this.bowDamageMultiplier = multiplier;
    }

    /** 命中生物后按等级施加 10 秒中毒（等级 0 表示不施加，对应米箭）。 */
    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        if (this.poisonLevel > 0) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION_TICKS, this.poisonLevel - 1));
        }
    }

    /** 掉落与回收时返回射出它的那种箭，避免落地的自定义箭变成原版箭。 */
    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(this.pickupItem);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble(TAG_HIT_DAMAGE, this.hitDamage);
        tag.putInt(TAG_POISON_LEVEL, this.poisonLevel);
        tag.putDouble(TAG_BOW_MULTIPLIER, this.bowDamageMultiplier);
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(this.pickupItem);
        if (id != null) {
            tag.putString(TAG_PICKUP_ITEM, id.toString());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(TAG_HIT_DAMAGE)) {
            this.hitDamage = tag.getDouble(TAG_HIT_DAMAGE);
        }
        this.poisonLevel = tag.getInt(TAG_POISON_LEVEL);
        if (tag.contains(TAG_BOW_MULTIPLIER)) {
            this.bowDamageMultiplier = tag.getDouble(TAG_BOW_MULTIPLIER);
        }
        if (tag.contains(TAG_PICKUP_ITEM)) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString(TAG_PICKUP_ITEM)));
            if (item != null) {
                this.pickupItem = item;
            }
        }
    }
}
