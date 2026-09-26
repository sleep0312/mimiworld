package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.GridCraftingTableBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;

/**
 * 杵界台菜单：2x2 输入网格（槽位与 pestle_realm_table.png 对齐）+ 右下结果槽。
 * <ul>
 *   <li>网格槽：x=38/60，y=15/37（槽间留 4px 间隙，行列间距 22）；</li>
 *   <li>结果槽：(116,44)；</li>
 *   <li>玩家背包 y=84，快捷栏 y=142（标准 166 高面板）。</li>
 * </ul>
 */
public class PestleRealmTableMenu extends GridCraftingTableMenu {

    /** 客户端构造：菜单类型工厂通过网络打开时调用，容器使用本地空实现（槽位同步照常工作） */
    public PestleRealmTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(2 * 2 + 1),
                ContainerLevelAccess.NULL);
    }

    /** 服务端构造：由 PestleRealmTableBlockEntity#createMenu 调用 */
    public PestleRealmTableMenu(int containerId, Inventory playerInventory,
                                Container container, ContainerLevelAccess access) {
        super(ModMenuTypes.PESTLE_REALM_TABLE_MENU.get(), containerId, playerInventory, container, access,
                2,        // gridSize：2x2
                38, 15,   // 网格左上槽
                22, 22,   // 槽间距（4px 间隙）
                116, 44,  // 结果槽（右下）
                84);      // 玩家背包首行
    }

    @Override
    protected Block validBlock() {
        return ModBlocks.PESTLE_REALM_TABLE.get();
    }
}
