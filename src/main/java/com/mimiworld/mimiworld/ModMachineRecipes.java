package com.mimiworld.mimiworld;

import com.mimiworld.mimiworld.block.entity.IonizationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.PrecipitationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.RicePoundingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.RicePressBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 工作方块（非原版工作台/熔炉）配方统一注册入口。
 * 在 FMLCommonSetupEvent 的 enqueueWork 中调用（物品注册表已冻结，Item.getId 可用）。
 *
 * 对应 ModRecipeProvider 头部《配方书写规则》第 6 条：
 * 锤米台为无序双输入、压饭机为 1..9 输入多重集合，均为即时合成；
 * 电离台/沉淀台为无序双输入定时加工（DualInputProcessor，registerRecipe 第 4 参为加工 ticks）。
 */
public final class ModMachineRecipes {

    private ModMachineRecipes() {
    }

    public static void register() {
        // #6：锤米台 —— 燧石 + 5 号米原木 → 6 号米木板（两槽无序，各消耗 1 个）
        RicePoundingTableBlockEntity.registerRecipe(
                Items.FLINT,
                ModItems.byCode(5),
                new ItemStack(ModItems.byCode(6)));

        // #51：锤米台 —— 金锭 + 50 号粗超结晶 → 51 号超结晶（两槽无序，各消耗 1 个）
        RicePoundingTableBlockEntity.registerRecipe(
                Items.GOLD_INGOT,
                ModItems.byCode(50),
                new ItemStack(ModItems.byCode(51)));

        // #140：锤米台 —— 16 号结晶米 + 铁锭 → 140 号召唤核心胚
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.byCode(16),
                Items.IRON_INGOT,
                new ItemStack(ModItems.byCode(140)));

        // #143：锤米台 —— 19 号再结晶米 + 钻石 → 143 号高级召唤核心胚
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.byCode(19),
                Items.DIAMOND,
                new ItemStack(ModItems.byCode(143)));

        // #148：锤米台 —— 147 号熔融混合结晶 + 下界合金锭 → 148 号最懒召唤核心胚
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.byCode(147),
                Items.NETHERITE_INGOT,
                new ItemStack(ModItems.byCode(148)));

        // #247：锤米台 —— 左上角 55 号超重结晶 + 左下角 11 号硬化米 → 247 号超重结晶棒（镐手柄）
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.byCode(55),
                ModItems.byCode(11),
                new ItemStack(ModItems.byCode(247)));

        // #248：锤米台 —— 左上角 60 号复合米饭锭 + 左下角 16 号结晶米 → 248 号复合米饭棒
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.byCode(60),
                ModItems.byCode(16),
                new ItemStack(ModItems.byCode(248)));

        // #249：锤米台 —— 左上角 75 号清洁复合锭 + 左下角 19 号重结晶米 → 249 号清洁复合棒
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.byCode(75),
                ModItems.byCode(19),
                new ItemStack(ModItems.byCode(249)));

        // #250：锤米台 —— 左上角 80 号超懒合金锭 + 左下角 147 号熔融混合结晶 → 250 号超懒合金棒
        RicePoundingTableBlockEntity.registerRecipe(
                ModItems.byCode(80),
                ModItems.byCode(147),
                new ItemStack(ModItems.byCode(250)));

        // #32：压饭机 —— 9 个 30 号饭桶 → 1 个 32 号压缩饭桶。
        // 压饭机仅按输入槽 shrink(1) 消耗，不触发原版合成剩余物逻辑，因此【桶不返还】。
        Item[] ninePails = new Item[RicePressBlockEntity.INPUT_SLOTS];
        Item pail = ModItems.byCode(30);
        for (int i = 0; i < ninePails.length; i++) {
            ninePails[i] = pail;
        }
        RicePressBlockEntity.registerRecipe(new ItemStack(ModItems.byCode(32)), ninePails);

        // #77：电离台 —— 左上角 76 号粗制超懒合金锭 + 左下角 84 号清洁米沉离液，
        // 处理 2s（40t）→ 77 号超懒合金离子态（输入无序，两槽各消耗 1 个）
        IonizationTableBlockEntity.registerRecipe(
                ModItems.byCode(76),
                ModItems.byCode(84),
                new ItemStack(ModItems.byCode(77)),
                40);

        // #78：沉淀台 —— 左上角 77 号超懒合金离子态 + 左下角 84 号清洁米沉离液，
        // 处理 2s（40t）→ 78 号超懒合金粉（两槽各消耗 1 个）
        PrecipitationTableBlockEntity.registerRecipe(
                ModItems.byCode(77),
                ModItems.byCode(84),
                new ItemStack(ModItems.byCode(78)),
                40);
    }
}
