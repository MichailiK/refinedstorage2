package com.refinedmods.refinedstorage2.fabric.init;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RefinedStorage2Items {
    private BlockItem cable;

    public void register(String namespace, RefinedStorage2Blocks blocks, ItemGroup itemGroup) {
        cable = new BlockItem(blocks.getCable(), new Item.Settings().group(itemGroup));
        Registry.register(Registry.ITEM, new Identifier(namespace, "cable"), cable);
    }
}
