package com.mimiworld.mimiworld;

import com.mimiworld.mimiworld.enchantment.ModEnchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Comparator;
import java.util.List;

/**
 * 米米世界自定义创造模式标签页。
 * 标签页 ID：mimi（完整注册名 mimiworld:mimi）
 * 自动收录所有命名空间为 mimiworld 的已注册物品（方块物品 BlockItem 也在物品注册表中，会一并收录）。
 *
 * <p>收录顺序：先按功能分类，再按注册 ID 字母序排列，避免物品按注册先后堆积显得杂乱。
 * 分类顺序为：食物与饮品 → 材料与杂项 → 工具 → 武器 → 防具 → 机器与功能方块 →
 * 建筑与装饰方块 → 头颅摆件 → 生物蛋。</p>
 */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MimiWorld.MODID);

    /** 食物与饮品（含药水食物）。 */
    private static final int RANK_FOOD = 0;
    /** 材料、零件、溶液及其他非方块物品。 */
    private static final int RANK_MATERIAL = 1;
    /** 镐/斧/锹/锄等挖掘工具及锤类工具。 */
    private static final int RANK_TOOL = 2;
    /** 剑、弓、箭等战斗武器。 */
    private static final int RANK_WEAPON = 3;
    /** 头盔/胸甲/护腿/靴子等防具。 */
    private static final int RANK_ARMOR = 4;
    /** 带方块实体的机器与功能方块。 */
    private static final int RANK_MACHINE = 5;
    /** 无方块实体的建筑与装饰方块。 */
    private static final int RANK_BLOCK = 6;
    /** 各 Boss / 生物头颅摆件。 */
    private static final int RANK_HEAD = 7;
    /** 生物蛋。 */
    private static final int RANK_SPAWN_EGG = 8;

    public static final RegistryObject<CreativeModeTab> MIMI_TAB = CREATIVE_MODE_TABS.register("mimi", () ->
            CreativeModeTab.builder()
                    // 图标：界天脑花
                    .icon(() -> ModItems.REALM_HEAVEN_BRAIN_FLOWER.get().getDefaultInstance())
                    .title(Component.translatable("itemGroup.mimiworld.mimi"))
                    .displayItems((parameters, output) -> {
                        // 本模组所有物品/方块物品，按功能分类 + 注册 ID 字母序稳定排序
                        List<Item> sortedItems = ForgeRegistries.ITEMS.getValues().stream()
                                .filter(item -> {
                                    ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                                    return id != null && id.getNamespace().equals(MimiWorld.MODID);
                                })
                                .sorted(Comparator.comparingInt(ModCreativeTabs::categoryRank)
                                        .thenComparing(ModCreativeTabs::registryPath))
                                .toList();
                        sortedItems.forEach(output::accept);

                        // 泣血附魔书 I～V：与原版耐久 I/II/III 附魔书一致，
                        // 使用原版附魔书物品与原版纹理，每本仅在 StoredEnchantments 中存储的等级不同
                        for (int level = 1; level <= ModEnchantments.WEEPING_BLOOD.get().getMaxLevel(); level++) {
                            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                            EnchantedBookItem.addEnchantment(book,
                                    new EnchantmentInstance(ModEnchantments.WEEPING_BLOOD.get(), level));
                            output.accept(book);
                        }

                        // 完美无缺附魔书：单级附魔，同样使用原版附魔书物品与原版纹理
                        ItemStack flawlessBook = new ItemStack(Items.ENCHANTED_BOOK);
                        EnchantedBookItem.addEnchantment(flawlessBook,
                                new EnchantmentInstance(ModEnchantments.FLAWLESS.get(), 1));
                        output.accept(flawlessBook);
                    })
                    .build());

    /**
     * 物品分类排序权重，数值越小越靠前。
     *
     * @param item 待分类物品
     * @return 分类权重
     */
    private static int categoryRank(Item item) {
        String path = registryPath(item);
        if (item instanceof ForgeSpawnEggItem) {
            return RANK_SPAWN_EGG;
        }
        if (item instanceof ArmorItem) {
            return RANK_ARMOR;
        }
        if (item instanceof SwordItem || item instanceof BowItem || item instanceof ArrowItem) {
            return RANK_WEAPON;
        }
        // 挖掘类工具（镐/斧/锹/锄）与自定义锤类工具
        if (item instanceof DiggerItem || path.endsWith("_hammer")) {
            return RANK_TOOL;
        }
        if (item.isEdible()) {
            return RANK_FOOD;
        }
        // 头颅摆件（排除镐头类材料）
        if (path.endsWith("_head") && !path.endsWith("_pickaxe_head")) {
            return RANK_HEAD;
        }
        if (item instanceof BlockItem blockItem) {
            return blockItem.getBlock() instanceof EntityBlock ? RANK_MACHINE : RANK_BLOCK;
        }
        return RANK_MATERIAL;
    }

    /**
     * 读取物品注册 ID 的路径部分，用于同分类内的字母序排序。
     *
     * @param item 已注册物品
     * @return 注册 ID 路径；异常情况下返回空串
     */
    private static String registryPath(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        return id == null ? "" : id.getPath();
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
