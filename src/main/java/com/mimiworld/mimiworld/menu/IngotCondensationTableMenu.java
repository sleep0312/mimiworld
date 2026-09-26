package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.ModBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;

/**
 * 凝锭台菜单：5x5 输入网格（槽位与 ingot_condensation_table.png 对齐）+ 下方中央结果槽。
 * <ul>
 *   <li>网格 25 槽：x=43+col*18（居中，宽 90），y=13+row*18；</li>
 *   <li>结果槽：(79,115)；</li>
 *   <li>玩家背包 y=154，快捷栏 y=212（加高面板，高 236）。</li>
 * </ul>
 */
public class IngotCondensationTableMenu extends GridCraftingTableMenu {

    /** 客户端构造：菜单类型工厂通过网络打开时调用，容器使用本地空实现（槽位同步照常工作） */
    public IngotCondensationTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(5 * 5 + 1),
                ContainerLevelAccess.NULL);
    }

    /** 服务端构造：由 IngotCondensationTableBlockEntity#createMenu 调用 */
    public IngotCondensationTableMenu(int containerId, Inventory playerInventory,
                                      Container container, ContainerLevelAccess access) {
        super(ModMenuTypes.INGOT_CONDENSATION_TABLE_MENU.get(), containerId, playerInventory, container, access,
                5,        // gridSize：5x5
                43, 13,   // 网格左上槽（90px 宽在 176 面板内居中）
                18, 18,   // 槽间距 18（紧密网格）
                79, 115,  // 结果槽（网格下方中央）
                154);     // 玩家背包首行（面板加高）
    }

    @Override
    protected Block validBlock() {
        return ModBlocks.INGOT_CONDENSATION_TABLE.get();
    }
}
