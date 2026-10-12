package com.mimiworld.mimiworld.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

/**
 * 清洁长命夔（Cleansing Longevity Kui，注册 id = cleansing_longevity_kui）——上古雷兽，
 * 单足牛形巨型敌对陆地 Boss（比劫掠兽更高大）。
 *
 * <p>模型/贴图/动画资源（Blockbench 导出）：geo/qingjiechangmingkui.geo.json、
 * textures/entity/qingjiechangmingkui.png、animations/qingjiechangmingkui.animation.json
 * （idle / walk / attack / roar / stomp / triple_bite）。模型高约 55px（≈3.4 格），
 * 碰撞箱 2.2×3.4，火焰免疫。</p>
 *
 * <p>攻击方式：</p>
 * <ol>
 *   <li>普通撕咬：MeleeAttackGoal，15 点伤害，attack 动画 0.8 秒。</li>
 *   <li>【巨吼】（roar，CD 30s）：仰天长啸 2 秒，释放坚守者式音波——
 *       以自身为中心 10 格内所有生物受到 60 点音波伤害与冲击，第 20 tick 爆发。</li>
 *   <li>【夔之践踏】（stomp，CD 45s）：原地以所在位置为中心 3×3 区域，
 *       5 秒内随机重踏 8 次，落点提前 4 tick 雷光预警，被踩中玩家受到 100 点伤害。</li>
 *   <li>【三连咬】（triple_bite，CD 120s）：向前猛烈撕咬三次，
 *       每次前方扇形判定 70 点伤害，动画 1.6 秒。</li>
 * </ol>
 */
public class QingjiechangmingkuiEntity extends Monster implements GeoEntity {

