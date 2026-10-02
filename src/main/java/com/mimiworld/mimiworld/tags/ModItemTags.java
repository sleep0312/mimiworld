package com.mimiworld.mimiworld.tags;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * 米米世界自定义物品标签。
 * 标签内容由 datagen（ModItemTagsProvider）生成到 data/mimiworld/tags/items/。
 */
public final class ModItemTags {

    private ModItemTags() {
    }

    /**
     * 任意肉类（生熟皆可）：原版牛/猪/鸡/羊/兔肉鱼 + 本模各系列生熟肉与腐米肉。
     * 注：Forge 1.20.1（47.4.10）的 Tags.Items 尚无 raw_meats/cooked_meats 标签，
     * 故由本模组自建标签承载用户配方规则中的“任意肉类”。
     */
    public static final TagKey<Item> MEATS =
            TagKey.create(Registries.ITEM, new ResourceLocation(MimiWorld.MODID, "meats"));
}
