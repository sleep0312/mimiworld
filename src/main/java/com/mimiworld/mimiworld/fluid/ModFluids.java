package com.mimiworld.mimiworld.fluid;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.ModItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 米米世界流体注册：米汤（rice_soup，米桶）与饭汤（rice_broth，饭桶）。
 *
 * 1.20.1 Forge 47.x 的流体三件套（旧版 FluidAttributes 已改名 FluidType）：
 * <ol>
 *     <li>FluidType：密度/粘度/亮度/音效/染色等「属性」，注册到 ForgeRegistries.Keys.FLUID_TYPES；</li>
 *     <li>源流体 + 流动流体（ForgeFlowingFluid.Source/Flowing），注册到 ForgeRegistries.FLUIDS；</li>
 *     <li>世界中的流体方块（LiquidBlock，无 BlockItem、不注册为物品）。</li>
 * </ol>
 * 米汤与饭汤各注册一个专属炼药锅；两种桶行为均同原版水桶。
 */
public class ModFluids {
    // 流体类型注册表（Forge 注册表，必须用 ForgeRegistries.Keys.FLUID_TYPES）
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MimiWorld.MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, MimiWorld.MODID);
    // 流体方块独立持有一个 BLOCKS 注册器（与 block.ModBlocks 平行，同一注册表允许多个 DeferredRegister）
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MimiWorld.MODID);

    // 贴图引用：assets/mimiworld/textures/block/rice_soup_still.png / rice_soup_flow.png
    public static final ResourceLocation STILL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MimiWorld.MODID, "block/rice_soup_still");
    public static final ResourceLocation FLOWING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MimiWorld.MODID, "block/rice_soup_flow");

    // 饭汤贴图引用：assets/mimiworld/textures/block/rice_broth_still.png / rice_broth_flow.png
    public static final ResourceLocation BROTH_STILL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MimiWorld.MODID, "block/rice_broth_still");
    public static final ResourceLocation BROTH_FLOWING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MimiWorld.MODID, "block/rice_broth_flow");

    // 米汤染色：ARGB = 0x80F5F5F5（半透明奶白）
    public static final int TINT_COLOR = 0x80F5F5F5;
    // 饭汤染色：ARGB = 0x80F5F5F5（半透明；贴图本身为暖奶油色，故观感呈暖汤）
    public static final int BROTH_TINT_COLOR = 0x80F5F5F5;

    // ===== 1. 流体类型（属性） =====
    // density=2000 kg/m³（比水重，实体会下沉）；静态 viscosity=6000（非米米世界默认值）；
    // luminosity=0；非气态（1.20.1 已无 gaseous() 方法，默认即非气态）；
    // 装桶/倒出音效与水桶完全一致；米米世界内流动变快由 RiceSoupFluid#getTickDelay 按维度实现。
    public static final RegistryObject<FluidType> RICE_SOUP_TYPE =
            FLUID_TYPES.register("rice_soup", () -> new FluidType(
                    FluidType.Properties.create()
                            .density(2000)
                            .viscosity(6000)
                            .lightLevel(0)
                            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() {
                            return STILL_TEXTURE;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLOWING_TEXTURE;
                        }

                        @Override
                        public int getTintColor() {
                            return TINT_COLOR;
                        }
                    });
                }
            });

    // ===== 2. 源流体 / 流动流体 =====
    public static final RegistryObject<FlowingFluid> RICE_SOUP =
            FLUIDS.register("rice_soup", () -> new RiceSoupFluid.Source(makeProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_RICE_SOUP =
            FLUIDS.register("flowing_rice_soup", () -> new RiceSoupFluid.Flowing(makeProperties()));

    // ===== 3. 流体方块（属性照抄原版水：不可替换时才放置、无碰撞、100 硬度、不可推动、不掉落；颜色奶白） =====
    public static final RegistryObject<LiquidBlock> RICE_SOUP_BLOCK =
            BLOCKS.register("rice_soup", () -> new LiquidBlock(RICE_SOUP,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.SNOW)
                            .replaceable()
                            .noCollission()
                            .strength(100.0F)
                            .pushReaction(PushReaction.DESTROY)
                            .noLootTable()
                            .liquid()
                            .sound(SoundType.EMPTY)));

    // ===== 4. 米汤炼药锅（无 BlockItem、不注册为物品：只能由米桶倒入/空桶舀出，破坏后掉原版空锅） =====
    // 交互表：必须用 fastutil 的 Object2ObjectOpenHashMap 并设置默认返回 PASS——
    // AbstractCauldronBlock#use 对 map.get(item) 的结果不做 null 判断（原版地图靠 defaultReturnValue 兜底）。
    public static final Map<Item, CauldronInteraction> RICE_SOUP_CAULDRON_INTERACTIONS =
            Util.make(new Object2ObjectOpenHashMap<>(), map -> {
                map.defaultReturnValue((state, level, pos, player, hand, stack) -> InteractionResult.PASS);
                // 空桶只能从「装满（level=3）」的米汤锅舀走一整桶米汤，锅变回空锅（与原版水锅用水桶的规则一致）
                map.put(Items.BUCKET, (state, level, pos, player, hand, stack) -> {
                    if (state.getValue(LayeredCauldronBlock.LEVEL) != 3) {
                        return InteractionResult.PASS;
                    }
                    if (!level.isClientSide) {
                        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                                new ItemStack(ModItems.RICE_BUCKET.get())));
                        player.awardStat(Stats.USE_CAULDRON);
                        player.awardStat(Stats.ITEM_USED.get(Items.BUCKET));
                        level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                });
            });

    // 方块本身照抄原版炼药锅属性（铁、需镐、2 硬度），液位行为同水锅（RAIN：雨天可接雨续满）
    public static final RegistryObject<LayeredCauldronBlock> RICE_SOUP_CAULDRON =
            BLOCKS.register("rice_soup_cauldron", () -> new LayeredCauldronBlock(
                    BlockBehaviour.Properties.copy(Blocks.CAULDRON),
                    LayeredCauldronBlock.RAIN,
                    RICE_SOUP_CAULDRON_INTERACTIONS));

    // ============================================================================
    // 饭汤（rice_broth，Rice Broth）——参数按用户给定：
    //   染色 ARGB 0x80F5F5F5；density=2000 kg/m³；静态 viscosity=7000（非米米世界），
    //   米米世界内 2000（由 RiceBrothFluid#getTickDelay 按维度实现，35t / 10t）；
    //   luminosity=0；非气态（1.20.1 无 gaseous()，默认即非气态）；装桶/倒出音效与水桶一致。
    // ============================================================================

    // ===== 1. 流体类型（属性） =====
    public static final RegistryObject<FluidType> RICE_BROTH_TYPE =
            FLUID_TYPES.register("rice_broth", () -> new FluidType(
                    FluidType.Properties.create()
                            .density(2000)
                            .viscosity(7000)
                            .lightLevel(0)
                            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() {
                            return BROTH_STILL_TEXTURE;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return BROTH_FLOWING_TEXTURE;
                        }

                        @Override
                        public int getTintColor() {
                            return BROTH_TINT_COLOR;
                        }
                    });
                }
            });

    // ===== 2. 源流体 / 流动流体 =====
    public static final RegistryObject<FlowingFluid> RICE_BROTH =
            FLUIDS.register("rice_broth", () -> new RiceBrothFluid.Source(makeBrothProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_RICE_BROTH =
            FLUIDS.register("flowing_rice_broth", () -> new RiceBrothFluid.Flowing(makeBrothProperties()));

    // ===== 3. 流体方块（属性同米汤方块：不可替换时才放置、无碰撞、100 硬度、不可推动、不掉落；颜色暖沙） =====
    public static final RegistryObject<LiquidBlock> RICE_BROTH_BLOCK =
            BLOCKS.register("rice_broth", () -> new LiquidBlock(RICE_BROTH,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.SAND)
                            .replaceable()
                            .noCollission()
                            .strength(100.0F)
                            .pushReaction(PushReaction.DESTROY)
                            .noLootTable()
                            .liquid()
                            .sound(SoundType.EMPTY)));

    // ===== 4. 饭汤炼药锅（无 BlockItem、不注册为物品：只能由饭桶倒入/空桶舀出，破坏后掉原版空锅） =====
    // 交互表规则与米汤锅完全一致，仅产物换成饭桶。
    public static final Map<Item, CauldronInteraction> RICE_BROTH_CAULDRON_INTERACTIONS =
            Util.make(new Object2ObjectOpenHashMap<>(), map -> {
                map.defaultReturnValue((state, level, pos, player, hand, stack) -> InteractionResult.PASS);
                // 空桶只能从「装满（level=3）」的饭汤锅舀走一整桶饭汤，锅变回空锅
                map.put(Items.BUCKET, (state, level, pos, player, hand, stack) -> {
                    if (state.getValue(LayeredCauldronBlock.LEVEL) != 3) {
                        return InteractionResult.PASS;
                    }
                    if (!level.isClientSide) {
                        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                                new ItemStack(ModItems.RICE_PAIL.get())));
                        player.awardStat(Stats.USE_CAULDRON);
                        player.awardStat(Stats.ITEM_USED.get(Items.BUCKET));
                        level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                });
            });

    // 方块属性同米汤锅（铁、需镐、2 硬度），液位行为同水锅（RAIN：雨天可接雨续满）
    public static final RegistryObject<LayeredCauldronBlock> RICE_BROTH_CAULDRON =
            BLOCKS.register("rice_broth_cauldron", () -> new LayeredCauldronBlock(
                    BlockBehaviour.Properties.copy(Blocks.CAULDRON),
                    LayeredCauldronBlock.RAIN,
                    RICE_BROTH_CAULDRON_INTERACTIONS));

    // ForgeFlowingFluid 属性：类型 + 源/流动互引 + 流体方块 + 桶（米桶）
    private static ForgeFlowingFluid.Properties makeProperties() {
        return new ForgeFlowingFluid.Properties(RICE_SOUP_TYPE, RICE_SOUP, FLOWING_RICE_SOUP)
                .block(RICE_SOUP_BLOCK)
                .bucket(() -> ModItems.RICE_BUCKET.get());
    }

    // 饭汤 ForgeFlowingFluid 属性：桶绑定饭桶（RicePailItem）
    private static ForgeFlowingFluid.Properties makeBrothProperties() {
        return new ForgeFlowingFluid.Properties(RICE_BROTH_TYPE, RICE_BROTH, FLOWING_RICE_BROTH)
                .block(RICE_BROTH_BLOCK)
                .bucket(() -> ModItems.RICE_PAIL.get());
    }

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
        BLOCKS.register(modEventBus);
    }

    /**
     * 把米桶/饭桶注册进原版「空炼药锅」交互表（FMLCommonSetup 主线程阶段调用，此时物品注册表已就绪）。
     * 逻辑逐行对应原版 CauldronInteraction#emptyBucket(FILL_WATER)：桶对空锅使用后
     * 空锅变成装满的对应汤锅、桶变空铁桶、播放水桶倒出音效。
     */
    public static void registerCauldronInteractions() {
        CauldronInteraction.EMPTY.put(ModItems.RICE_BUCKET.get(),
                (state, level, pos, player, hand, stack) -> {
                    if (!level.isClientSide) {
                        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                                new ItemStack(Items.BUCKET)));
                        player.awardStat(Stats.FILL_CAULDRON);
                        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                        level.setBlockAndUpdate(pos, RICE_SOUP_CAULDRON.get().defaultBlockState()
                                .setValue(LayeredCauldronBlock.LEVEL, 3));
                        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                });
        // 饭桶对空锅使用：空锅变成装满的饭汤锅、饭桶变空铁桶
        CauldronInteraction.EMPTY.put(ModItems.RICE_PAIL.get(),
                (state, level, pos, player, hand, stack) -> {
                    if (!level.isClientSide) {
                        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                                new ItemStack(Items.BUCKET)));
                        player.awardStat(Stats.FILL_CAULDRON);
                        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                        level.setBlockAndUpdate(pos, RICE_BROTH_CAULDRON.get().defaultBlockState()
                                .setValue(LayeredCauldronBlock.LEVEL, 3));
                        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                });
    }
}
