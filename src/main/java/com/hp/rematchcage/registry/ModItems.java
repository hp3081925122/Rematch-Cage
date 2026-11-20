package com.hp.rematchcage.registry;

import com.hp.rematchcage.Rematchcage;
import com.hp.rematchcage.item.RematchCageBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Rematchcage.MOD_ID);

    public static final DeferredHolder<Item, Item> REMATCH_CAGE = ITEMS.register(
        "rematch_cage",
        () -> new RematchCageBlockItem(
            ModBlocks.REMATCH_CAGE.get(),
            new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, Rematchcage.id("rematch_cage")))
                .useBlockDescriptionPrefix()
                .rarity(Rarity.UNCOMMON)
        )
    );

    private ModItems() {
    }

    public static void onBuildTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS || event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(REMATCH_CAGE.get());
        }
    }
}
