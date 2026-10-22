package com.mimiworld.mimiworld.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * 第八宗罪龙（Dibazongzui Dragon，注册 id = dibazongzuilong）——最终 Boss，龙系超巨型敌对 Boss，
 * 体型为最懒终末龙的 1.5 倍，技能复用最懒终末龙（数值冻结）。
 *
 * <p>GeckoLib 资源（Blockbench 导出，451 方块/32 骨骼/2048 像素贴图）：
 * geo/dibazongzuilong.geo.json、textures/entity/dibazongzuilong.png、
 * animations/dibazongzuilong.animation.json，含六个动画：
 * circling（盘旋循环）/ dive_strike（冲撞）/ dragon_breath（龙息）/
 * laser_beam（光束循环）/ melee_bite（撕咬普攻）/ hurt_roar（受伤嘶吼）。</p>
 *
 * <p>技能（数值冻结）：</p>
 * <ol>
 *   <li><b>龙息</b>（CD 30s）：蓄力后在目标处生成原版末影龙紫色龙息云，
 *       喷吐命中瞬间造成 50 点无视护甲伤害（动画第 17 tick = 0.85s）。</li>
 *   <li><b>巨龙冲撞</b>（CD 60s）：锁定玩家，0.5s 扬翅蓄力后脚本化高速直线冲撞，
 *       动画第 22 tick（1.1s）命中帧对沿途玩家造成 150 点伤害与强力击退。</li>
 *   <li><b>最懒光束</b>（CD 120s）：锁定一名玩家从嘴部持续射出紫色光束 60 秒，
 *       每 0.5s 结算一次 500 点无视护甲伤害；被锁定玩家死亡立即自动切换下一名玩家，
 *       直到 60s 结束；光束会被方块遮挡。</li>
 *   <li><b>普通撕咬</b>：近身时猛咬，走 ATTACK_DAMAGE=100 的标准近战结算，并挂多重负面效果。</li>
 * </ol>
 *
 * <p>常驻无重力飞行（{@link FlyingMoveControl} + {@link FlyingPathNavigation}）、火焰免疫、
 * 免疫击退、紫色 Boss 血条、末影龙音效。2500 血、100 甲。</p>
 */
public class DibazongzuilongEntity extends PathfinderMob implements GeoEntity, Enemy {

    // -------------------- 动画状态同步（服务端递减，自动同步客户端） --------------------

    private static final EntityDataAccessor<Integer> DATA_BREATH_ANIM_TICKS =
            SynchedEntityData.defineId(DibazongzuilongEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CHARGE_ANIM_TICKS =
            SynchedEntityData.defineId(DibazongzuilongEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BEAM_ANIM_TICKS =
            SynchedEntityData.defineId(DibazongzuilongEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BITE_ANIM_TICKS =
            SynchedEntityData.defineId(DibazongzuilongEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HURT_ANIM_TICKS =
            SynchedEntityData.defineId(DibazongzuilongEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_CIRCLING = RawAnimation.begin().thenLoop("animation.circling");
    private static final RawAnimation ANIM_BREATH = RawAnimation.begin().thenPlay("animation.dragon_breath");
    private static final RawAnimation ANIM_CHARGE = RawAnimation.begin().thenPlay("animation.dive_strike");
    private static final RawAnimation ANIM_BEAM = RawAnimation.begin().thenLoop("animation.laser_beam");
    private static final RawAnimation ANIM_BITE = RawAnimation.begin().thenPlay("animation.melee_bite");
    private static final RawAnimation ANIM_HURT = RawAnimation.begin().thenPlay("animation.hurt_roar");

    // -------------------- 技能数值 --------------------

    /** 龙息：动画 2.45s=49tick，第 17tick（0.85s）张口喷射；CD 30s=600tick；每跳 30 伤害 */
    private static final int BREATH_ANIMATION_TICKS = 49;
    private static final int BREATH_HIT_TICK = 17;
    private static final int BREATH_COOLDOWN_TICKS = 600;
    private static final float BREATH_DAMAGE = 30.0F;
    private static final double BREATH_RANGE_SQR = 32.0D * 32.0D;
    private static final float BREATH_CLOUD_RADIUS = 4.5F;
    private static final int BREATH_CLOUD_DURATION = 120;
    /** 龙息云伤害跳间隔（tick）：20tick=1s，对齐原版末影龙龙息云的结算节奏 */
    private static final int BREATH_CLOUD_HIT_INTERVAL = 20;

    /** 冲撞：动画 2.3s=46tick，10tick 蓄力，第 22tick（1.1s）命中；CD 60s=1200tick；60 伤害 */
    private static final int CHARGE_ANIMATION_TICKS = 46;
    private static final int CHARGE_WINDUP_TICKS = 10;
    private static final int CHARGE_HIT_TICK = 22;
    private static final int CHARGE_DASH_END_TICK = 34;
    private static final int CHARGE_COOLDOWN_TICKS = 1200;
    private static final float CHARGE_DAMAGE = 60.0F;
    private static final double CHARGE_HIT_RADIUS = 4.5D;
    /** 冲撞位移：格/tick（2.4×24tick ≈ 58 格冲刺距离，约 48 格/s，为龙最快动作） */
    private static final double CHARGE_SPEED = 2.4D;

    /** 最懒光束：持续 60s=1200tick，每 10tick（0.5s）造成 20；CD 120s=2400tick */
    private static final int BEAM_DURATION_TICKS = 1200;
    private static final int BEAM_COOLDOWN_TICKS = 2400;
    private static final float BEAM_DAMAGE = 20.0F;
    private static final int BEAM_HIT_INTERVAL = 10;
    private static final double BEAM_RANGE = 48.0D;
    /** 悬停照射点与锁定玩家的水平距离（格），垂直高度在 Goal 内取目标上方 9 格 */
    private static final double BEAM_FIRE_DIST = 16.0D;

    /** 普攻撕咬：动画 1.45s=29tick，第 16tick（0.78s）咬合；20 伤害 */
    private static final int BITE_ANIMATION_TICKS = 29;
    private static final int BITE_HIT_TICK = 16;
    private static final int BITE_COOLDOWN_TICKS = 45;
    private static final double BITE_RANGE_SQR = 5.0D * 5.0D;

    /** 受伤嘶吼动画 1.8s=36tick */
    private static final int HURT_ANIMATION_TICKS = 36;

    /** 首次技能前的准备时间，避免出生瞬间立刻出招 */
    private static final int INITIAL_BREATH_DELAY = 120;
    private static final int INITIAL_CHARGE_DELAY = 260;
    private static final int INITIAL_BEAM_DELAY = 480;

    /** 三个无视护甲的自定义伤害类型（data/mimiworld/damage_type/） */
    private static final ResourceKey<DamageType> BREATH_DAMAGE_TYPE_KEY =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("mimiworld", "dragon_breath"));
    private static final ResourceKey<DamageType> CHARGE_DAMAGE_TYPE_KEY =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("mimiworld", "dragon_charge"));
    private static final ResourceKey<DamageType> BEAM_DAMAGE_TYPE_KEY =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("mimiworld", "dragon_beam"));

    // 技能冷却（仅服务端计时）
    private int breathCooldown = INITIAL_BREATH_DELAY;
    private int chargeCooldown = INITIAL_CHARGE_DELAY;
    private int beamCooldown = INITIAL_BEAM_DELAY;
    private int biteCooldown;

    /** 当前场上仍在持续造成伤害的龙息云（仅服务端维护，云消失后自动摘除） */
    private final List<ActiveBreathCloud> activeBreathClouds = new ArrayList<>();

    /** 紫色 Boss 血条（服务端维护，自动同步给看到它的玩家） */
    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    public DibazongzuilongEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        // 常驻飞行：无重力 + 飞行移动控制器（转向灵活度 10，同 Ghast）。
        // hoversInPlace 必须为 true：FlyingMoveControl 在没有移动指令的 tick 会调用
        // setNoGravity(false)，传 false 会让龙在悬停/刹停 tick 反复下坠抖动，显得又笨又慢。
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        // 飞龙可穿过非固体危险方块，不应被火/岩浆/水卡住或惩罚
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, -1.0F);
    }

