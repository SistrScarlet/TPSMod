package com.sistr.rshud.datagen;

import com.sistr.rshud.setup.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(Registration.GOGGLE_ITEM.get())
                .patternLine("SSS")
                .patternLine("SRS")
                .patternLine(" S ")
                .key('S', net.minecraftforge.common.Tags.Items.STONE)
                .key('R', Tags.Items.DUSTS_REDSTONE)
                .addCriterion("has_redstone", this.hasItem(Tags.Items.DUSTS_REDSTONE))
                .build(consumer);
    }
}
