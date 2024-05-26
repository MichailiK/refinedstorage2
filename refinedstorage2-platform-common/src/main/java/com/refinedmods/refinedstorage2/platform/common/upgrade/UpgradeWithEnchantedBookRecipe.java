package com.refinedmods.refinedstorage2.platform.common.upgrade;

import com.refinedmods.refinedstorage2.platform.common.content.Items;

import java.util.Objects;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import static java.util.Objects.requireNonNull;

public class UpgradeWithEnchantedBookRecipe extends ShapedRecipe {

    private final ResourceLocation enchantmentId;
    private final int level;
    private final ItemStack resultItem;

    UpgradeWithEnchantedBookRecipe(final ResourceLocation recipeId,
                                   final ResourceLocation enchantmentId,
                                   final int level,
                                   final ItemStack resultItem) {
        super(recipeId, "", CraftingBookCategory.MISC, 3, 3, NonNullList.of(
            Ingredient.EMPTY,
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(
                getEnchantment(enchantmentId),
                level
            ))),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(new ItemStack(Blocks.BOOKSHELF)),
            Ingredient.of(new ItemStack(Items.INSTANCE.getUpgrade())),
            Ingredient.of(new ItemStack(Blocks.BOOKSHELF)),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron())),
            Ingredient.of(new ItemStack(Items.INSTANCE.getQuartzEnrichedIron()))
        ), new ItemStack(resultItem.getItemHolder().value()));
        this.enchantmentId = enchantmentId;
        this.level = level;
        this.resultItem = resultItem;
    }

    private static Enchantment getEnchantment(final ResourceLocation enchantmentId) {
        return requireNonNull(BuiltInRegistries.ENCHANTMENT.get(enchantmentId));
    }

    ResourceLocation getEnchantmentId() {
        return enchantmentId;
    }

    int getEnchantmentLevel() {
        return level;
    }

    ItemStack getResult() {
        return resultItem;
    }

    @Override
    public boolean matches(final CraftingContainer craftingContainer, final Level theLevel) {
        if (!super.matches(craftingContainer, theLevel)) {
            return false;
        }
        final ListTag enchantments = EnchantedBookItem.getEnchantments(craftingContainer.getItem(1));
        for (int i = 0; i < enchantments.size(); ++i) {
            final CompoundTag tag = enchantments.getCompound(i);
            final int containerLevel = EnchantmentHelper.getEnchantmentLevel(tag);
            final ResourceLocation containerEnchantment = EnchantmentHelper.getEnchantmentId(tag);
            if (Objects.equals(containerEnchantment, getEnchantmentId()) && containerLevel == level) {
                return true;
            }
        }
        return false;
    }
}
