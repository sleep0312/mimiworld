package com.mimiworld.mimiworld.jei;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.BloodlettingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.CookingPotBlockEntity;
import com.mimiworld.mimiworld.block.entity.CrusherBlockEntity;
import com.mimiworld.mimiworld.block.entity.DissociationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.ExtraLargeCookingPotBlockEntity;
import com.mimiworld.mimiworld.block.entity.GrindingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.IngotCondensationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.IonizationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.IronWokBlockEntity;
import com.mimiworld.mimiworld.block.entity.JumboMegaCookingPotBlockEntity;
import com.mimiworld.mimiworld.block.entity.LargeCookingPotBlockEntity;
import com.mimiworld.mimiworld.block.entity.MixerBlockEntity;
import com.mimiworld.mimiworld.block.entity.OvenBlockEntity;
import com.mimiworld.mimiworld.block.entity.PestleRealmTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.PrecipitationTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.RiceMakerBlockEntity;
import com.mimiworld.mimiworld.block.entity.RicePoundingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.RicePressBlockEntity;
import com.mimiworld.mimiworld.block.entity.AdvancedRicePoundingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.SwordQuenchingTableBlockEntity;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * mimiworld 工作方块 JEI 插件入口（JEI 反射加载，未安装 JEI 时本类不会被加载）。
 * <p>
 * 每台工作方块注册一个独立 JEI 分类（图标/催化剂=机器方块，标题=方块名），
 * 配方直接读各 BlockEntity 的 {@code jeiRecipes()} 只读快照——这些快照由
 * ModMachineRecipes 在 FMLCommonSetup 阶段填充，早于 JEI 注册阶段。
 */
