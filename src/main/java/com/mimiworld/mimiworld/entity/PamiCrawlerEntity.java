package com.mimiworld.mimiworld.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

/**
 * 爪巴米者（Pami Crawler，注册 id = pami_crawler）——旧米麻袋成精的苦力怕，四足爬行自爆怪。
 *
 * <p>模型/贴图/动画资源（均由 Blockbench 导出）：
 * geo/pami_crawler.geo.json、textures/entity/pami_crawler.png、
 * animations/pami_crawler.animation.json（动画键 animation.idle / animation.walk / animation.attack）。
 * 模型为 30 方块 7 骨骼 Q 版四足（root→body→head + 四条独立腿骨），大米主题：
 * 旧黄麻麻袋身、米袋束口、白胖米粒、破麻袋补丁、饭团小尾、头顶稻芽、苦力怕苦脸。
 * 碰撞箱 0.7×0.8（沿用占位尺寸）。</p>
 *
 * <p>生物逻辑完全对齐原版苦力怕：自身不会近战，而是在目标进入 3 格内点燃引信
 * （{@link #SWELL_DURATION}=30 tick=1.5 秒，与 Blockbench 中 animation.attack 时长一致），
 * 目标脱离 7 格或被甩开时引信快速熄灭；引信走满即自爆（半径同苦力怕 3.0，受 mobGriefing 规则约束）。
 * 12 心（24 点血，沿用占位数值），移速 0.25，音效使用原版苦力怕音效。</p>
 */
public class PamiCrawlerEntity extends Monster implements GeoEntity {

    /** 引信总时长 30 tick = 1.5 秒（对齐原版苦力怕 maxSwell，也正好等于 attack 动画长度） */
    public static final int SWELL_DURATION = 30;
    /** 爆炸半径：与原版苦力怕相同（本 mod 暂不做闪电附魔变大的蓝毛苦力怕） */
    public static final float EXPLOSION_RADIUS = 3.0F;
    /** 点燃引信的距离平方（3 格），对齐原版苦力怕 */
    private static final double SWELL_START_DISTANCE_SQR = 9.0D;
    /** 引信熄灭的距离平方（7 格），对齐原版苦力怕 SwellGoal */
    private static final double SWELL_STOP_DISTANCE_SQR = 49.0D;

    /**
     * 同步数据：当前引信进度（0..30）。服务端在 customServerAiStep 中推进，
     * 客户端既驱动 GeckoLib 的 attack 动画，也驱动渲染器的白色鼓胀闪烁。
     */
    private static final EntityDataAccessor<Integer> DATA_SWELL =
            SynchedEntityData.defineId(PamiCrawlerEntity.class, EntityDataSerializers.INT);

    // GeckoLib：每个实体实例持有一个动画状态缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 待机："animation.idle"（loop=true，4 秒，呼吸+重心慢移+脑袋张望）
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.idle");
    // 行走："animation.walk"（loop=true，1 秒，四足对角线步态+身体扭摆）
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.walk");
    // 引爆："animation.attack"（不循环，1.5 秒=30 tick，警觉→蹲伏→三鼓脉冲→极限鼓胀定格）
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.attack");

    // 客户端渲染用：上一 tick 与当前 tick 的引信进度，用于帧间插值闪白
    private int prevSwell;
    private int swell;