    // -------------------- 同步动画状态（服务端递减，自动同步客户端） --------------------

    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(QingjiechangmingkuiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ROAR_ANIM_TICKS =
            SynchedEntityData.defineId(QingjiechangmingkuiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STOMP_ANIM_TICKS =
            SynchedEntityData.defineId(QingjiechangmingkuiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BITE_ANIM_TICKS =
            SynchedEntityData.defineId(QingjiechangmingkuiEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.qingjiechangmingkui.idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.qingjiechangmingkui.walk");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.qingjiechangmingkui.attack");
    private static final RawAnimation ANIM_ROAR = RawAnimation.begin().thenPlay("animation.qingjiechangmingkui.roar");
    private static final RawAnimation ANIM_STOMP = RawAnimation.begin().thenLoop("animation.qingjiechangmingkui.stomp");
    private static final RawAnimation ANIM_TRIPLE_BITE = RawAnimation.begin().thenPlay("animation.qingjiechangmingkui.triple_bite");

    /** 普攻动画 0.8 秒 = 16 tick */
    private static final int ATTACK_ANIMATION_TICKS = 16;

    /** 巨吼动画 2 秒 = 40 tick */
    private static final int ROAR_ANIMATION_TICKS = 40;
    /** 音波爆发时刻：仰天长啸顶点 = 第 20 tick */
    private static final int ROAR_HIT_TICK = 20;
    /** 巨吼冷却：30 秒 = 600 tick */
    private static final int ROAR_COOLDOWN_TICKS = 600;
    /** 巨吼伤害（对所有生物） */
    private static final float ROAR_DAMAGE = 60.0F;
    /** 音波影响半径（格） */
    private static final double ROAR_RADIUS = 10.0D;

    /** 践踏持续：5 秒 = 100 tick（stomp 为 0.6 秒循环动画） */
    private static final int STOMP_ANIMATION_TICKS = 100;
    /** 践踏次数 */
    private static final int STOMP_COUNT = 8;
    /** 践踏冷却：45 秒 = 900 tick */
    private static final int STOMP_COOLDOWN_TICKS = 900;
    /** 践踏伤害（对玩家） */
    private static final float STOMP_DAMAGE = 100.0F;
    /** 落点提前预警 tick 数 */
    private static final int STOMP_WARN_TICKS = 4;

    /** 三连咬动画 1.6 秒 = 32 tick */
    private static final int BITE_ANIMATION_TICKS = 32;
    /** 三次咬合结算时刻 */
    private static final int[] BITE_HIT_TICKS = {5, 16, 27};
    /** 三连咬冷却：120 秒 = 2400 tick */
    private static final int BITE_COOLDOWN_TICKS = 2400;
    /** 每次撕咬伤害（对玩家） */
    private static final float BITE_DAMAGE = 70.0F;
    private static final double BITE_HALF_WIDTH = 2.0D;
    private static final double BITE_DEPTH = 3.6D;

    /** 首次技能前的准备时间，避免出生瞬间立刻出招 */
    private static final int INITIAL_ROAR_DELAY = 80;
    private static final int INITIAL_STOMP_DELAY = 140;
    private static final int INITIAL_BITE_DELAY = 220;

    // 技能冷却（仅服务端计时）
    private int roarCooldown = INITIAL_ROAR_DELAY;
    private int stompCooldown = INITIAL_STOMP_DELAY;
    private int biteCooldown = INITIAL_BITE_DELAY;

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);

    public QingjiechangmingkuiEntity(EntityType<? extends QingjiechangmingkuiEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 属性：500 血、移速 0.3、普攻 15、追击 40、护甲 12、韧性 8、80% 击退抗性、攻击击退 1.5。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    /**
     * AI：0 漂浮；1 巨吼（中距离全屏音波）；2 三连咬（近身高伤）；3 夔之践踏（近身持续 AOE）；
     * 4 普攻追击；6 避水游荡；7 看玩家；8 张望。攻击类 Goal 共用 MOVE+LOOK 标志，天然互斥。
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RoarGoal());
        this.goalSelector.addGoal(2, new TripleBiteGoal());
        this.goalSelector.addGoal(3, new StompGoal());
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    private PlayState handleAnimState(AnimationState<QingjiechangmingkuiEntity> state) {
        if (this.entityData.get(DATA_ROAR_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_ROAR);
        } else if (this.entityData.get(DATA_STOMP_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_STOMP);
        } else if (this.entityData.get(DATA_BITE_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_TRIPLE_BITE);
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

    /** 是否正在释放技能（巨吼/践踏/三连咬），技能期间锁住一切其他攻击 Goal。 */
    private boolean isCasting() {
        return this.entityData.get(DATA_ROAR_ANIM_TICKS) > 0
                || this.entityData.get(DATA_STOMP_ANIM_TICKS) > 0
                || this.entityData.get(DATA_BITE_ANIM_TICKS) > 0;
    }

    // -------------------- Boss 血条 --------------------

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
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        super.customServerAiStep();
    }

    // -------------------- 攻击逻辑 --------------------

    /** 普攻命中时触发 attack 动画。 */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            this.entityData.set(DATA_ATTACK_ANIM_TICKS, ATTACK_ANIMATION_TICKS);
            return true;
        }
        return false;
    }

