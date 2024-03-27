package einstein.jmc.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import einstein.jmc.block.entity.CeramicBowlBlockEntity;
import einstein.jmc.init.ModRecipes;
import einstein.jmc.item.crafting.CountedIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
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
    private final NonNullList<CountedIngredient> ingredients;
    private final Item result;
    private final int mixingTime;
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

    public MixingRecipeBuilder(RecipeCategory category, NonNullList<CountedIngredient> ingredients, ItemLike result, int mixingTime) {
        this.category = category;
        this.ingredients = ingredients;
        this.result = result.asItem();
        this.mixingTime = mixingTime;
    }

    public static MixingRecipeBuilder mixing(RecipeCategory category, ItemLike result, int mixingTime, Ingredient... ingredients) {
        return mixing(category, result, mixingTime, Arrays.stream(ingredients).map(CountedIngredient::new).toArray(CountedIngredient[]::new));
    }

    public static MixingRecipeBuilder mixing(RecipeCategory category, ItemLike result, int mixingTime, CountedIngredient... ingredients) {
        if (mixingTime < 1) {
            throw new IllegalStateException("mixingTime must be a positive number");
        }

        if (ingredients.length > CeramicBowlBlockEntity.SLOT_COUNT) {
            throw new IllegalStateException("Too many ingredients for mixing recipe. The max is 4");
        }

        NonNullList<CountedIngredient> ingredientsList = NonNullList.create();
        Collections.addAll(ingredientsList, ingredients);
        return new MixingRecipeBuilder(category, ingredientsList, result, mixingTime);
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
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        }

        advancement.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
                .rewards(AdvancementRewards.Builder.recipe(recipeId)).requirements(RequirementsStrategy.OR);
        consumer.accept(new Result(recipeId, ingredients, result, mixingTime, recipeId.withPrefix("recipes/" + category.getFolderName() + "/"), advancement));
    }

    public record Result(ResourceLocation recipeId, NonNullList<CountedIngredient> ingredients, Item result,
                         int mixingTime, ResourceLocation advancementId,
                         Advancement.Builder advancement) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonArray jsonIngredients = new JsonArray(CeramicBowlBlockEntity.SLOT_COUNT);

            for (CountedIngredient ingredient : ingredients) {
                jsonIngredients.add(ingredient.toJson());
            }

            json.add("ingredients", jsonIngredients);
            json.addProperty("mixingTime", mixingTime);
            json.addProperty("result", BuiltInRegistries.ITEM.getKey(result).toString());
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
            return advancement.serializeToJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return advancementId;
        }
    }
}
