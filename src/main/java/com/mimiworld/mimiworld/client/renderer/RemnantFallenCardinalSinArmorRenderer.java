package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.RemnantFallenCardinalSinArmorModel;
import com.mimiworld.mimiworld.item.RemnantFallenCardinalSinArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * 残堕宗罪套装穿戴渲染器。
 * GeoArmorRenderer 自身继承 HumanoidModel，由盔甲物品的 IClientItemExtensions 返回，
 * 并根据当前 EquipmentSlot 自动隐藏与槽位无关的 armor* 骨（含其子骨）。
 */
public class RemnantFallenCardinalSinArmorRenderer
        extends GeoArmorRenderer<RemnantFallenCardinalSinArmorItem> {

    public RemnantFallenCardinalSinArmorRenderer() {
        super(new RemnantFallenCardinalSinArmorModel());
    }
}