    /**
     * 前方矩形区域撕咬判定（三连咬用）。先硬吸附朝向，再按目标 AABB 中心做
     * 前后/左右分量判定，外扩体积补偿避免擦边漏判。
     */
    private void performFrontalBite(float damage, double push) {
        LivingEntity lockedTarget = this.getTarget();
        float strikeYaw = this.getYRot();
        if (lockedTarget != null) {
            strikeYaw = (float) Math.toDegrees(Math.atan2(
                    -(lockedTarget.getX() - this.getX()),
                    lockedTarget.getZ() - this.getZ()));
            this.setYRot(strikeYaw);
            this.yBodyRot = strikeYaw;
            this.setYHeadRot(strikeYaw);
        }

        Vec3 base = this.position();
        Vec3 facing = Vec3.directionFromRotation(0.0F, strikeYaw);
        Vec3 right = new Vec3(-facing.z, 0.0D, facing.x);
        final double margin = 0.6D;

        AABB searchArea = this.getBoundingBox().inflate(5.0D);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchArea,
                target -> target != this && target.isAlive() && !target.isAlliedTo(this));
        for (LivingEntity target : targets) {
            Vec3 center = target.getBoundingBox().getCenter();
            double relX = center.x - base.x;
            double relZ = center.z - base.z;
            double forward = relX * facing.x + relZ * facing.z;
            double side = relX * right.x + relZ * right.z;
            boolean horizontalHit = forward >= -margin && forward <= BITE_DEPTH + margin
                    && Math.abs(side) <= BITE_HALF_WIDTH + margin;
            boolean verticalHit = target.getBoundingBox().maxY >= base.y - 0.5D
                    && target.getBoundingBox().minY <= base.y + 3.2D;
            if (horizontalHit && verticalHit) {
                target.hurt(this.damageSources().mobAttack(this), damage);
                double pushX = target.getX() - this.getX();
                double pushZ = target.getZ() - this.getZ();
                double pushLen = Math.sqrt(pushX * pushX + pushZ * pushZ);
                if (pushLen > 1.0E-4D) {
                    target.push(pushX / pushLen * push, 0.3D, pushZ / pushLen * push);
                }
            }
        }
    }

    /** 【三连咬】单次咬合：前方 70 点撕咬 + 青色咬合粒子。 */
    private void performBite() {
        this.performFrontalBite(BITE_DAMAGE, 0.7D);
        this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 0.8F + this.random.nextFloat() * 0.2F);
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 16; i++) {
                double dist = 0.8D + this.random.nextDouble() * 1.6D;
                double side = (this.random.nextDouble() - 0.5D) * 2.6D;
                Vec3 facing = Vec3.directionFromRotation(0.0F, this.getYRot());
                Vec3 right = new Vec3(-facing.z, 0.0D, facing.x);
                double cx = this.getX() + facing.x * dist + right.x * side;
                double cz = this.getZ() + facing.z * dist + right.z * side;
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        cx, this.getY() + 1.2D + this.random.nextDouble() * 1.6D, cz,
                        2, 0.15D, 0.2D, 0.15D, 0.02D);
            }
        }
    }

    /** 【巨吼】蓄力期：夔身周围攀升的幽蓝雷魂粒子。 */
    private void spawnRoarChargeParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (int i = 0; i < 6; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double radius = 1.2D + this.random.nextDouble() * 1.4D;
            double cx = this.getX() + Math.cos(angle) * radius;
            double cz = this.getZ() + Math.sin(angle) * radius;
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL,
                    cx, this.getY() + 0.4D + this.random.nextDouble() * 2.4D, cz,
                    1, 0.0D, 0.08D, 0.0D, 0.01D);
            if (i < 3) {
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        cx, this.getY() + 0.6D + this.random.nextDouble() * 2.2D, cz,
                        1, 0.1D, 0.1D, 0.1D, 0.0D);
            }
        }
    }

    /** 【巨吼】第 20 tick 音波爆发：坚守者式音波特效 + 半径 10 格 60 点伤害。 */
    private void performSonicRoar() {
        this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 2.5F, 0.9F + this.random.nextFloat() * 0.15F);
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        double mouthX = this.getX();
        double mouthY = this.getEyeY() + 0.2D;
        double mouthZ = this.getZ();

        // 嘴部音波爆点
        serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, mouthX, mouthY, mouthZ,
                3, 0.6D, 0.6D, 0.6D, 0.0D);

        // 向外扩散的三圈幽蓝冲击波
        for (int ring = 0; ring < 3; ring++) {
            double radius = 3.0D + ring * 2.6D;
            int count = 28 + ring * 10;
            for (int i = 0; i < count; i++) {
                double angle = (Math.PI * 2.0D * i) / count + this.random.nextDouble() * 0.1D;
                double px = mouthX + Math.cos(angle) * (radius + (this.random.nextDouble() - 0.5D) * 0.6D);
                double pz = mouthZ + Math.sin(angle) * (radius + (this.random.nextDouble() - 0.5D) * 0.6D);
                double py = this.getY() + 0.3D + this.random.nextDouble() * 2.6D;
                serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, px, py, pz,
                        1, 0.0D, 0.05D, 0.0D, 0.0D);
                if (i % 3 == 0) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz,
                            1, 0.15D, 0.25D, 0.15D, 0.05D);
                }
            }
        }

        // 以自身为中心：半径内所有生物 60 点音波伤害 + 向外冲击
        AABB area = this.getBoundingBox().inflate(ROAR_RADIUS + 1.0D);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area,
                target -> target != this && target.isAlive() && !target.isAlliedTo(this));
        for (LivingEntity target : targets) {
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist <= ROAR_RADIUS) {
                target.hurt(this.damageSources().sonicBoom(this), ROAR_DAMAGE);
                if (dist > 1.0E-4D) {
                    target.hurtMarked = true;
                    target.push(dx / dist * 1.3D, 0.55D, dz / dist * 1.3D);
                }
            }
        }
    }

    /** 【夔之践踏】落点雷光预警（提前 4 tick）。 */
    private void spawnStompWarning(int blockX, int blockZ) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        double cx = blockX + 0.5D;
        double cz = blockZ + 0.5D;
        double cy = this.getY() + 0.15D;
        for (int i = 0; i < 12; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double r = this.random.nextDouble() * 0.8D;
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    cx + Math.cos(angle) * r, cy + this.random.nextDouble() * 0.4D, cz + Math.sin(angle) * r,
                    1, 0.05D, 0.1D, 0.05D, 0.02D);
        }
        serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, cx, cy + 0.2D, cz,
                5, 0.4D, 0.1D, 0.4D, 0.01D);
    }

    /** 【夔之践踏】单次重踏落地：该格周围玩家 100 点伤害 + 震地特效。 */
    private void performStomp(int blockX, int blockZ) {
        double cx = blockX + 0.5D;
        double cz = blockZ + 0.5D;
        this.playSound(SoundEvents.GENERIC_EXPLODE, 0.9F, 0.55F + this.random.nextFloat() * 0.1F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, cx, this.getY() + 0.25D, cz,
                    2, 0.3D, 0.1D, 0.3D, 0.0D);
            for (int i = 0; i < 18; i++) {
                double angle = this.random.nextDouble() * Math.PI * 2.0D;
                double r = this.random.nextDouble() * 1.1D;
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        cx + Math.cos(angle) * r, this.getY() + 0.2D + this.random.nextDouble() * 0.5D,
                        cz + Math.sin(angle) * r,
                        1, 0.08D, 0.12D, 0.08D, 0.05D);
            }
            for (int i = 0; i < 8; i++) {
                double angle = this.random.nextDouble() * Math.PI * 2.0D;
                double r = this.random.nextDouble() * 1.0D;
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        cx + Math.cos(angle) * r, this.getY() + 0.3D + this.random.nextDouble() * 0.8D,
                        cz + Math.sin(angle) * r,
                        1, 0.1D, 0.15D, 0.1D, 0.08D);
            }
        }

        // 踩中判定：以落点为中心 1.3 格内、垂直区间合理的玩家受 100 点伤害并被震飞
        AABB hitArea = new AABB(cx - 1.3D, this.getY() - 1.0D, cz - 1.3D,
                cx + 1.3D, this.getY() + 3.2D, cz + 1.3D);
        List<Player> players = this.level().getEntitiesOfClass(Player.class, hitArea,
                player -> player.isAlive() && !player.isAlliedTo(this));
        for (Player player : players) {
            player.hurt(this.damageSources().mobAttack(this), STOMP_DAMAGE);
            double dx = player.getX() - cx;
            double dz = player.getZ() - cz;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 1.0E-4D) {
                dx = this.random.nextDouble() - 0.5D;
                dz = this.random.nextDouble() - 0.5D;
                len = Math.sqrt(dx * dx + dz * dz);
            }
            player.push(dx / len * 0.8D, 0.95D, dz / len * 0.8D);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.roarCooldown > 0) {
                this.roarCooldown--;
            }
            if (this.stompCooldown > 0) {
                this.stompCooldown--;
            }
            if (this.biteCooldown > 0) {
                this.biteCooldown--;
            }
            int attackTicks = this.entityData.get(DATA_ATTACK_ANIM_TICKS);
            if (attackTicks > 0) {
                this.entityData.set(DATA_ATTACK_ANIM_TICKS, attackTicks - 1);
            }
            int roarTicks = this.entityData.get(DATA_ROAR_ANIM_TICKS);
            if (roarTicks > 0) {
                this.entityData.set(DATA_ROAR_ANIM_TICKS, roarTicks - 1);
            }
            int stompTicks = this.entityData.get(DATA_STOMP_ANIM_TICKS);
            if (stompTicks > 0) {
                this.entityData.set(DATA_STOMP_ANIM_TICKS, stompTicks - 1);
            }
            int biteTicks = this.entityData.get(DATA_BITE_ANIM_TICKS);
            if (biteTicks > 0) {
                this.entityData.set(DATA_BITE_ANIM_TICKS, biteTicks - 1);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_ANIM_TICKS, 0);
        this.entityData.define(DATA_ROAR_ANIM_TICKS, 0);
        this.entityData.define(DATA_STOMP_ANIM_TICKS, 0);
        this.entityData.define(DATA_BITE_ANIM_TICKS, 0);
    }

    // -------------------- 技能 Goal --------------------

    /**
     * 【巨吼】Goal：目标进入 10 格内时释放。前 20 tick 仰天蓄力（雷光环绕），
     * 第 20 tick 音波爆发，造成半径 10 格 60 点伤害，随后收势；冷却 600 tick。
     */
    private class RoarGoal extends Goal {
        private int runTicks;

        RoarGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = QingjiechangmingkuiEntity.this.getTarget();
            return QingjiechangmingkuiEntity.this.roarCooldown <= 0
                    && !QingjiechangmingkuiEntity.this.isCasting()
                    && target != null && target.isAlive()
                    // 释放距离必须与音波伤害半径一致：巨吼开始时夔会锁住自身移动，
                    // 若触发距离大于 ROAR_RADIUS，目标会停在伤害范围之外导致技能空放
                    && QingjiechangmingkuiEntity.this.distanceToSqr(target) <= ROAR_RADIUS * ROAR_RADIUS
                    && QingjiechangmingkuiEntity.this.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = QingjiechangmingkuiEntity.this.getTarget();
            return this.runTicks < ROAR_ANIMATION_TICKS && target != null && target.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            QingjiechangmingkuiEntity.this.getNavigation().stop();
            QingjiechangmingkuiEntity.this.entityData.set(DATA_ROAR_ANIM_TICKS, ROAR_ANIMATION_TICKS);
            QingjiechangmingkuiEntity.this.playSound(SoundEvents.WARDEN_ROAR, 2.0F, 0.85F);
        }

        @Override
        public void tick() {
            this.runTicks++;
            QingjiechangmingkuiEntity.this.getNavigation().stop();
            LivingEntity target = QingjiechangmingkuiEntity.this.getTarget();
            if (target != null) {
                QingjiechangmingkuiEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            if (this.runTicks >= 10 && this.runTicks < ROAR_HIT_TICK) {
                QingjiechangmingkuiEntity.this.spawnRoarChargeParticles();
            }
            if (this.runTicks == ROAR_HIT_TICK) {
                QingjiechangmingkuiEntity.this.performSonicRoar();
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            QingjiechangmingkuiEntity.this.roarCooldown = ROAR_COOLDOWN_TICKS;
        }
    }

    /**
     * 【三连咬】Goal：目标进入 4 格内时释放，1.6 秒内向前猛咬三次，
     * 每次前方矩形判定 70 点伤害；冷却 2400 tick。
     */
    private class TripleBiteGoal extends Goal {
        private int runTicks;
        private int biteIndex;

        TripleBiteGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = QingjiechangmingkuiEntity.this.getTarget();
            return QingjiechangmingkuiEntity.this.biteCooldown <= 0
                    && !QingjiechangmingkuiEntity.this.isCasting()
                    && target != null && target.isAlive()
                    && QingjiechangmingkuiEntity.this.distanceToSqr(target) <= 16.0D
                    && QingjiechangmingkuiEntity.this.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = QingjiechangmingkuiEntity.this.getTarget();
            return this.runTicks < BITE_ANIMATION_TICKS && target != null && target.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            this.biteIndex = 0;
            QingjiechangmingkuiEntity.this.getNavigation().stop();
            QingjiechangmingkuiEntity.this.entityData.set(DATA_BITE_ANIM_TICKS, BITE_ANIMATION_TICKS);
            QingjiechangmingkuiEntity.this.playSound(SoundEvents.RAVAGER_ROAR, 1.4F, 0.75F);
        }

        @Override
        public void tick() {
            this.runTicks++;
            QingjiechangmingkuiEntity.this.getNavigation().stop();
            LivingEntity target = QingjiechangmingkuiEntity.this.getTarget();
            if (target != null) {
                QingjiechangmingkuiEntity.this.getLookControl().setLookAt(target, 40.0F, 40.0F);
            }
            if (this.biteIndex < BITE_HIT_TICKS.length && this.runTicks == BITE_HIT_TICKS[this.biteIndex]) {
                QingjiechangmingkuiEntity.this.performBite();
                this.biteIndex++;
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            this.biteIndex = 0;
            QingjiechangmingkuiEntity.this.biteCooldown = BITE_COOLDOWN_TICKS;
        }
    }

    /**
     * 【夔之践踏】Goal：目标进入 5 格内时释放，以起手时所在位置为中心 3×3 区域，
     * 5 秒内随机重踏 8 次，落点提前 4 tick 雷光预警，踩中玩家 100 点伤害；冷却 900 tick。
     */
    private class StompGoal extends Goal {
        private int runTicks;
        private int originBlockX;
        private int originBlockZ;
        private final int[] stompTicks = new int[STOMP_COUNT];
        private final int[] stompX = new int[STOMP_COUNT];
        private final int[] stompZ = new int[STOMP_COUNT];
        private final boolean[] warned = new boolean[STOMP_COUNT];

        StompGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = QingjiechangmingkuiEntity.this.getTarget();
            return QingjiechangmingkuiEntity.this.stompCooldown <= 0
                    && !QingjiechangmingkuiEntity.this.isCasting()
                    && target != null && target.isAlive()
                    && QingjiechangmingkuiEntity.this.distanceToSqr(target) <= 25.0D;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = QingjiechangmingkuiEntity.this.getTarget();
            return this.runTicks < STOMP_ANIMATION_TICKS && target != null && target.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.runTicks = 0;
            this.originBlockX = Mth.floor(QingjiechangmingkuiEntity.this.getX());
            this.originBlockZ = Mth.floor(QingjiechangmingkuiEntity.this.getZ());
            LivingEntity target = QingjiechangmingkuiEntity.this.getTarget();
            int targetBX = target != null ? Mth.floor(target.getX()) : this.originBlockX;
            int targetBZ = target != null ? Mth.floor(target.getZ()) : this.originBlockZ;

            for (int i = 0; i < STOMP_COUNT; i++) {
                // 第 12 tick 起约每 11 tick 一踏，均匀铺满 5 秒
                this.stompTicks[i] = 12 + i * 11;
                int dx;
                int dz;
                if (i < 3 && Math.abs(targetBX - this.originBlockX) <= 1
                        && Math.abs(targetBZ - this.originBlockZ) <= 1) {
                    // 前三次优先封玩家走位
                    dx = targetBX - this.originBlockX;
                    dz = targetBZ - this.originBlockZ;
                } else {
                    dx = QingjiechangmingkuiEntity.this.random.nextInt(3) - 1;
                    dz = QingjiechangmingkuiEntity.this.random.nextInt(3) - 1;
                }
                this.stompX[i] = this.originBlockX + dx;
                this.stompZ[i] = this.originBlockZ + dz;
                this.warned[i] = false;
            }

            QingjiechangmingkuiEntity.this.getNavigation().stop();
            QingjiechangmingkuiEntity.this.entityData.set(DATA_STOMP_ANIM_TICKS, STOMP_ANIMATION_TICKS);
            QingjiechangmingkuiEntity.this.playSound(SoundEvents.RAVAGER_ROAR, 1.6F, 0.6F);
        }

        @Override
        public void tick() {
            this.runTicks++;
            QingjiechangmingkuiEntity.this.getNavigation().stop();
            LivingEntity target = QingjiechangmingkuiEntity.this.getTarget();
            if (target != null) {
                QingjiechangmingkuiEntity.this.getLookControl().setLookAt(target, 20.0F, 20.0F);
            }
            for (int i = 0; i < STOMP_COUNT; i++) {
                if (!this.warned[i] && this.runTicks == this.stompTicks[i] - STOMP_WARN_TICKS) {
                    QingjiechangmingkuiEntity.this.spawnStompWarning(this.stompX[i], this.stompZ[i]);
                    this.warned[i] = true;
                }
                if (this.runTicks == this.stompTicks[i]) {
                    QingjiechangmingkuiEntity.this.performStomp(this.stompX[i], this.stompZ[i]);
                }
            }
        }

        @Override
        public void stop() {
            this.runTicks = 0;
            QingjiechangmingkuiEntity.this.stompCooldown = STOMP_COOLDOWN_TICKS;
        }
    }

    // -------------------- 音效：坚守者（巨吼）+ 劫掠兽（撕咬/咆哮） --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 140;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 0.35F, 0.7F);
    }
}
