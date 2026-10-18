package com.mimiworld.mimiworld.item;

import com.mimiworld.mimiworld.client.model.MubangongBlankModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

/**
 * 清洁复合弓胚的 GeckoLib 3D 物品实现。
 *
 * <p>显示上与正式弓（{@link MubangongBowItem}）完全一致：共用同一套 geo 模型
 * （geo/mubangong.geo.json）与 idle 循环动画，各视角变换读取
 * models/item/cleansing_compound_bow_blank.json 的 display 节点，贴图为
 * textures/item/cleansing_compound_bow_blank.png。</p>
 *
 * <p>行为上是普通合成材料：堆叠 64、无耐久、不可拉弓射击（不继承 {@link MimiBowItem}，
 * 不挂 {@code NockedArrowLayer}），仅用于合成正式弓。</p>
 */
public class MubangongBlankItem extends Item implements GeoItem {

    /** 与正式弓相同的静止循环动画。 */
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.mubangong.idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** 本胚件专属贴图名（不含路径与扩展名），渲染时解析为 textures/item/&lt;textureName&gt;.png。 */
    private final String textureName;

    public MubangongBlankItem(String textureName, Properties properties) {
        super(properties);
        this.textureName = textureName;
    }

    /** 供 {@link MubangongBlankModel} 取用本胚件的贴图名。 */
    public String getTextureName() {
        return this.textureName;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer<MubangongBlankItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<MubangongBlankItem>(new MubangongBlankModel()) {
                        @Override
                        public long getInstanceId(MubangongBlankItem animatable) {
                            return System.identityHashCode(this.getCurrentItemStack());
                        }
                    };
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "blank", 2, state -> {
            state.setAndContinue(IDLE);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
