package com.mimiworld.mimiworld.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * mimiworld 全部工作方块共用的 JEI 分类实现。
 * <p>
 * 一台机器 = 一个本类实例（独立 RecipeType + 机器方块作图标/催化剂）。
 * 布局为标准「左输入区（cols×rows 个标准槽位）→ 箭头 → 右产物槽」，
 * 底部预留一条 16px 横带：燃料机器在左侧画火焰，无序配方在右侧放无序标记。
 * 槽位的实际坐标全部来自 {@link JeiView}（每族机器只有一份映射）。
 */
public class ViewCategory implements IRecipeCategory<JeiView> {

    /** 原版熔炉 GUI 贴图：箭头与火焰图标直接复用，避免自绘 */
    private static final ResourceLocation FURNACE_GUI =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/furnace.png");

    private static final int SLOT = 18;
    private static final int PAD = 7;
    private static final int GAP = 6;
    private static final int ARROW_W = 24;
    private static final int ARROW_H = 17;
    private static final int FLAME_W = 14;
    private static final int FLAME_H = 14;
    /** 底部横带高度（放火焰 / 无序标记） */
    private static final int STRIP = 16;

    private final RecipeType<JeiView> recipeType;
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotBackground;
    private final IDrawable arrow;
    private final IDrawable flame;

    private final int width;
    private final int height;
    private final int arrowX;
    private final int arrowY;
    private final int outputX;
    private final int outputY;
    private final int shapelessX;
    private final int shapelessY;
    private final int flameX;
    private final int flameY;
    private final boolean fuel;

    public ViewCategory(IGuiHelper guiHelper, RecipeType<JeiView> recipeType,
                        ItemStack catalyst, int cols, int rows, boolean fuel) {
        this.recipeType = recipeType;
        this.title = catalyst.getHoverName();
        this.fuel = fuel;

        this.width = PAD + cols * SLOT + GAP + ARROW_W + GAP + SLOT + PAD;
        this.height = PAD + rows * SLOT + STRIP + PAD;

        this.background = guiHelper.createBlankDrawable(width, height);
        this.icon = guiHelper.createDrawableItemStack(catalyst);
        this.slotBackground = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.createDrawable(FURNACE_GUI, 176, 14, ARROW_W, ARROW_H);
        this.flame = guiHelper.createDrawable(FURNACE_GUI, 176, 0, FLAME_W, FLAME_H);

        this.arrowX = PAD + cols * SLOT + GAP;
        this.arrowY = PAD + (rows * SLOT - ARROW_H) / 2;
        this.outputX = arrowX + ARROW_W + GAP;
        this.outputY = PAD + (rows * SLOT - SLOT) / 2;

        this.shapelessX = width - PAD - 15;
        this.shapelessY = PAD + rows * SLOT + 1;
        this.flameX = PAD + 1;
        this.flameY = shapelessY;
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
            IRecipeSlotBuilder slotBuilder = builder
                    .addSlot(RecipeIngredientRole.INPUT, slot.x(), slot.y())
                    .setBackground(slotBackground, -1, -1);
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
                .setBackground(slotBackground, -1, -1)
                .addItemStack(recipe.output());

        if (recipe.shapeless()) {
            builder.setShapeless(shapelessX, shapelessY);
        }
    }

    @Override
    public void draw(JeiView recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        arrow.draw(guiGraphics, arrowX, arrowY);
        if (fuel) {
            flame.draw(guiGraphics, flameX, flameY);
        }
    }
}
