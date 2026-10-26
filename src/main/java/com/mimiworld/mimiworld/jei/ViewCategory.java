package com.mimiworld.mimiworld.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * mimiworld 全部工作方块共用的 JEI 分类实现。
 * <p>
 * 一台机器 = 一个本类实例（独立 RecipeType + 机器方块作图标/催化剂）。
 * 背景直接取该机器 GUI 贴图的 176 x panelHeight 区域（panelHeight 为面板中
 * 玩家背包起始行，即只截取机器操作区），槽位坐标全部是 GUI 内的绝对像素坐标，
 * 直接来自各 Menu 的 addSlot —— 因此 JEI 页面与游戏内界面逐像素一致。
 * <p>
 * 槽框、进度箭头、火焰底均已在贴图内烘焙，所以这里不再绘制 JEI 槽位背景，
 * 也不再自绘箭头/火焰，只按坐标摆放配料。
 */
public class ViewCategory implements IRecipeCategory<JeiView> {

    /** GUI 贴图统一宽度 */
    public static final int GUI_WIDTH = 176;

    private final RecipeType<JeiView> recipeType;
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;
    private final int outputX;
    private final int outputY;

    /**
     * @param texture   机器 GUI 贴图（mimiworld:textures/gui/&lt;机器&gt;.png）
     * @param panelHeight 需要展示的面板高度（不含玩家背包）
     * @param outputX   产物槽在 GUI 中的绝对坐标
     * @param outputY   产物槽在 GUI 中的绝对坐标
     */
    public ViewCategory(IGuiHelper guiHelper, RecipeType<JeiView> recipeType, ItemStack catalyst,
                        ResourceLocation texture, int panelHeight, int outputX, int outputY) {
        this.recipeType = recipeType;
        this.title = catalyst.getHoverName();
        this.outputX = outputX;
        this.outputY = outputY;
        this.background = guiHelper.createDrawable(texture, 0, 0, GUI_WIDTH, panelHeight);
        this.icon = guiHelper.createDrawableItemStack(catalyst);
    }

    @Override
    public RecipeType<JeiView> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JeiView recipe, IFocusGroup focuses) {
        for (JeiView.Slot slot : recipe.inputs()) {
            IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, slot.x(), slot.y());
            if (slot.tag() != null) {
                // 标签配料（如任意肉类）：展开为标签内全部物品轮播
                slotBuilder.addIngredients(Ingredient.of(slot.tag()));
            } else if (slot.fluid() != null) {
                // 流体配料（如米片机 500mB 水）：JEI 默认流体渲染器
                slotBuilder.addFluidStack(slot.fluid(), slot.fluidAmount());
            } else if (slot.stack() != null && !slot.stack().isEmpty()) {
                slotBuilder.addItemStack(slot.stack());
            }
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, outputX, outputY)
                .addItemStack(recipe.output());
    }
}
