package com.mimiworld.mimiworld.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
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
 * 米莱马（Milaima，注册 id = milai_horse）——大米主题的敌对史莱姆，米浆凝胶成精。
 *
 * <p>模型/贴图/动画资源（均由 Blockbench 导出）：
 * geo/milaima.geo.json、textures/entity/milaima.png、
 * animations/milaima.animation.json（动画键 animation.milaima.idle / walk / attack）。
 * 模型为 38 方块 3 骨骼（root/body/sprout）的圆墩凝胶造型，暖白米浆本体悬浮米粒、
 * 酱油色大眼、梅粉腮红、头顶稻绿嫩芽；渲染整体放大 1.3 倍对齐 1.02×1.02 碰撞箱。</p>
 *
 * <p>生物逻辑：直接继承原版 {@link Slime}——保留史莱姆的随机蹦跳移动、朝目标连续扑跳的
 * 近战 AI 和 Q 弹的 squish 音效（与米浆凝胶质感天然契合）。固定为 2 号体型（1.02×1.02）、
 * 12 心、近战接触 4 点伤害；与原版史莱姆的差异：
 * <ul>
 *   <li>死亡不分裂成小史莱姆（{@link #die} 前把体型降为 1，绕过分裂逻辑），</li>
 *   <li>不掉原版黏液球（{@link #dropCustomDeathLoot} 置空），掉落走 mod 战利品表
 *       （大米 + 米莱马球，见 ModLootTableProvider）；</li>
 *   <li>朝敌人发起攻击蹦跳时触发一次单次 attack 扑击动画（{@link #jumpFromGround}）。</li>
 * </ul>
 * 正常蹦跳（位移中）播 walk 蹦跳循环，静止播 idle 呼吸浮动。</p>
 */
public class MilaimaEntity extends Slime implements GeoEntity {

    /** 米莱马固定的史莱姆体型编号：2 号 → 碰撞箱 1.02×1.02，接触伤害在 {@link #getAttackDamage} 覆写 */
    private static final int SLIME_SIZE = 2;

    /**
     * 同步数据：攻击动画剩余 tick（服务端在攻击蹦跳时置 16 并逐 tick 递减，自动同步到客户端；
     * GeckoLib 动画在客户端播放，必须用 SynchedEntityData 而不能用普通字段）。
     * 做法与没米入/糊米古尸等 GeckoLib 实体一致。
     */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(MilaimaEntity.class, EntityDataSerializers.INT);

    // GeckoLib：每个实体实例持有一个动画状态缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 待机：milaima.animation.json 中的 "animation.milaima.idle"（loop=true，2 秒，米浆呼吸+嫩芽摇摆）
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.milaima.idle");
    // 蹦跳行走："animation.milaima.walk"（loop=true，1.2 秒，蓄力压扁→腾空拉伸→落地压扁→果冻回弹）
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.milaima.walk");
    // 攻击："animation.milaima.attack"（不循环，单次 0.8 秒，后仰蓄力→向前猛扑 1.8 格→砸地回弹）
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.milaima.attack");

    /** attack 动画时长 0.8 秒 = 16 tick */
    private static final int ATTACK_ANIMATION_TICKS = 16;

    public MilaimaEntity(EntityType<? extends Slime> type, Level level) {
        super(type, level);
        // 史莱姆构造后默认是 1 号体型，这里固定为 2 号（不重置血量，此时本就满血）
        this.setSize(SLIME_SIZE, false);
    }

    /**
     * 属性：在原版史莱姆基础上把血量提到 12 心 = 24 血（沿用占位数值）、移速略提到 0.28
     * （蹦跳更积极），攻击属性登记 4 点（实际接触伤害以 {@link #getAttackDamage()} 为准）。
     * 在 ModEntityAttributes 的 EntityAttributeCreationEvent 中注册。
     */
    public static AttributeSupplier.Builder createAttributes() {
        // 史莱姆自身不提供 createAttributes，直接用怪物属性基类（含攻击/追击），等价原版史莱姆属性
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    /** 接触伤害：原版 2 号史莱姆只有 2 点，提到 4 点（2 心），与攻击属性一致。 */
    @Override
    protected float getAttackDamage() {
        return 4.0F;
    }

    // -------------------- GeckoLib 动画 --------------------
    //
    // 蹦跳移动、朝玩家扑跳的近战 AI、随机转向、漂浮、squish 音效全部继承 Slime，
    // 这里不覆写 registerGoals。

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 过渡时间 5 tick（与项目内其他 GeckoLib 实体一致）
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    /**
     * 动画状态：攻击扑跳后的 16 tick 内播单次 attack；否则位移中播 walk 蹦跳循环、静止播 idle。
     * setAndContinue 保证同名动画不会被逐帧重启，attack 单次播完自然切回。
     */
    private PlayState handleAnimState(AnimationState<MilaimaEntity> state) {
        if (this.entityData.get(DATA_ATTACK_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_ATTACK);
            return PlayState.CONTINUE;
        }
        if (state.isMoving()) {
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

    /**
     * 蹦跳起跳瞬间：目标在 4 格内时视为攻击扑跳，触发一次 attack 动画。
     * 史莱姆的跳跃由其 SlimeMoveControl 在 AI 决策后驱动，普通游荡蹦跳不触发。
     */
    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        if (!this.level().isClientSide) {
            LivingEntity target = this.getTarget();
            if (target != null && this.entityData.get(DATA_ATTACK_ANIM_TICKS) == 0
                    && this.distanceToSqr(target) < 16.0D) {
                this.entityData.set(DATA_ATTACK_ANIM_TICKS, ATTACK_ANIMATION_TICKS);
            }
        }
    }

    /** 兜底：其他途径结算近战命中（如外部伤害流程）时也触发攻击动画。 */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            this.entityData.set(DATA_ATTACK_ANIM_TICKS, ATTACK_ANIMATION_TICKS);
            return true;
        }
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // 仅服务端倒计时并驱动同步，客户端每 tick 收到 16→0 的新值
        if (!this.level().isClientSide) {
            int ticks = this.entityData.get(DATA_ATTACK_ANIM_TICKS);
            if (ticks > 0) {
                this.entityData.set(DATA_ATTACK_ANIM_TICKS, ticks - 1);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_ANIM_TICKS, 0);
    }

    /** 从存档读取后仍固定回 2 号体型（米莱马不分裂，体型恒定）。 */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setSize(SLIME_SIZE, false);
    }

    /**
     * 死亡不分裂：原版史莱姆在 die() 中检测 size>1 会分裂出同类小怪。
     * 先走父类死亡流程前把体型降为 1（不重置血量），绕过分裂；掉落仍走 mod 战利品表。
     */
    @Override
    public void die(DamageSource cause) {
        this.setSize(1, false);
        super.die(cause);
    }

    /** 不掉原版黏液球——米莱马的掉落（大米 + 米莱马球）由 mod 实体战利品表提供。 */
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
    }
}
