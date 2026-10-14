package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.SuperLazyWingsChestplateModel;
import com.mimiworld.mimiworld.item.SuperLazyWingsChestplateItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * 超懒神翼胸甲穿戴渲染器。
 * GeoArmorRenderer 自身继承 HumanoidModel，由盔甲物品的 IClientItemExtensions 返回，
 * 胸甲槽位下显示 body（含挂在 bipedBody 下的双翼），并持续播放循环扇翅动画。
 */
public class SuperLazyWingsChestplateRenderer extends GeoArmorRenderer<SuperLazyWingsChestplateItem> {

    public SuperLazyWingsChestplateRenderer() {
        super(new SuperLazyWingsChestplateModel());
    }
}
