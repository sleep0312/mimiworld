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
 * 搅碎器菜单：输入槽左上 (26,17)、左下 (26,50)、右上 (121,17)，输出槽右下 (121,50)，
 * 坐标与 crusher.png 槽框严格一致。
 */
public class CrusherMenu extends MultiInputProcessorMenu {

    public static final int SLOT_TOP_LEFT_X = 26;
    public static final int SLOT_TOP_LEFT_Y = 17;
    public static final int SLOT_BOTTOM_LEFT_X = 26;
    public static final int SLOT_BOTTOM_LEFT_Y = 50;
    public static final int SLOT_TOP_RIGHT_X = 121;
    public static final int SLOT_TOP_RIGHT_Y = 17;
    public static final int SLOT_OUTPUT_X = 121;
    public static final int SLOT_OUTPUT_Y = 50;

    /** 客户端构造 */
    public CrusherMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(4),
                new SimpleContainerData(MultiInputProcessorBlockEntity.DATA_COUNT),
                ContainerLevelAccess.NULL);
    }

    /** 服务端构造：由 CrusherBlockEntity#createMenu 调用 */
    public CrusherMenu(int containerId, Inventory playerInventory,
                       Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.CRUSHER_MENU.get(), containerId, playerInventory, container, data, access);
    }

    @Override
    protected Block validBlock() {
        return ModBlocks.CRUSHER.get();
    }

    @Override
    protected void addMachineSlots(Container container) {
        addInputSlot(container, 0, SLOT_TOP_LEFT_X, SLOT_TOP_LEFT_Y);
        addInputSlot(container, 1, SLOT_BOTTOM_LEFT_X, SLOT_BOTTOM_LEFT_Y);
        addInputSlot(container, 2, SLOT_TOP_RIGHT_X, SLOT_TOP_RIGHT_Y);
        addResultSlot(container, 3, SLOT_OUTPUT_X, SLOT_OUTPUT_Y);
    }
}
