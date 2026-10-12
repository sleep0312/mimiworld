package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.RiceCrystalAlloyArmorModel;
import com.mimiworld.mimiworld.item.RiceCrystalAlloyArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * 饭晶合金套（T1）穿戴渲染器。
 * GeoArmorRenderer 自身继承 HumanoidModel，由盔甲物品的 IClientItemExtensions 返回，
 * 并根据当前 EquipmentSlot 自动隐藏与槽位无关的 armor* 骨（含其子骨）。
 */
public class RiceCrystalAlloyArmorRenderer extends GeoArmorRenderer<RiceCrystalAlloyArmorItem> {

    public RiceCrystalAlloyArmorRenderer() {
        super(new RiceCrystalAlloyArmorModel());
    }
}
