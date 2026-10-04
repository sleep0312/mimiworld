package com.mimiworld.mimiworld.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 米巫（Rice Witch，注册 id = rice_witch）——黑紫丝绒长袍的大米主题敌对女巫。
 *
 * <p>模型/贴图/动画资源（均由 Blockbench 导出）：
 * geo/rice_witch.geo.json、textures/entity/rice_witch.png、
 * animations/rice_witch.animation.json（动画键 animation.idle / animation.walk /
 * animation.attack / animation.throw）。模型为 64 方块 8 骨骼双足人形，
 * 三层弯尖帽+翘鼻+A 字裙+后腰药袋药瓶；贴图黑紫丝绒+金边+米白米饰+稻绿眼。
 * 模型脚底贴 y=0，碰撞箱 0.6×1.95（弯帽尖 y43.4 略高出箱体，同骨冠先例）。</p>
 *
 * <p>生物逻辑：敌对怪物（MobCategory.MONSTER），双攻击方式——
 * 远程向玩家丢溅射药水（4 格外随机丢中毒/缓慢，贴近时丢瞬间伤害），
 * 目标走到脸上时用近战魔法推击补刀。30 心（60 点血，沿用占位数值），
 * 音效沿用原版女巫。投掷动画 1.2 秒（24 tick），药水瓶在第 12 tick（出手帧）生成；
 * 近战动画 0.9 秒（18 tick），在 doHurtTarget 命中时触发。</p>
 */
public class RiceWitchEntity extends Monster implements GeoEntity, RangedAttackMob {

    /** 近战动画剩余 tick（命中时置 18 逐 tick 递减，自动同步到客户端驱动 GeckoLib 动画） */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(RiceWitchEntity.class, EntityDataSerializers.INT);
    /** 投药动画剩余 tick（RangedAttackGoal 触发时置 24，第 12 tick 生成药水实体） */
    private static final EntityDataAccessor<Integer> DATA_THROW_ANIM_TICKS =
            SynchedEntityData.defineId(RiceWitchEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.walk");
    /** 近战：单次 0.9 秒，收手后拧蓄力→右手向前法术推击→回收 */
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.attack");
    /** 投药：单次 1.2 秒，引臂→过头扭腰→前挥出手→随挥 */
    private static final RawAnimation ANIM_THROW = RawAnimation.begin().thenPlay("animation.throw");

    /** attack 动画时长 0.9 秒 = 18 tick */
    private static final int ATTACK_ANIMATION_TICKS = 18;
    /** throw 动画时长 1.2 秒 = 24 tick */
    private static final int THROW_ANIMATION_TICKS = 24;
    /** 药水瓶生成时刻：throw 动画第 12 tick（0.6 秒出手帧），计数器从 24 递减到 13 的下一 tick */
    private static final int THROW_RELEASE_TICK = 12;

    public RiceWitchEntity(EntityType<? extends RiceWitchEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 属性：30 心 = 60 血（沿用占位数值）、移速 0.25、近战攻击力 4（略高于原版僵尸 3，女巫法系怪）。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    /**
     * AI 目标：0 水上漂浮；2 近战追击（目标贴身时优先，与远程互斥同走 MOVE flag）；
     * 3 远程丢药水（速度 1.0、间隔 50 tick、射程 8 格）；5 避水游荡；6 看玩家；7 张望。
     * 仇恨：1 受击反击；2 主动索敌玩家。
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.0D, 50, 8.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 过渡时间 5 tick（与项目内其他 GeckoLib 实体一致）
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    /**
     * 动画优先级：投药动画 > 近战动画 > 行走 > 待机。
     * setAndContinue 保证同名动画不被逐帧重启，单次动画播完自然切回。
     */
    private PlayState handleAnimState(AnimationState<RiceWitchEntity> state) {
        if (this.entityData.get(DATA_THROW_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_THROW);
        } else if (this.entityData.get(DATA_ATTACK_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_ATTACK);
        } else if (state.isMoving()) {
            state.setAndContinue(ANIM_WALK);
        } else {
            state.setAndContinue(ANIM_IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // -------------------- 攻击：近战 + 丢药水 --------------------

    /** 近战命中目标时触发 attack 动画（服务端伤害结算成功一次，数据自动同步） */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            this.entityData.set(DATA_ATTACK_ANIM_TICKS, ATTACK_ANIMATION_TICKS);
            return true;
        }
        return false;
    }

    /**
     * RangedAttackGoal 决定开火时调用（仅服务端）：只启动投药动画并计时，
     * 药水瓶延迟到动画出手帧（第 12 tick）才真正生成，做到挥手与出手对齐。
     */
    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        this.entityData.set(DATA_THROW_ANIM_TICKS, THROW_ANIMATION_TICKS);
    }

    /**
     * 第 12 tick 朝目标丢一瓶溅射药水（弹道参数对齐原版女巫）：
     * 4 格外随机中毒/缓慢（限制玩家走位），4 格内丢瞬间伤害（贴脸爆发）。
     */
    private void launchSplashPotion(LivingEntity target) {
        Potion potion = Potions.HARMING;
        if (target.distanceToSqr(this) > 16.0D) {
            potion = this.random.nextBoolean() ? Potions.POISON : Potions.SLOWNESS;
        }
        ThrownPotion thrownPotion = new ThrownPotion(this.level(), this);
        thrownPotion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
        double dx = target.getX() - this.getX();
        double dy = target.getY() + target.getEyeHeight() - 1.100000023841858D - thrownPotion.getY();
        double dz = target.getZ() - this.getZ();
        double horizontalDist = Mth.sqrt((float) (dx * dx + dz * dz));
        thrownPotion.shoot(dx, dy + horizontalDist * 0.1F, dz, 0.75F, 8.0F);
        if (!this.isSilent()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WITCH_THROW, this.getSoundSource(),
                    1.0F, 0.8F + this.random.nextFloat() * 0.4F);
        }
        this.level().addFreshEntity(thrownPotion);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            int attackTicks = this.entityData.get(DATA_ATTACK_ANIM_TICKS);
            if (attackTicks > 0) {
                this.entityData.set(DATA_ATTACK_ANIM_TICKS, attackTicks - 1);
            }
            int throwTicks = this.entityData.get(DATA_THROW_ANIM_TICKS);
            if (throwTicks > 0) {
                // 计数器在 13→12 这一刻出手（动画 0.6 秒前挥帧）
                if (throwTicks == THROW_RELEASE_TICK + 1) {
                    LivingEntity target = this.getTarget();
                    if (target != null && target.isAlive()) {
                        launchSplashPotion(target);
                    }
                }
                this.entityData.set(DATA_THROW_ANIM_TICKS, throwTicks - 1);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_ANIM_TICKS, 0);
        this.entityData.define(DATA_THROW_ANIM_TICKS, 0);
    }

    // -------------------- 音效：沿用原版女巫 --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITCH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITCH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITCH_DEATH;
    }
}
