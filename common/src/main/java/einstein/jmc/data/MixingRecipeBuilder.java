package einstein.jmc.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import einstein.jmc.block.entity.CeramicBowlBlockEntity;
import einstein.jmc.init.ModRecipes;
import einstein.jmc.util.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

public class MixingRecipeBuilder implements RecipeBuilder {

    private final RecipeCategory category;
    private final NonNullList<Ingredient> ingredients;
    private final Item result;
    private final ResourceLocation contents;
    private final int count;
    private final int mixingTime;
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

    private MixingRecipeBuilder(RecipeCategory category, NonNullList<Ingredient> ingredients, ItemLike result, ResourceLocation contents, int count, int mixingTime) {
        this.category = category;
        this.ingredients = ingredients;
        this.result = result.asItem();
        this.contents = contents;
        this.count = count;
        this.mixingTime = mixingTime;
    }

    public static MixingRecipeBuilder mixing(RecipeCategory category, ItemLike result, ResourceLocation contents, int mixingTime, ItemLike... ingredients) {
        return mixing(category, result, contents, 1, mixingTime, ingredients);
    }

    public static MixingRecipeBuilder mixing(RecipeCategory category, ItemLike result, ResourceLocation contents, int count, int mixingTime, ItemLike... ingredients) {
        return mixing(category, result, contents, count, mixingTime, Arrays.stream(ingredients).map(Ingredient::of).toArray(Ingredient[]::new));
    }

    public static MixingRecipeBuilder mixing(RecipeCategory category, ItemLike result, ResourceLocation contents, int mixingTime, Ingredient... ingredients) {
        return mixing(category, result, contents, 1, mixingTime, ingredients);
    }

    public static MixingRecipeBuilder mixing(RecipeCategory category, ItemLike result, ResourceLocation contents, int count, int mixingTime, Ingredient... ingredients) {
        if (mixingTime < 1) {
            throw new IllegalStateException("mixingTime must be a positive number");
        }

        if (ingredients.length > CeramicBowlBlockEntity.INGREDIENT_SLOT_COUNT) {
            throw new IllegalStateException("Too many ingredients for mixing recipe. The max is 4");
        }

        NonNullList<Ingredient> ingredientsList = NonNullList.create();
        Collections.addAll(ingredientsList, ingredients);
        return new MixingRecipeBuilder(category, ingredientsList, result, contents, count, mixingTime);
    }

    @Override
    public RecipeBuilder unlockedBy(String name, CriterionTriggerInstance trigger) {
        advancement.addCriterion(name, trigger);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public Item getResult() {
        return result;
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
        if (advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe: " + recipeId);
        }

        advancement.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
                .rewards(AdvancementRewards.Builder.recipe(recipeId)).requirements(RequirementsStrategy.OR);
        consumer.accept(new Result(recipeId, this, recipeId.withPrefix("recipes/" + category.getFolderName() + "/")));
    }

    public record Result(ResourceLocation recipeId, MixingRecipeBuilder builder,
                         ResourceLocation advancementId) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonArray jsonIngredients = new JsonArray(CeramicBowlBlockEntity.INGREDIENT_SLOT_COUNT);

            for (Ingredient ingredient : builder.ingredients) {
                jsonIngredients.add(ingredient.toJson());
            }

            json.add("ingredients", jsonIngredients);
            json.addProperty("mixingTime", builder.mixingTime);
            json.addProperty("contents", builder.contents.toString());
            Util.serializeResult(json, builder.result, builder.count);
        }

        @Override
        public ResourceLocation getId() {
            return recipeId;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ModRecipes.MIXING_SERIALIZER.get();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return builder.advancement.serializeToJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return advancementId;
        }
    }
}
