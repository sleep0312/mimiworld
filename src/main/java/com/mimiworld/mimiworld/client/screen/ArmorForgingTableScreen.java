package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.ArmorForgingTableMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 铸甲台 GUI：原版工作台贴图，标题为"铸甲台"。 */
public class ArmorForgingTableScreen extends ForgingTableScreen<ArmorForgingTableMenu> {

    public ArmorForgingTableScreen(ArmorForgingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
