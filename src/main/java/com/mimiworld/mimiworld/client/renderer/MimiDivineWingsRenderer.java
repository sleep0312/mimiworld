package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.MimiDivineWingsModel;
import com.mimiworld.mimiworld.item.MimiDivineWingsItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * 米米神翼穿戴渲染器。
 * GeoArmorRenderer 自身继承 HumanoidModel，由盔甲物品的 IClientItemExtensions 返回，
 * 胸甲槽位下显示挂在 bipedBody 下的双翼，并持续播放循环扇翅动画。
 */
public class MimiDivineWingsRenderer extends GeoArmorRenderer<MimiDivineWingsItem> {

    public MimiDivineWingsRenderer() {
        super(new MimiDivineWingsModel());
    }
}