@JeiPlugin
public class MimiJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID =
            ResourceLocation.fromNamespaceAndPath(MimiWorld.MODID, "jei_plugin");

    /** 一台机器的全部注册信息（在 registerCategories 阶段一次性构建） */
    private record MachineDef(mezz.jei.api.recipe.RecipeType<JeiView> type,
                              ViewCategory category,
                              Supplier<List<JeiView>> views,
                              ItemStack catalyst) {
    }

    private final List<MachineDef> machines = new ArrayList<>();

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        machines.clear();
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();

        // ---- A. MultiInputProcessor（有序槽；pos 表 = inputs 下标 → 槽格列/行）----
        // 搅碎器：槽0 左上、槽1 左下、槽2 右上
        add(gui, "crusher", ModBlocks.CRUSHER.get(), 2, 2, false,
                () -> JeiLayouts.multi(CrusherBlockEntity.jeiRecipes(),
                        new int[][]{{0, 0}, {0, 1}, {1, 0}}));
        // 碾磨台：槽0~2 左列上中下、槽3~5 右列上中下
        add(gui, "grinding_table", ModBlocks.GRINDING_TABLE.get(), 2, 3, false,
                () -> JeiLayouts.multi(GrindingTableBlockEntity.jeiRecipes(),
                        new int[][]{{0, 0}, {0, 1}, {0, 2}, {1, 0}, {1, 1}, {1, 2}}));
        // 淬剑台：槽0 左上、槽1 右上（隔一列）、槽2 居中下
        add(gui, "sword_quenching_table", ModBlocks.SWORD_QUENCHING_TABLE.get(), 3, 2, false,
                () -> JeiLayouts.multi(SwordQuenchingTableBlockEntity.jeiRecipes(),
                        new int[][]{{0, 0}, {2, 0}, {1, 1}}));
        // 汲血台：槽0 左上、槽1 左下；标签配方（如任意肉类 + 玻璃瓶）并入同一分类
        add(gui, "bloodletting_table", ModBlocks.BLOODLETTING_TABLE.get(), 1, 2, false,
                () -> {
                    List<JeiView> views = new ArrayList<>(JeiLayouts.multi(
                            BloodlettingTableBlockEntity.jeiRecipes(),
                            new int[][]{{0, 0}, {0, 1}}));
                    views.addAll(JeiLayouts.bloodTags(
                            BloodlettingTableBlockEntity.jeiTaggedRecipes()));
                    return views;
                });

        // ---- B. DualInputProcessor（两槽无序；单输入配方只画一槽）----
        add(gui, "ionization_table", ModBlocks.IONIZATION_TABLE.get(), 2, 1, false,
                () -> JeiLayouts.dual(IonizationTableBlockEntity.jeiRecipes()));
        add(gui, "precipitation_table", ModBlocks.PRECIPITATION_TABLE.get(), 2, 1, false,
                () -> JeiLayouts.dual(PrecipitationTableBlockEntity.jeiRecipes()));

        // ---- C. 离解台（上槽 + 下槽）----
        add(gui, "dissociation_table", ModBlocks.DISSOCIATION_TABLE.get(), 1, 2, false,
                () -> JeiLayouts.dissociation(DissociationTableBlockEntity.jeiRecipes()));

        // ---- D. 网格工作台（即时合成）----
        add(gui, "pestle_realm_table", ModBlocks.PESTLE_REALM_TABLE.get(), 2, 2, false,
                () -> JeiLayouts.gridCrafting(2, PestleRealmTableBlockEntity.jeiRecipes()));
        add(gui, "ingot_condensation_table", ModBlocks.INGOT_CONDENSATION_TABLE.get(), 5, 5, false,
                () -> JeiLayouts.gridCrafting(5, IngotCondensationTableBlockEntity.jeiRecipes()));

        // ---- E. 煮锅族（NxN 网格 + 右上额外槽，需燃料）----
        add(gui, "cooking_pot", ModBlocks.COOKING_POT.get(), 3, 2, true,
                () -> JeiLayouts.pot(2, CookingPotBlockEntity.jeiRecipes()));
        add(gui, "large_cooking_pot", ModBlocks.LARGE_COOKING_POT.get(), 4, 3, true,
                () -> JeiLayouts.pot(3, LargeCookingPotBlockEntity.jeiRecipes()));
        add(gui, "extra_large_cooking_pot", ModBlocks.EXTRA_LARGE_COOKING_POT.get(), 5, 4, true,
                () -> JeiLayouts.pot(4, ExtraLargeCookingPotBlockEntity.jeiRecipes()));
        add(gui, "jumbo_mega_cooking_pot", ModBlocks.JUMBO_MEGA_COOKING_POT.get(), 6, 5, true,
                () -> JeiLayouts.pot(5, JumboMegaCookingPotBlockEntity.jeiRecipes()));
        add(gui, "iron_wok", ModBlocks.IRON_WOK.get(), 5, 2, true,
                () -> JeiLayouts.ironWok(IronWokBlockEntity.jeiRecipes()));
        add(gui, "oven", ModBlocks.OVEN.get(), 4, 3, true,
                () -> JeiLayouts.pot(3, OvenBlockEntity.jeiRecipes()));

        // ---- F. 搅拌器（两列四行 + 额外槽，无序即时合成）----
        add(gui, "mixer", ModBlocks.MIXER.get(), 3, 4, false,
                () -> JeiLayouts.mixer(MixerBlockEntity.jeiRecipes()));

        // ---- G. 米系早期机器 ----
        add(gui, "rice_pounding_table", ModBlocks.RICE_POUNDING_TABLE.get(), 2, 1, false,
                () -> JeiLayouts.pounding(RicePoundingTableBlockEntity.jeiRecipes()));
        add(gui, "advanced_rice_pounding_table", ModBlocks.ADVANCED_RICE_POUNDING_TABLE.get(), 2, 1, false,
                () -> JeiLayouts.advancedPounding(AdvancedRicePoundingTableBlockEntity.jeiRecipes()));
        add(gui, "rice_press", ModBlocks.RICE_PRESS.get(), 3, 3, false,
                () -> JeiLayouts.press(RicePressBlockEntity.jeiRecipes()));

        // ---- H. 米片机（原料 + 水（流体槽，500mB）→ 米）----
        add(gui, "rice_maker", ModBlocks.RICE_MAKER.get(), 2, 1, false,
                () -> JeiLayouts.riceMaker(RiceMakerBlockEntity.jeiRecipes()));

        for (MachineDef def : machines) {
            registration.addRecipeCategories(def.category());
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        for (MachineDef def : machines) {
            registration.addRecipes(def.type(), def.views().get());
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (MachineDef def : machines) {
            registration.addRecipeCatalyst(def.catalyst(), def.type());
        }
    }

    private void add(IGuiHelper gui, String name, Block block, int cols, int rows, boolean fuel,
                     Supplier<List<JeiView>> views) {
        mezz.jei.api.recipe.RecipeType<JeiView> type =
                mezz.jei.api.recipe.RecipeType.create(MimiWorld.MODID, name, JeiView.class);
        ItemStack catalyst = new ItemStack(block);
        machines.add(new MachineDef(type,
                new ViewCategory(gui, type, catalyst, cols, rows, fuel),
                views, catalyst));
    }
}
