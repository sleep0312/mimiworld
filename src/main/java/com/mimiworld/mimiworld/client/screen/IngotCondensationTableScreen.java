package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.IngotCondensationTableMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 凝锭台 GUI：5x5 输入网格 + 下方中央结果槽（加高面板），标题为"凝锭台"。 */
public class IngotCondensationTableScreen
        extends GridCraftingTableScreen<IngotCondensationTableMenu> {

    public IngotCondensationTableScreen(IngotCondensationTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title,
                "ingot_condensation_table.png",
                236, // 加高面板：5 行网格 + 箭头 + 结果槽 + 背包
                142); // 背包标题 Y
    }
}
