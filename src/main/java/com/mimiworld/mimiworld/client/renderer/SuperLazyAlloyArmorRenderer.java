package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.SuperLazyAlloyArmorModel;
import com.mimiworld.mimiworld.item.SuperLazyAlloyArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * 超懒合金套（T5·终极套）穿戴渲染器。
 * GeoArmorRenderer 自身继承 HumanoidModel，由盔甲物品的 IClientItemExtensions 返回，
 * 并根据当前 EquipmentSlot 自动隐藏与槽位无关的 armor* 骨（含其子骨）。
 */
public class SuperLazyAlloyArmorRenderer extends GeoArmorRenderer<SuperLazyAlloyArmorItem> {

    public SuperLazyAlloyArmorRenderer() {
        super(new SuperLazyAlloyArmorModel());
    }
}
