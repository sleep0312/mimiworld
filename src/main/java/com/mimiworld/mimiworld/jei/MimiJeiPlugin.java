package com.mimiworld.mimiworld.jei;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.BloodlettingTableBlockEntity;
import com.mimiworld.mimiworld.block.entity.CrusherBlockEntity;
import com.mimiworld.mimiworld.block.entity.DissociationTableBlockEntity;
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

        // ---- A. MultiInputProcessor（pos 表 = inputs 下标 → GUI 绝对槽坐标）----
        // 搅碎器：槽0 左上(26,17)、槽1 左下(26,50)、槽2 右上(121,17)
        add(gui, "crusher", ModBlocks.CRUSHER.get(), 84, 121, 50,
                () -> JeiLayouts.multi(CrusherBlockEntity.jeiRecipes(),
                        new int[][]{{26, 17}, {26, 50}, {121, 17}}));
        // 碾磨台：槽0~2 左列(26)、槽3~5 右列(130)，y=15/36/57
        add(gui, "grinding_table", ModBlocks.GRINDING_TABLE.get(), 116, 79, 81,
                () -> JeiLayouts.multi(GrindingTableBlockEntity.jeiRecipes(),
                        new int[][]{{26, 15}, {26, 36}, {26, 57}, {130, 15}, {130, 36}, {130, 57}}));
        // 淬剑台：槽0 (35,15)、槽1 (121,15)、槽2 (79,38)
        add(gui, "sword_quenching_table", ModBlocks.SWORD_QUENCHING_TABLE.get(), 84, 79, 61,
                () -> JeiLayouts.multi(SwordQuenchingTableBlockEntity.jeiRecipes(),
                        new int[][]{{35, 15}, {121, 15}, {79, 38}}));
        // 汲血台：槽0 (44,18)、槽1 (44,50)；标签配方（如任意肉类 + 玻璃瓶）并入同一分类
        add(gui, "bloodletting_table", ModBlocks.BLOODLETTING_TABLE.get(), 84, 116, 33,
                () -> {
                    List<JeiView> views = new ArrayList<>(JeiLayouts.multi(
                            BloodlettingTableBlockEntity.jeiRecipes(),
                            new int[][]{{44, 18}, {44, 50}}));
                    views.addAll(JeiLayouts.bloodTags(
                            BloodlettingTableBlockEntity.jeiTaggedRecipes()));
                    return views;
                });

        // ---- B. DualInputProcessor（两槽；单输入配方只画 A 槽）----
        add(gui, "ionization_table", ModBlocks.IONIZATION_TABLE.get(), 84, 102, 33,
                () -> JeiLayouts.dual(IonizationTableBlockEntity.jeiRecipes()));
        add(gui, "precipitation_table", ModBlocks.PRECIPITATION_TABLE.get(), 84, 102, 33,
                () -> JeiLayouts.dual(PrecipitationTableBlockEntity.jeiRecipes()));

        // ---- C. 离解台（上槽 (35,24) + 下槽 (79,53)）----
        add(gui, "dissociation_table", ModBlocks.DISSOCIATION_TABLE.get(), 84, 121, 24,
                () -> JeiLayouts.dissociation(DissociationTableBlockEntity.jeiRecipes()));

        // ---- D. 网格工作台（即时合成）----
        add(gui, "pestle_realm_table", ModBlocks.PESTLE_REALM_TABLE.get(), 84, 116, 44,
                () -> JeiLayouts.gridCrafting(2, 38, 15, 22, 22,
                        PestleRealmTableBlockEntity.jeiRecipes()));
        add(gui, "ingot_condensation_table", ModBlocks.INGOT_CONDENSATION_TABLE.get(), 154, 79, 115,
                () -> JeiLayouts.gridCrafting(5, 43, 13, 18, 18,
                        IngotCondensationTableBlockEntity.jeiRecipes()));

        // ---- E. 煮锅族（NxN 网格 (8,17) 间距 18 + 右上独立输入槽）----
        add(gui, "large_cooking_pot", ModBlocks.LARGE_COOKING_POT.get(), 114, 116, 71,
                () -> JeiLayouts.pot(3, 116, 35, LargeCookingPotBlockEntity.jeiRecipes()));
        add(gui, "jumbo_mega_cooking_pot", ModBlocks.JUMBO_MEGA_COOKING_POT.get(), 150, 116, 89,
                () -> JeiLayouts.pot(5, 116, 53, JumboMegaCookingPotBlockEntity.jeiRecipes()));
        add(gui, "iron_wok", ModBlocks.IRON_WOK.get(), 96, 116, 74,
                () -> JeiLayouts.ironWok(IronWokBlockEntity.jeiRecipes()));
        add(gui, "oven", ModBlocks.OVEN.get(), 114, 116, 71,
                () -> JeiLayouts.pot(3, 116, 35, OvenBlockEntity.jeiRecipes()));

        // ---- F. 搅拌器（两列四行 + 中央槽，无序即时合成）----
        add(gui, "mixer", ModBlocks.MIXER.get(), 152, 79, 115,
                () -> JeiLayouts.mixer(MixerBlockEntity.jeiRecipes()));

        // ---- G. 米系早期机器 ----
        add(gui, "rice_pounding_table", ModBlocks.RICE_POUNDING_TABLE.get(), 84, 112, 26,
                () -> JeiLayouts.pounding(RicePoundingTableBlockEntity.jeiRecipes()));

        // ---- H. 米片机（原料 + 水（流体槽，500mB）→ 米）----
        add(gui, "rice_maker", ModBlocks.RICE_MAKER.get(), 84, 112, 43,
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

    /**
     * 注册一台机器：背景贴图固定为 assets/mimiworld/textures/gui/&lt;name&gt;.png，
     * panelHeight 为需要展示的面板高度（玩家背包起始行），outputX/Y 为产物槽绝对坐标。
     */
    private void add(IGuiHelper gui, String name, Block block, int panelHeight, int outputX, int outputY,
                     Supplier<List<JeiView>> views) {
        mezz.jei.api.recipe.RecipeType<JeiView> type =
                mezz.jei.api.recipe.RecipeType.create(MimiWorld.MODID, name, JeiView.class);
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MimiWorld.MODID,
                "textures/gui/" + name + ".png");
        ItemStack catalyst = new ItemStack(block);
        machines.add(new MachineDef(type,
                new ViewCategory(gui, type, catalyst, texture, panelHeight, outputX, outputY),
                views, catalyst));
    }
}