    /**
     * 属性：2500 血、普攻 100、飞行加速 4.5、索敌 64、100 甲 6 韧性、免疫击退。
     * 飞行生物空中稳态速度 ≈ 0.222 × MoveControl速度系数 × FLYING_SPEED（格/tick），
     * 4.5 配合盘旋 1.15 系数约 23 格/s、掠飞 2.2 系数约 44 格/s，动作迅猛、压迫感强。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FLYING_SPEED, 4.5D)
                .add(Attributes.ATTACK_DAMAGE, 100.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ARMOR, 100.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    /** 飞行生物使用空中直线寻路（节点评估器允许悬空点位） */
    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    /**
     * 撕咬命中结算：给自身套上短暂的高额生命恢复，并给被击目标挂上
     * 虚弱 I、失明、反胃与缓慢 IV 多重负面效果。
     */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 1));
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0));
                living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
                living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
            }
            return true;
        }
        return false;
    }

    /** 常驻自增益：生命恢复 III + 力量 IV（服务端 aiStep 中调用，效果过期后自动续接）。 */
    private void maintainPassiveBuffs() {
        if (!this.hasEffect(MobEffects.REGENERATION)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 2));
        }
        if (!this.hasEffect(MobEffects.DAMAGE_BOOST)) {
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 3));
        }
    }

    /**
     * AI 优先级（所有攻击/飞行类 Goal 都占用 MOVE+LOOK，GoalSelector 天然保证互斥）：
     * 1 光束（60s 持续悬停照射）；2 冲撞（俯冲）；3 龙息（悬停吐息）；4 近身撕咬；
     * 5 无技能时的末影龙式高空大圈盘旋（含掠飞与悬停）；6 无目标高空漫游；7/8 注视张望。
     * 技能在盘旋到距玩家较近或悬停窗口自然触发，节奏接近原版末影龙。
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new DibazongzuiBeamGoal());
        this.goalSelector.addGoal(2, new DragonChargeGoal());
        this.goalSelector.addGoal(3, new DragonBreathGoal());
        this.goalSelector.addGoal(4, new MeleeBiteGoal());
        this.goalSelector.addGoal(5, new DragonOrbitFlightGoal());
        this.goalSelector.addGoal(6, new DragonAirWanderGoal());
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // 全天敌对：受击必反击 + 主动索敌玩家
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 过渡时间 5 tick：技能之间平滑混合
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    /** 技能动画优先级：光束 > 龙息 > 冲撞 > 撕咬 > 受伤嘶吼 > 盘旋（常态飞行）。 */
    private PlayState handleAnimState(AnimationState<DibazongzuilongEntity> state) {
        if (this.entityData.get(DATA_BEAM_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_BEAM);
        } else if (this.entityData.get(DATA_BREATH_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_BREATH);
        } else if (this.entityData.get(DATA_CHARGE_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_CHARGE);
        } else if (this.entityData.get(DATA_BITE_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_BITE);
        } else if (this.entityData.get(DATA_HURT_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_HURT);
        } else {
            state.setAndContinue(ANIM_CIRCLING);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** 是否正在释放技能（光束/冲撞/龙息/撕咬），技能期间锁住其他攻击 Goal。 */
    private boolean isCasting() {
        return this.entityData.get(DATA_BEAM_ANIM_TICKS) > 0
                || this.entityData.get(DATA_CHARGE_ANIM_TICKS) > 0
                || this.entityData.get(DATA_BREATH_ANIM_TICKS) > 0
                || this.entityData.get(DATA_BITE_ANIM_TICKS) > 0;
    }

    // -------------------- Boss 血条 / 同步数据 / 计时 --------------------

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BREATH_ANIM_TICKS, 0);
        this.entityData.define(DATA_CHARGE_ANIM_TICKS, 0);
        this.entityData.define(DATA_BEAM_ANIM_TICKS, 0);
        this.entityData.define(DATA_BITE_ANIM_TICKS, 0);
        this.entityData.define(DATA_HURT_ANIM_TICKS, 0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.maintainPassiveBuffs();
            if (this.breathCooldown > 0) {
                this.breathCooldown--;
            }
            if (this.chargeCooldown > 0) {
                this.chargeCooldown--;
            }
            if (this.beamCooldown > 0) {
                this.beamCooldown--;
            }
            if (this.biteCooldown > 0) {
                this.biteCooldown--;
            }
            this.decAnim(DATA_BREATH_ANIM_TICKS);
            this.decAnim(DATA_CHARGE_ANIM_TICKS);
            this.decAnim(DATA_BEAM_ANIM_TICKS);
            this.decAnim(DATA_BITE_ANIM_TICKS);
            this.decAnim(DATA_HURT_ANIM_TICKS);
            this.tickBreathClouds();
        }
    }

    /**
     * 龙息云持续伤害（对齐原版末影龙龙息体验）：云存活期间每
     * {@value #BREATH_CLOUD_HIT_INTERVAL}tick（1s）对雾内玩家结算一次，
     * 每跳都是 {@value #BREATH_DAMAGE} 点无视护甲伤害；伤害范围跟随云当前（收缩中的）半径。
     */
    private void tickBreathClouds() {
        if (this.activeBreathClouds.isEmpty()) {
            return;
        }
        Iterator<ActiveBreathCloud> iterator = this.activeBreathClouds.iterator();
        while (iterator.hasNext()) {
            ActiveBreathCloud active = iterator.next();
            AreaEffectCloud cloud = active.cloud;
            active.ticks++;
            if (cloud.isRemoved() || active.ticks > BREATH_CLOUD_DURATION) {
                iterator.remove();
                continue;
            }
            if (active.ticks % BREATH_CLOUD_HIT_INTERVAL != 0) {
                continue;
            }
            float radius = cloud.getRadius();
            AABB hitArea = new AABB(
                    cloud.getX() - radius, cloud.getY() - 2.5D, cloud.getZ() - radius,
                    cloud.getX() + radius, cloud.getY() + 2.5D, cloud.getZ() + radius);
            List<Player> players = this.level().getEntitiesOfClass(Player.class, hitArea,
                    DibazongzuilongEntity::validTarget);
            for (Player player : players) {
                player.hurt(this.skillDamageSource(BREATH_DAMAGE_TYPE_KEY), BREATH_DAMAGE);
            }
        }
    }

    /** 活跃龙息云引用：云实体 + 自生成起的 tick 计数（独立计时，不依赖云内部字段）。 */
    private static final class ActiveBreathCloud {
        private final AreaEffectCloud cloud;
        private int ticks;

        private ActiveBreathCloud(AreaEffectCloud cloud) {
            this.cloud = cloud;
        }
    }

    private void decAnim(EntityDataAccessor<Integer> key) {
        int ticks = this.entityData.get(key);
        if (ticks > 0) {
            this.entityData.set(key, ticks - 1);
        }
    }

    /** 受伤且未在出招时触发一次嘶吼动画（技能不被受击打断）。 */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean flag = super.hurt(source, amount);
        if (flag && !this.level().isClientSide && !this.isCasting()
                && this.entityData.get(DATA_HURT_ANIM_TICKS) <= 0) {
            this.entityData.set(DATA_HURT_ANIM_TICKS, HURT_ANIMATION_TICKS);
        }
        return flag;
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        // 每服务端 tick 同步血条进度（末影龙同款 BossBar 行为）
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        super.customServerAiStep();
    }

    // -------------------- 通用工具 --------------------

    /** 构造技能用的无视护甲伤害源（causing entity = 龙，死亡统计/仇恨正确归属）。 */
    private DamageSource skillDamageSource(ResourceKey<DamageType> key) {
        Holder<DamageType> holder = this.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(key);
        return new DamageSource(holder, this);
    }

    /** 可被攻击的玩家判定：存活、非旁观、非创造。 */
    private static boolean validTarget(Player player) {
        return player != null && player.isAlive() && !player.isSpectator() && !player.isCreative();
    }

    /** 平滑转向并直视目标（含俯仰，供光束/龙息瞄准）。 */
    private void faceTarget(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double dy = (target.getY() + target.getBbHeight() * 0.5D)
                - (this.getY() + this.getBbHeight() * 0.5D);
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, horiz)));
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.setYHeadRot(yaw);
        this.setXRot(Mth.clamp(pitch, -60.0F, 60.0F));
    }

    /** 龙嘴在模型空间相对 root（=实体脚点）的水平前伸量（颌 pivot z=-255px/16）。 */
    private static final float MOUTH_FORWARD = 15.9F;
    /** 龙嘴在模型空间相对脚点的高度（颌 pivot y=138px/16，激光姿态略低）。 */
    private static final float MOUTH_UP = 8.6F;

    /**
     * 龙嘴世界坐标（含帧间插值，客户端渲染光束用）。模型朝向 -Z，GeckoLib 已做 180° 对齐，
     * 实体朝向即模型头部方向；头颅位于 root 前方约 15.9 格、上方约 8.6 格，
     * 再沿当前视线（含俯仰）前移 1.2 格，正好落在张开的双颌之间。
     */
    public Vec3 getMouthPosition(float partialTick) {
        Vec3 feet = this.getPosition(partialTick);
        float yaw = this.getViewYRot(partialTick);
        float pitch = this.getViewXRot(partialTick);
        Vec3 horizontalForward = Vec3.directionFromRotation(0.0F, yaw);
        Vec3 look = Vec3.directionFromRotation(pitch, yaw);
        return feet.add(0.0D, MOUTH_UP, 0.0D)
                .add(horizontalForward.scale(MOUTH_FORWARD))
                .add(look.scale(1.2D));
    }

    /** 龙嘴世界坐标（服务端技能判定用）。 */
    private Vec3 mouthPosition() {
        return this.getMouthPosition(1.0F);
    }

    /** 刹停空中惯性（蓄力/喷吐定点时用）。 */
    private void brakeInAir() {
        this.navigation.stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.55D, 0.8D, 0.55D));
    }

    // -------------------- 音效：末影龙 --------------------

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_GROWL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    // -------------------- 技能 1：龙息 --------------------

    /**
     * 【龙息】CD 30s：2.45s 动画内，前 0.85s 仰头蓄力，第 17tick 朝锁定玩家当前位置
     * 生成原版末影龙紫色龙息云（持续 6s 紫雾，半径 4.5 逐渐收缩到 1）。雾区不是只扣一次血，
     * 而是和原版龙息一样持续结算：留在雾内每 1s 受一次 50 点无视护甲伤害（共 6 跳），
     * 逼玩家立刻走出紫雾。
     */
    private class DragonBreathGoal extends Goal {
        private int runTicks;

        DragonBreathGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = DibazongzuilongEntity.this.getTarget();
            return DibazongzuilongEntity.this.breathCooldown <= 0
                    && !DibazongzuilongEntity.this.isCasting()
                    && target instanceof Player player && validTarget(player)
                    && DibazongzuilongEntity.this.distanceToSqr(target) <= BREATH_RANGE_SQR
                    && DibazongzuilongEntity.this.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = DibazongzuilongEntity.this.getTarget();
            return this.runTicks < BREATH_ANIMATION_TICKS && target != null && target.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            DibazongzuilongEntity.this.brakeInAir();
            DibazongzuilongEntity.this.entityData.set(DATA_BREATH_ANIM_TICKS, BREATH_ANIMATION_TICKS);
            DibazongzuilongEntity.this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 2.0F, 0.7F);
        }

        @Override
        public void tick() {
            this.runTicks++;
            DibazongzuilongEntity dragon = DibazongzuilongEntity.this;
            dragon.brakeInAir();
            LivingEntity target = dragon.getTarget();
            if (target != null) {
                dragon.faceTarget(target);
            }
            if (this.runTicks == BREATH_HIT_TICK) {
                dragon.performDragonBreath(target);
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            DibazongzuilongEntity.this.breathCooldown = BREATH_COOLDOWN_TICKS;
        }
    }

    /** 生成末影龙龙息云并登记，云的持续多跳 50 伤害由 {@link #tickBreathClouds()} 每秒结算。 */
    private void performDragonBreath(LivingEntity target) {
        this.playSound(SoundEvents.ENDER_DRAGON_SHOOT, 2.0F, 1.0F);
        double cx = target.getX();
        double cy = target.getY() + 0.6D;
        double cz = target.getZ();

        // 原版末影龙同款紫色龙息雾（不挂药水效果，每跳 50 点伤害由本 Boss 统一结算）
        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), cx, cy, cz);
        cloud.setRadius(BREATH_CLOUD_RADIUS);
        cloud.setRadiusPerTick((1.0F - BREATH_CLOUD_RADIUS) / BREATH_CLOUD_DURATION);
        cloud.setWaitTime(0);
        cloud.setDuration(BREATH_CLOUD_DURATION);
        cloud.setParticle(ParticleTypes.DRAGON_BREATH);
        cloud.setOwner(this);
        this.level().addFreshEntity(cloud);
        this.activeBreathClouds.add(new ActiveBreathCloud(cloud));

        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 40; i++) {
                double angle = this.random.nextDouble() * Math.PI * 2.0D;
                double r = this.random.nextDouble() * BREATH_CLOUD_RADIUS;
                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                        cx + Math.cos(angle) * r, cy + this.random.nextDouble() * 1.6D,
                        cz + Math.sin(angle) * r, 1, 0.0D, 0.06D, 0.0D, 0.0D);
            }
        }
    }

    // -------------------- 技能 2：巨龙冲撞 --------------------

    /**
     * 【巨龙冲撞】CD 60s：前 10tick 扬翅蓄力锁定（带目标速度提前量），
     * 11~34tick 沿固定方向以 2.4 格/tick 高速直线冲撞（脚本位移，撞墙提前结束），
     * 第 22tick（动画俯冲最低点）命中帧结算 150 伤害+强击退，35~46tick 拉起恢复。
     */
    private class DragonChargeGoal extends Goal {
        private int runTicks;
        private Vec3 dashDir = Vec3.ZERO;
        private boolean hitDone;

        DragonChargeGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = DibazongzuilongEntity.this.getTarget();
            return DibazongzuilongEntity.this.chargeCooldown <= 0
                    && !DibazongzuilongEntity.this.isCasting()
                    && target instanceof Player player && validTarget(player)
                    && DibazongzuilongEntity.this.distanceToSqr(target) <= 40.0D * 40.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return this.runTicks < CHARGE_ANIMATION_TICKS;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            this.hitDone = false;
            this.dashDir = Vec3.ZERO;
            DibazongzuilongEntity dragon = DibazongzuilongEntity.this;
            dragon.brakeInAir();
            dragon.entityData.set(DATA_CHARGE_ANIM_TICKS, CHARGE_ANIMATION_TICKS);
            dragon.playSound(SoundEvents.ENDER_DRAGON_GROWL, 2.2F, 0.55F);
        }

        @Override
        public void tick() {
            this.runTicks++;
            DibazongzuilongEntity dragon = DibazongzuilongEntity.this;
            LivingEntity target = dragon.getTarget();

            if (this.runTicks <= CHARGE_WINDUP_TICKS) {
                // 蓄力：空中刹停、压低身位、死盯目标
                dragon.brakeInAir();
                if (target != null) {
                    dragon.faceTarget(target);
                }
                if (this.runTicks == CHARGE_WINDUP_TICKS && target != null) {
                    // 锁定冲点：目标当前位置 + 速度提前量（约 12tick）
                    Vec3 aim = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                            .add(target.getDeltaMovement().scale(12.0D));
                    Vec3 start = dragon.position().add(0.0D, dragon.getBbHeight() * 0.5D, 0.0D);
                    this.dashDir = aim.subtract(start);
                    if (this.dashDir.lengthSqr() < 1.0E-4D) {
                        this.dashDir = dragon.getLookAngle();
                    }
                    this.dashDir = this.dashDir.normalize();
                    float yaw = (float) (Math.toDegrees(Math.atan2(this.dashDir.z, this.dashDir.x)) - 90.0D);
                    dragon.setYRot(yaw);
                    dragon.yBodyRot = yaw;
                    dragon.setYHeadRot(yaw);
                }
            } else if (this.runTicks <= CHARGE_DASH_END_TICK) {
                // 高速直线冲撞：脚本位移 + 碰撞检测（撞墙立刻进入拉起）
                // 碰撞箱已扩到整条龙翼展，探墙改用龙体核心 3×3 小箱，避免贴地/掠林时整箱误判撞墙
                Vec3 move = this.dashDir.scale(CHARGE_SPEED);
                AABB nextBox = new AABB(
                        dragon.getX() - 1.5D, dragon.getY(), dragon.getZ() - 1.5D,
                        dragon.getX() + 1.5D, dragon.getY() + dragon.getBbHeight(), dragon.getZ() + 1.5D).move(move);
                if (dragon.level().noCollision(dragon, nextBox)) {
                    dragon.setPos(dragon.getX() + move.x, dragon.getY() + move.y, dragon.getZ() + move.z);
                    dragon.setDeltaMovement(move);
                } else {
                    this.runTicks = Math.max(this.runTicks, CHARGE_DASH_END_TICK);
                    dragon.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.6F, 0.6F);
                }

                // 身体拖出的紫色残影
                if (dragon.level() instanceof ServerLevel serverLevel && this.runTicks % 2 == 0) {
                    serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                            dragon.getX(), dragon.getY() + dragon.getBbHeight() * 0.5D, dragon.getZ(),
                            6, 2.0D, dragon.getBbHeight() * 0.35D,
                            2.0D, 0.02D);
                }

                if (!this.hitDone && this.runTicks >= CHARGE_HIT_TICK) {
                    this.hitDone = true;
                    dragon.performChargeHit();
                }
            } else {
                // 拉起恢复：减速、沿冲撞方向上方爬升
                Vec3 recover = this.dashDir.scale(0.45D).add(0.0D, 0.55D, 0.0D);
                dragon.setDeltaMovement(recover);
                dragon.maybeRecoverMove();
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            this.hitDone = false;
            this.dashDir = Vec3.ZERO;
            DibazongzuilongEntity.this.chargeCooldown = CHARGE_COOLDOWN_TICKS;
        }
    }

    /** 冲撞命中帧：龙体周围 4.5 格内玩家受 150 伤害并沿冲撞方向被轰飞。 */
    private void performChargeHit() {
        this.playSound(SoundEvents.GENERIC_EXPLODE, 1.6F, 0.7F);
        // 命中范围按龙体核心固定盒计算：碰撞箱已扩到翼展，直接 inflate 会让判定扩散到三十格外
        AABB hitArea = new AABB(
                this.getX() - CHARGE_HIT_RADIUS, this.getY(), this.getZ() - CHARGE_HIT_RADIUS,
                this.getX() + CHARGE_HIT_RADIUS, this.getY() + this.getBbHeight(),
                this.getZ() + CHARGE_HIT_RADIUS);
        List<Player> players = this.level().getEntitiesOfClass(Player.class, hitArea,
                DibazongzuilongEntity::validTarget);
        for (Player player : players) {
            player.hurt(this.skillDamageSource(CHARGE_DAMAGE_TYPE_KEY), CHARGE_DAMAGE);
            Vec3 push = player.position().subtract(this.position());
            if (push.lengthSqr() < 1.0E-4D) {
                push = this.getLookAngle();
            }
            push = push.normalize();
            player.push(push.x * 1.6D, 0.7D, push.z * 1.6D);
            player.hurtMarked = true;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                    2, 1.0D, 1.0D, 1.0D, 0.0D);
        }
    }

    /** 冲撞恢复末期交给飞行控制器接管（无目标时自然过渡到盘旋/漫游）。 */
    private void maybeRecoverMove() {
        LivingEntity target = this.getTarget();
        if (target != null) {
            this.getMoveControl().setWantedPosition(
                    target.getX(), target.getY() + 9.0D, target.getZ(), 1.0D);
        }
    }

    // -------------------- 技能 3：最懒光束 --------------------

    /**
     * 【最懒光束】CD 120s：先飞抵锁定玩家斜上方约 {@value #BEAM_FIRE_DIST} 格的照射点
     * （最多 2s 就位），随后悬停、张口持续照射 60s——对应「高空盘旋时停下来放技能」。
     * 视觉光束由客户端 {@link DragonBeamEntity}/{@code DragonBeamRenderer} 从龙嘴实心渲染；
     * 每 10tick（0.5s）做一次无遮挡射线判定，命中造成 500 点无视护甲伤害；
     * 目标死亡立即换锁最近的下一名玩家（光束实体改写 targetId，视觉无缝转向），
     * 直到 1200tick 照射走完。玩家跑远时龙会缓慢跟进保持射程。
     */
    private class DibazongzuiBeamGoal extends Goal {
        private static final int ALIGN = 0;
        private static final int FIRE = 1;
        /** 就位阶段上限：40tick（2s），超时也强制开火 */
        private static final int ALIGN_MAX = 40;
        private static final double ARRIVE_DIST_SQR = 3.5D * 3.5D;

        private int runTicks;
        private int fireTicks;
        private int alignTicks;
        private int phase;
        private Player beamTarget;
        private DragonBeamEntity beamEntity;

        DibazongzuiBeamGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = DibazongzuilongEntity.this.getTarget();
            return DibazongzuilongEntity.this.beamCooldown <= 0
                    && !DibazongzuilongEntity.this.isCasting()
                    && target instanceof Player player && validTarget(player);
        }

        @Override
        public boolean canContinueToUse() {
            return this.runTicks < ALIGN_MAX + BEAM_DURATION_TICKS;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            this.fireTicks = 0;
            this.alignTicks = 0;
            this.phase = ALIGN;
            this.beamEntity = null;
            DibazongzuilongEntity dragon = DibazongzuilongEntity.this;
            this.beamTarget = dragon.getTarget() instanceof Player player ? player : null;
            dragon.brakeInAir();
            // 动画覆盖就位+照射全程（循环动画，时长无所谓）
            dragon.entityData.set(DATA_BEAM_ANIM_TICKS, ALIGN_MAX + BEAM_DURATION_TICKS);
            dragon.playSound(SoundEvents.ENDER_DRAGON_GROWL, 2.5F, 0.45F);
        }

        /** 计算照射悬停点：目标斜上方、保持在龙当前所在一侧，避免横穿玩家 */
        private Vec3 firePoint(DibazongzuilongEntity dragon, Player target) {
            double dx = dragon.getX() - target.getX();
            double dz = dragon.getZ() - target.getZ();
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 1.0E-3D) {
                dx = dragon.getLookAngle().x;
                dz = dragon.getLookAngle().z;
            } else {
                dx /= len;
                dz /= len;
            }
            double y = target.getY() + 9.0D;
            y = Mth.clamp(y, dragon.level().getMinBuildHeight() + 4.0D,
                    dragon.level().getMaxBuildHeight() - 4.0D);
            return new Vec3(target.getX() + dx * BEAM_FIRE_DIST, y, target.getZ() + dz * BEAM_FIRE_DIST);
        }

        @Override
        public void tick() {
            this.runTicks++;
            DibazongzuilongEntity dragon = DibazongzuilongEntity.this;

            // 目标死亡/失效（就位或照射中都可能发生）：立刻自动选择下一位玩家
            if (!validTarget(this.beamTarget)) {
                this.beamTarget = dragon.level().getNearestPlayer(
                        dragon.getX(), dragon.getY(), dragon.getZ(), BEAM_RANGE,
                        entity -> entity instanceof Player player && validTarget(player));
                if (this.beamTarget != null) {
                    dragon.playSound(SoundEvents.ENDER_DRAGON_GROWL, 2.0F, 0.5F);
                    if (this.beamEntity != null) {
                        this.beamEntity.setTarget(this.beamTarget);
                    }
                }
            }

            if (this.phase == ALIGN) {
                this.alignTicks++;
                if (this.beamTarget != null) {
                    Vec3 point = this.firePoint(dragon, this.beamTarget);
                    dragon.getMoveControl().setWantedPosition(point.x, point.y, point.z, 1.8D);
                    dragon.faceTarget(this.beamTarget);
                    if (this.alignTicks >= ALIGN_MAX
                            || dragon.position().distanceToSqr(point) <= ARRIVE_DIST_SQR) {
                        this.beginFire(dragon);
                    }
                } else {
                    dragon.brakeInAir();
                    if (this.alignTicks >= ALIGN_MAX) {
                        this.beginFire(dragon);
                    }
                }
            } else {
                this.fireTicks++;
                if (this.beamTarget != null) {
                    Player target = this.beamTarget;
                    dragon.faceTarget(target);
                    // 悬停照射；玩家跑出射程则缓慢跟进到新的照射点，不打断照射
                    if (dragon.distanceToSqr(target) > (BEAM_RANGE - 6.0D) * (BEAM_RANGE - 6.0D)) {
                        Vec3 point = this.firePoint(dragon, target);
                        dragon.getMoveControl().setWantedPosition(point.x, point.y, point.z, 1.3D);
                    } else {
                        dragon.brakeInAir();
                    }
                    dragon.performBeamHit(target, this.fireTicks);
                } else {
                    // 暂时无人可锁：原地悬停空等，计时不停，新目标出现即续射
                    dragon.brakeInAir();
                }
            }
        }

        /** 就位完成：生成光束渲染实体，进入照射阶段。 */
        private void beginFire(DibazongzuilongEntity dragon) {
            this.phase = FIRE;
            this.fireTicks = 0;
            dragon.brakeInAir();
            if (dragon.level() instanceof ServerLevel serverLevel && this.beamTarget != null) {
                this.beamEntity = DragonBeamEntity.spawn(
                        serverLevel, dragon, this.beamTarget, BEAM_DURATION_TICKS);
                dragon.playSound(SoundEvents.ENDER_DRAGON_SHOOT, 2.2F, 0.7F);
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            this.fireTicks = 0;
            this.phase = ALIGN;
            if (this.beamEntity != null) {
                this.beamEntity.discard();
                this.beamEntity = null;
            }
            this.beamTarget = null;
            DibazongzuilongEntity.this.beamCooldown = BEAM_COOLDOWN_TICKS;
        }
    }

    /**
     * 光束照射单 tick：仅做嘴部微光 + 每 10tick 的 500 伤害射线判定，
     * 光束本体由 {@link DragonBeamEntity} 在客户端实心渲染。
     */
    private void performBeamHit(Player target, int fireTicks) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 start = this.mouthPosition();

        // 嘴部发射微光（不是光束本体，只强化「光从嘴里出来」的发射点）
        if (fireTicks % 2 == 0) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    start.x, start.y, start.z, 1, 0.05D, 0.05D, 0.05D, 0.0D);
        }

        Vec3 aimPoint = target.getBoundingBox().getCenter().add(0.0D, 0.4D, 0.0D);
        double dist = start.distanceTo(aimPoint);
        if (dist > BEAM_RANGE) {
            return;
        }

        // 方块遮挡：射线被碰撞方块截断则本跳不造成伤害
        BlockHitResult blockHit = this.level().clip(new ClipContext(
                start, aimPoint, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            return;
        }

        // 每 0.5s 结算一次 500 点无视护甲伤害 + 命中点紫光爆裂
        if (fireTicks % BEAM_HIT_INTERVAL == 0) {
            target.hurt(this.skillDamageSource(BEAM_DAMAGE_TYPE_KEY), BEAM_DAMAGE);
            this.playSound(SoundEvents.ENDER_DRAGON_SHOOT, 1.6F, 1.25F);
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                    aimPoint.x, aimPoint.y, aimPoint.z, 16, 0.35D, 0.35D, 0.35D, 0.03D);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    aimPoint.x, aimPoint.y, aimPoint.z, 5, 0.2D, 0.2D, 0.2D, 0.02D);
        } else if (fireTicks % 40 == 0) {
            this.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.0F, 0.45F);
        }
    }

    // -------------------- 普通攻击：撕咬 --------------------

    /**
     * 【撕咬普攻】：目标进入 5 格且垂直差不大时，播 1.45s 撕咬动画，
     * 第 16tick（0.78s）咬合帧做一次近战伤害（走 ATTACK_DAMAGE=100 的标准近战结算）。
     */
    private class MeleeBiteGoal extends Goal {
        private int runTicks;
        private boolean hitDone;

        MeleeBiteGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            DibazongzuilongEntity dragon = DibazongzuilongEntity.this;
            LivingEntity target = dragon.getTarget();
            return dragon.biteCooldown <= 0 && !dragon.isCasting()
                    && target instanceof Player player && validTarget(player)
                    && dragon.distanceToSqr(target) <= BITE_RANGE_SQR
                    && Math.abs(target.getY() - dragon.getY()) < 4.0D
                    && dragon.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = DibazongzuilongEntity.this.getTarget();
            return this.runTicks < BITE_ANIMATION_TICKS && target != null && target.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            this.hitDone = false;
            DibazongzuilongEntity.this.brakeInAir();
            DibazongzuilongEntity.this.entityData.set(DATA_BITE_ANIM_TICKS, BITE_ANIMATION_TICKS);
        }

        @Override
        public void tick() {
            this.runTicks++;
            DibazongzuilongEntity dragon = DibazongzuilongEntity.this;
            LivingEntity target = dragon.getTarget();
            if (target != null) {
                dragon.faceTarget(target);
                // 咬合前短促贴近，咬合帧后悬停收势
                if (this.runTicks < BITE_HIT_TICK) {
                    dragon.getMoveControl().setWantedPosition(
                            target.getX(), target.getY() + 1.0D, target.getZ(), 1.8D);
                } else {
                    dragon.brakeInAir();
                }
            }
            if (!this.hitDone && this.runTicks >= BITE_HIT_TICK) {
                this.hitDone = true;
                if (target != null) {
                    dragon.doHurtTarget(target);
                    dragon.playSound(SoundEvents.RAVAGER_ATTACK, 1.2F, 0.85F);
                }
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            this.hitDone = false;
            DibazongzuilongEntity.this.biteCooldown = BITE_COOLDOWN_TICKS;
        }
    }

    // -------------------- 常态飞行 AI --------------------

    /**
     * 末影龙式高空盘旋（仿原版末影龙 HoldingPattern 巡航 + Strafe 掠飞节奏，去掉与末地
     * 水晶/返回传送门绑定的着陆阶段）：以首次交战点为圆心，在约 38 格半径、上方约 16 格
     * 的高空沿大圈快速巡航；巡航中随机穿插：
     * <ul>
     *   <li>悬停（停下来 3~7 秒盯住玩家——此时光束/龙息等技能会自然插入施法）；</li>
     *   <li>掠飞（朝玩家当前位置高速逼近 2~3 秒，给冲撞/撕咬创造距离窗口），随后回到大圈。</li>
     * </ul>
     * 盘旋半径上带小幅正弦起伏，模拟原版轨道航点而非死板圆圈。被玩家引离圆心超过 64 格时，
     * 圆心转移到玩家处，避免龙被风筝到永远够不着。
     */
    private class DragonOrbitFlightGoal extends Goal {
        private static final int ORBIT = 0;
        private static final int HOVER = 1;
        private static final int STRAFE = 2;

        private static final double ORBIT_RADIUS = 38.0D;
        private static final double ORBIT_HEIGHT = 16.0D;
        private static final double REANCHOR_DISTANCE = 64.0D;

        private int phase = ORBIT;
        private int phaseTime;
        private int phaseLength;
        private double circleAngle;
        private boolean anchored;
        private double centerX;
        private double centerY;
        private double centerZ;

        DragonOrbitFlightGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return DibazongzuilongEntity.this.getTarget() != null;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = DibazongzuilongEntity.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void start() {
            DibazongzuilongEntity dragon = DibazongzuilongEntity.this;
            // Goal 实例长期复用（被技能抢占后再恢复），圆心只在首次交战时锚定一次
            if (!this.anchored) {
                this.centerX = dragon.getX();
                this.centerY = dragon.getY();
                this.centerZ = dragon.getZ();
                this.circleAngle = dragon.random.nextDouble() * Math.PI * 2.0D;
                this.phase = ORBIT;
                this.phaseTime = 0;
                this.phaseLength = 120 + dragon.random.nextInt(140);
                this.anchored = true;
            }
        }

        @Override
        public void stop() {
            DibazongzuilongEntity.this.navigation.stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            DibazongzuilongEntity dragon = DibazongzuilongEntity.this;
            LivingEntity target = dragon.getTarget();
            if (target == null) {
                return;
            }

            double dx = target.getX() - this.centerX;
            double dz = target.getZ() - this.centerZ;
            if (dx * dx + dz * dz > REANCHOR_DISTANCE * REANCHOR_DISTANCE) {
                // 被风筝过远：圆心转移到玩家身上，龙继续在新圆心高空巡航
                this.centerX = target.getX();
                this.centerY = target.getY();
                this.centerZ = target.getZ();
            }

            this.phaseTime++;
            switch (this.phase) {
                case HOVER -> this.tickHover(dragon, target);
                case STRAFE -> this.tickStrafe(dragon, target);
                default -> this.tickOrbit(dragon, target);
            }
        }

        /** 高空大圈巡航：角速度与巡航速度匹配（目标点线速度≈1.0/tick，龙稳态约1.15/tick，贴圈不拖尾）。 */
        private void tickOrbit(DibazongzuilongEntity dragon, LivingEntity target) {
            this.circleAngle += 0.027D;
            double radius = ORBIT_RADIUS + Math.sin(this.circleAngle * 3.0D) * 4.0D;
            double wx = this.centerX + Math.cos(this.circleAngle) * radius;
            double wz = this.centerZ + Math.sin(this.circleAngle) * radius;
            double wy = Math.max(this.centerY + ORBIT_HEIGHT
                            + Math.sin(this.circleAngle * 1.7D) * 5.0D,
                    dragon.level().getMinBuildHeight() + 6.0D);
            dragon.getMoveControl().setWantedPosition(wx, wy, wz, 1.15D);
            dragon.getLookControl().setLookAt(target, 12.0F, 12.0F);

            if (this.phaseTime >= this.phaseLength) {
                this.chooseNextPhase(dragon, target);
            }
        }

        /** 悬停：空中刹停、盯住玩家（技能 Goal 在此窗口插入施法）。 */
        private void tickHover(DibazongzuilongEntity dragon, LivingEntity target) {
            dragon.brakeInAir();
            dragon.getLookControl().setLookAt(target, 20.0F, 20.0F);
            if (this.phaseTime >= this.phaseLength) {
                this.enterPhase(ORBIT, 100 + dragon.random.nextInt(160));
            }
        }

        /** 掠飞：朝玩家头顶高速逼近（约 44 格/s，不直接造成伤害，伤害由冲撞/撕咬技能结算）。 */
        private void tickStrafe(DibazongzuilongEntity dragon, LivingEntity target) {
            dragon.getMoveControl().setWantedPosition(
                    target.getX(), target.getY() + 8.0D, target.getZ(), 2.2D);
            dragon.getLookControl().setLookAt(target, 20.0F, 20.0F);
            if (this.phaseTime >= this.phaseLength) {
                this.enterPhase(ORBIT, 120 + dragon.random.nextInt(140));
            }
        }

        /** 巡航到点：30% 悬停；30% 且玩家在 40 格内时掠飞；其余继续巡航。 */
        private void chooseNextPhase(DibazongzuilongEntity dragon, LivingEntity target) {
            float roll = dragon.random.nextFloat();
            double distSqr = dragon.distanceToSqr(target);
            if (roll < 0.30F) {
                this.enterPhase(HOVER, 60 + dragon.random.nextInt(80));
            } else if (roll < 0.60F && distSqr < 40.0D * 40.0D) {
                this.enterPhase(STRAFE, 36 + dragon.random.nextInt(30));
            } else {
                this.enterPhase(ORBIT, 100 + dragon.random.nextInt(160));
            }
        }

        private void enterPhase(int phase, int length) {
            this.phase = phase;
            this.phaseTime = 0;
            this.phaseLength = length;
        }
    }

    /** 无目标时的高空漫游：在当前位置附近选悬空点飞过去（参考 Ghast 随机漂浮）。 */
    private class DragonAirWanderGoal extends Goal {
        private int timeout;

        DragonAirWanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            DibazongzuilongEntity dragon = DibazongzuilongEntity.this;
            if (dragon.getTarget() != null || !dragon.navigation.isDone()) {
                return false;
            }
            return dragon.random.nextInt(35) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.timeout > 0 && !DibazongzuilongEntity.this.navigation.isDone();
        }

        @Override
        public void start() {
            DibazongzuilongEntity dragon = DibazongzuilongEntity.this;
            Level level = dragon.level();
            double tx = dragon.getX() + (dragon.random.nextDouble() * 2.0D - 1.0D) * 18.0D;
            double tz = dragon.getZ() + (dragon.random.nextDouble() * 2.0D - 1.0D) * 18.0D;
            double ty = dragon.getY() + (dragon.random.nextDouble() * 12.0D - 4.0D);
            ty = Mth.clamp(ty, level.getMinBuildHeight() + 5.0D, level.getMaxBuildHeight() - 6.0D);
            dragon.navigation.moveTo(tx, ty, tz, 1.2D);
            this.timeout = 120;
        }

        @Override
        public void tick() {
            this.timeout--;
        }

        @Override
        public void stop() {
            this.timeout = 0;
        }
    }
}
