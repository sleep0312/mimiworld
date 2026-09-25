package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.ForgingTableBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;

/**
 * 铸剑台菜单：槽位布局见 {@link ForgingTableMenu}（与原版工作台一致），配方表独立。
 */
public class SwordForgingTableMenu extends ForgingTableMenu {

    /** 客户端构造：菜单类型工厂通过网络打开时调用，容器使用本地空实现（槽位同步照常工作） */
    public SwordForgingTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(ForgingTableBlockEntity.TOTAL_SLOTS),
                ContainerLevelAccess.NULL);
    }

    /** 服务端构造：由 SwordForgingTableBlockEntity#createMenu 调用 */
    public SwordForgingTableMenu(int containerId, Inventory playerInventory,
                                 Container container, ContainerLevelAccess access) {
        super(ModMenuTypes.SWORD_FORGING_TABLE_MENU.get(), containerId, playerInventory, container, access);
    }

    @Override
    protected Block validBlock() {
        return ModBlocks.SWORD_FORGING_TABLE.get();
    }
}
