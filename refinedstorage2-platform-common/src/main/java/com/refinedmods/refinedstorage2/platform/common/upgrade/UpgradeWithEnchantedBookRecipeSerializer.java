package com.refinedmods.refinedstorage2.platform.common.upgrade;

import java.util.Objects;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class UpgradeWithEnchantedBookRecipeSerializer implements RecipeSerializer<UpgradeWithEnchantedBookRecipe> {
    @Override
    @SuppressWarnings("deprecation") // Forge deprecates BuiltinRegistries
    public UpgradeWithEnchantedBookRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
        final JsonObject enchantmentInfo = json.getAsJsonObject("enchantment");
        final Item resultItem = BuiltInRegistries.ITEM.get(
                new ResourceLocation(json.getAsJsonPrimitive("result").getAsString())
        );
        final ResourceLocation enchantment = new ResourceLocation(enchantmentInfo.getAsJsonPrimitive("id").getAsString());
        int level = 1;
        if (enchantmentInfo.has("level")) {
            level = enchantmentInfo.getAsJsonPrimitive("level").getAsInt();
        }
        return new UpgradeWithEnchantedBookRecipe(recipeId, enchantment, level, new ItemStack(resultItem));
    }

    @Override
    @SuppressWarnings("deprecation") // Forge deprecates BuiltinRegistries
    public UpgradeWithEnchantedBookRecipe fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
        final ItemStack result = buffer.readItem();
        final ResourceLocation enchantment = buffer.readResourceLocation();
        final int level = buffer.readInt();
        return new UpgradeWithEnchantedBookRecipe(recipeId, Objects.requireNonNull(enchantment), level, result);
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buf, final UpgradeWithEnchantedBookRecipe recipe) {
        buf.writeItem(recipe.getResult());
        buf.writeResourceLocation(Objects.requireNonNull(recipe.getEnchantmentId()));
        buf.writeInt(recipe.getEnchantmentLevel());
    }
}