    public PamiCrawlerEntity(EntityType<? extends PamiCrawlerEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 属性：12 心 = 24 血（沿用占位数值）、移速 0.25（对齐原版苦力怕）。
     * 无近战攻击力——它的攻击方式只有自爆。在 ModEntityAttributes 中注册。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    /**
     * AI 目标：0 水上漂浮；1 引信停步（进入 3 格后不再移动，原地点燃麻袋引信）；
     * 2 避水随机游荡；3 看向玩家；4 四处张望。
     * 仇恨：1 受击反击；2 主动索敌玩家。
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SwellGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

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
     * 动画状态：引信点燃期间（swell>0）播单次 attack（鼓胀引信）；
     * 否则移动播 walk（四足爬行）、静止播 idle。
     * setAndContinue 保证同名动画不会被逐帧重启，attack 播完停在鼓胀定格帧，此时正好爆炸。
     */
    private PlayState handleAnimState(AnimationState<PamiCrawlerEntity> state) {
        if (this.entityData.get(DATA_SWELL) > 0) {
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

    // -------------------- 苦力怕式引信状态机 --------------------

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SWELL, 0);
    }

    /** 当前引信进度（0..30），服务端逻辑/客户端动画共用 */
    public int getSwell() {
        return this.entityData.get(DATA_SWELL);
    }

    /** 引信是否在燃烧（动画状态机与停步 AI 都用它判断） */
    public boolean isSwelling() {
        return this.entityData.get(DATA_SWELL) > 0;
    }

    /**
     * 引信进度 0..1（渲染器白色鼓胀闪烁用），客户端按帧间插值。
     */
    public float getSwelling(float partialTick) {
        return Mth.lerp(partialTick, this.prevSwell, this.swell) / (float) SWELL_DURATION;
    }

    /**
     * 客户端：每 tick 把同步下来的引信进度拷贝到本地插值字段
     * （super.tick 前记录旧值，数据包在此前已由网络线程应用）。
     */
    @Override
    public void tick() {
        this.prevSwell = this.swell;
        super.tick();
        if (this.level().isClientSide) {
            this.swell = this.entityData.get(DATA_SWELL);
        }
    }

    /**
     * 仅服务端调用的 AI 步进：推进/熄灭引信，走满就自爆。逻辑与原版苦力怕一致——
     * 目标在 3 格内逐 tick 增加，否则每 tick 减 2（灭得比点得快），钳制在 0..30。
     */
    @Override
    protected void customServerAiStep() {
        LivingEntity target = this.getTarget();
        int swell = this.getSwell();
        if (target != null && this.distanceToSqr(target) < SWELL_START_DISTANCE_SQR) {
            if (swell == 0) {
                this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
            }
            swell++;
        } else if (swell > 0) {
            swell -= 2;
        }
        swell = Mth.clamp(swell, 0, SWELL_DURATION);
        this.entityData.set(DATA_SWELL, swell);
        this.swell = swell;

        if (swell >= SWELL_DURATION && !this.isRemoved()) {
            this.explode();
        }
    }

    /**
     * 自爆：调用原版爆炸（ExplosionInteraction.MOB 受 /gamerule mobGriefing 约束），随后消失。
     */
    private void explode() {
        if (!this.level().isClientSide) {
            this.level().explode(this, this.getX(), this.getY(0.0625D), this.getZ(),
                    EXPLOSION_RADIUS, ExplosionInteraction.MOB);
            this.discard();
        }
    }

    // -------------------- 音效：沿用原版苦力怕 --------------------

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CREEPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREEPER_DEATH;
    }

    /**
     * 引信停步 AI（对应原版苦力怕的内部 SwellGoal）：目标进入 3 格后站住不动点燃引信，
     * 目标脱离 7 格或引信熄灭后恢复游荡。
     */
    public static class SwellGoal extends Goal {
        private final PamiCrawlerEntity crawler;

        public SwellGoal(PamiCrawlerEntity crawler) {
            this.crawler = crawler;
            // 占用 MOVE 标志：引信期间其他移动类 AI（随机游荡）不能同时跑
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.crawler.getTarget();
            return target != null && this.crawler.distanceToSqr(target) < 9.0D;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.crawler.getTarget();
            if (target == null || this.crawler.distanceToSqr(target) > 49.0D) {
                return false;
            }
            return this.crawler.isSwelling();
        }

        @Override
        public void start() {
            this.crawler.getNavigation().stop();
        }

        @Override
        public void tick() {
            // 盯住目标站住：持续清掉寻路移动，让 customServerAiStep 安心推引信
            this.crawler.getNavigation().stop();
        }
    }
}
