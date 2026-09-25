package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.SwordForgingTableMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 铸剑台 GUI：原版工作台贴图，标题为"铸剑台"。 */
public class SwordForgingTableScreen extends ForgingTableScreen<SwordForgingTableMenu> {

    public SwordForgingTableScreen(SwordForgingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
