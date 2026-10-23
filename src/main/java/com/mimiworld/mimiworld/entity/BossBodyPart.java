package com.mimiworld.mimiworld.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

/**
 * 多部件 Boss 的单个身体部位碰撞箱（Forge {@link PartEntity} 实现）。
 *
 * <p>巨型 Boss 的模型往往远大于单个 {@code EntityType.Builder#sized} 方块盒（例如翼展 58 格），
 * 用一个大方块会覆盖大量空气、且 XZ 必须同宽，无法贴合模型。这里把身体拆成
 * 躯干/颈/头/尾/双翼/四肢若干部位，每个部位一个独立 AABB：</p>
 * <ul>
 *   <li>只参与受击判定（{@link #isPickable()} 为 true），不参与物理推挤
 *       （{@link #canBeCollidedWith()} 为 false），因此玩家可以自由穿过翼尖；</li>
 *   <li>伤害通过 {@link #hurt} 原样转交给本体，血条、护甲、受击动画全部沿用本体逻辑；</li>
 *   <li>不单独存盘、不单独发包，位置完全由本体每 tick 按朝向摆放。</li>
 * </ul>
 */
public class BossBodyPart<T extends LivingEntity> extends PartEntity<T> {

    private final String partName;
    private final EntityDimensions size;
    /** 相对本体脚底的模型空间偏移（格）：局部 +X 为本体右方，局部 -Z 为本体前方 */
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    /**
     * @param parent  本体
     * @param partName 部位名（head/body/wing 等，仅用于调试与识别）
     * @param width   碰撞箱宽度（X 向，格）
     * @param height  碰撞箱高度（Y 向，格）
     * @param offsetX 模型空间中心 X 偏移（格，正值 = 模型局部 +X = 本体右方）
     * @param offsetY 模型空间底部 Y 偏移（格，自本体脚底向上）
     * @param offsetZ 模型空间中心 Z 偏移（格，负值 = 本体前方）
     */
    public BossBodyPart(T parent, String partName, float width, float height,
                        float offsetX, float offsetY, float offsetZ) {
        super(parent);
        this.partName = partName;
        this.size = EntityDimensions.scalable(width, height);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.refreshDimensions();
    }

    public String getPartName() {
        return this.partName;
    }

    public float getOffsetX() {
        return this.offsetX;
    }

    public float getOffsetY() {
        return this.offsetY;
    }

    public float getOffsetZ() {
        return this.offsetZ;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    /** 部位可被近战/弹射物选中，这是多部件受击的关键。 */
    @Override
    public boolean isPickable() {
        return true;
    }

    /** 部位不产生物理碰撞/推挤，只有本体的核心碰撞箱参与实体物理。 */
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    /** 让“部位”与“本体”在实体比对时视为同一目标（避免友军 AI、命中判定误判）。 */
    @Override
    public boolean is(Entity entity) {
        return this == entity || this.getParent() == entity;
    }

    /** 伤害一律转交给本体结算，保证血条、护甲、无敌帧、受击动画只有一套逻辑。 */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && this.getParent().hurt(source, amount);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.size;
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return this.getParent().getPickResult();
    }
}
