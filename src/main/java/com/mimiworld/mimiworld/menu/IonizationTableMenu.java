package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.DualInputProcessorBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.Block;

/**
 * 电离台菜单：槽位布局见 {@link DualInputProcessorMenu}。
 */
public class IonizationTableMenu extends DualInputProcessorMenu {

    /** 客户端构造：菜单类型工厂通过网络打开时调用，容器/数据使用本地空实现（槽位同步照常工作） */
    public IonizationTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(DualInputProcessorBlockEntity.TOTAL_SLOTS),
                new SimpleContainerData(DualInputProcessorBlockEntity.DATA_COUNT),
                ContainerLevelAccess.NULL);
    }

    /** 服务端构造：由 IonizationTableBlockEntity#createMenu 调用 */
    public IonizationTableMenu(int containerId, Inventory playerInventory,
                               Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.IONIZATION_TABLE_MENU.get(), containerId, playerInventory,
                container, data, access);
    }

    @Override
    protected Block validBlock() {
        return ModBlocks.IONIZATION_TABLE.get();
    }
}
