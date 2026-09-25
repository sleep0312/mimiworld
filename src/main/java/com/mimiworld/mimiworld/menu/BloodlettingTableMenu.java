package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.MultiInputProcessorBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.Block;

/**
 * 汲血台菜单：左列两个输入槽 (44,18)/(44,50)、右侧输出槽 (116,33)，
 * 坐标与 bloodletting_table.png 槽框严格一致。
 */
public class BloodlettingTableMenu extends MultiInputProcessorMenu {

    public static final int SLOT_INPUT_0_X = 44;
    public static final int SLOT_INPUT_0_Y = 18;
    public static final int SLOT_INPUT_1_X = 44;
    public static final int SLOT_INPUT_1_Y = 50;
    public static final int SLOT_OUTPUT_X = 116;
    public static final int SLOT_OUTPUT_Y = 33;

    /** 客户端构造：网络打开时调用，容器/数据使用本地空实现 */
    public BloodlettingTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(3),
                new SimpleContainerData(MultiInputProcessorBlockEntity.DATA_COUNT),
                ContainerLevelAccess.NULL);
    }

    /** 服务端构造：由 BloodlettingTableBlockEntity#createMenu 调用 */
    public BloodlettingTableMenu(int containerId, Inventory playerInventory,
                                 Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.BLOODLETTING_TABLE_MENU.get(), containerId, playerInventory, container, data, access);
    }

    @Override
    protected Block validBlock() {
        return ModBlocks.BLOODLETTING_TABLE.get();
    }

    @Override
    protected void addMachineSlots(Container container) {
        addInputSlot(container, 0, SLOT_INPUT_0_X, SLOT_INPUT_0_Y);
        addInputSlot(container, 1, SLOT_INPUT_1_X, SLOT_INPUT_1_Y);
        addResultSlot(container, 2, SLOT_OUTPUT_X, SLOT_OUTPUT_Y);
    }
}
