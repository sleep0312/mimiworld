package com.mimiworld.mimiworld.datagen;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.ModItems;
import com.mimiworld.mimiworld.tags.ModItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * 米米世界物品标签数据生成器。
 * runData 后生成 src/generated/resources/data/mimiworld/tags/items/*.json。
 * 继承原版 ItemTagsProvider 才能直接 add(Item...)（Forge 47.4.10 的裸 TagsProvider.TagAppender 只收 ResourceKey）。
 */
public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                               ExistingFileHelper existingFileHelper) {
        super(output, registries, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()),
                MimiWorld.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 任意肉类（生/熟、陆地/鱼类、腐肉均算）：
        // 原版生肉
        tag(ModItemTags.MEATS).add(
                Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.RABBIT,
                Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH,
                // 原版熟肉
                Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_CHICKEN, Items.COOKED_MUTTON,
                Items.COOKED_RABBIT, Items.COOKED_COD, Items.COOKED_SALMON,
                // 原版腐肉
                Items.ROTTEN_FLESH,
                // 本模生肉 5 种、熟肉 5 种与腐米肉（顺序见下）
                ModItems.RAW_MIMOO_BEEF.get(), ModItems.RAW_MIMI_MUTTON.get(), ModItems.RAW_MIHENG_PORK.get(),
                ModItems.RAW_TANMI_FISH.get(), ModItems.ZHUOMI_CHICKEN.get(),
                ModItems.COOKED_ZHUOMI_CHICKEN.get(), ModItems.COOKED_MIMOO_BEEF.get(), ModItems.COOKED_MIMI_MUTTON.get(),
                ModItems.COOKED_MIHENG_PORK.get(), ModItems.COOKED_TANMI_FISH.get(),
                ModItems.ROTTEN_RICE_FLESH.get());
    }
}
