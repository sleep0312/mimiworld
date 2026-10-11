package com.mimiworld.mimiworld.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 最懒光束的「渲染载体实体」：本身无碰撞、无 AI、不存档、不造成伤害（伤害由
 * {@link LaziestFinalDragonEntity} 的技能逻辑结算）。只负责把施化龙与锁定玩家的
 * 网络 ID 同步到客户端，由 {@code DragonBeamRenderer} 在两者之间渲染一条真正的
 * 紫色光束（守卫者光束纹理 + 双层光柱），避免用粒子拼光束导致的「从肚子射出/不像」。
 *
 * <p>生命周期完全跟随技能：技能开始时 spawn，龙死亡/技能结束/存活期归零即移除；
 * 锁定玩家死亡时由服务端 Goal 改写 TARGET_ID 实现「自动换人」，光束视觉无缝转向。</p>
 */
public class DragonBeamEntity extends Entity {

    private static final EntityDataAccessor<Integer> DATA_OWNER_ID =
            SynchedEntityData.defineId(DragonBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(DragonBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIFE_TICKS =
            SynchedEntityData.defineId(DragonBeamEntity.class, EntityDataSerializers.INT);

    public DragonBeamEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setSilent(true);
        this.setInvisible(true);
    }

    /** 服务端生成一条持续 {@code lifeTicks} 的光束，挂在施化龙与锁定玩家之间。 */
    public static DragonBeamEntity spawn(ServerLevel level, LaziestFinalDragonEntity dragon,
                                        Player target, int lifeTicks) {
        DragonBeamEntity beam = new DragonBeamEntity(ModEntities.DRAGON_BEAM.get(), level);
        // 实体位置跟随龙，纯粹用于区块加载/网络同步范围判定；渲染坐标从 owner 实时取
        beam.moveTo(dragon.getX(), dragon.getY(), dragon.getZ(), dragon.getYRot(), 0.0F);
        beam.entityData.set(DATA_OWNER_ID, dragon.getId());
        beam.entityData.set(DATA_TARGET_ID, target.getId());
        beam.entityData.set(DATA_LIFE_TICKS, lifeTicks);
        level.addFreshEntity(beam);
        return beam;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_OWNER_ID, -1);
        this.entityData.define(DATA_TARGET_ID, -1);
        this.entityData.define(DATA_LIFE_TICKS, 0);
    }

    @Override
    public void tick() {
        // 每 tick 吸附到龙的位置，保证光束在龙跨区块移动时仍处于正确的网络同步范围
        Entity owner = this.getOwner();
        if (owner != null) {
            this.setPos(owner.getX(), owner.getY(), owner.getZ());
        }
        if (!this.level().isClientSide) {
            int life = this.entityData.get(DATA_LIFE_TICKS);
            if (life <= 1 || !(owner instanceof LivingEntity livingOwner) || !livingOwner.isAlive()) {
                this.discard();
                return;
            }
            this.entityData.set(DATA_LIFE_TICKS, life - 1);
        }
    }

    /** 锁定玩家死亡/离场时由技能逻辑调用，无缝切换到下一名玩家。 */
    public void setTarget(Player target) {
        this.entityData.set(DATA_TARGET_ID, target.getId());
    }

    public Entity getOwner() {
        int id = this.entityData.get(DATA_OWNER_ID);
        return id == -1 ? null : this.level().getEntity(id);
    }

    public Player getTargetPlayer() {
        int id = this.entityData.get(DATA_TARGET_ID);
        if (id == -1) {
            return null;
        }
        Entity entity = this.level().getEntity(id);
        return entity instanceof Player player ? player : null;
    }

    public int getLifeTicks() {
        return this.entityData.get(DATA_LIFE_TICKS);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSqr) {
        // 光束最大射程 48 格，给到 96 格都可见
        return distanceSqr < 96.0D * 96.0D * 4.0D;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    // 不存档：纯瞬时技能特效，重载世界时不应残留
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
