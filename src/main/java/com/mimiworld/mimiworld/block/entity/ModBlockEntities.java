package com.mimiworld.mimiworld.block.entity;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 米米世界方块实体类型注册。
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MimiWorld.MODID);

    // 造米台（27 号方块 rice_maker）：3 槽加工台，GUI 见 menu.RiceMakerMenu
    public static final RegistryObject<BlockEntityType<RiceMakerBlockEntity>> RICE_MAKER =
            BLOCK_ENTITIES.register("rice_maker",
                    () -> BlockEntityType.Builder.of(RiceMakerBlockEntity::new, ModBlocks.RICE_MAKER.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
