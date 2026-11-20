package com.hp.rematchcage.registry;

import com.hp.rematchcage.Rematchcage;
import com.hp.rematchcage.item.RematchCageBlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Rematchcage.MOD_ID);

    public static final RegistryObject<Item> REMATCH_CAGE = ITEMS.register(
        "rematch_cage",
        () -> new RematchCageBlockItem(ModBlocks.REMATCH_CAGE.get(), new Item.Properties().rarity(Rarity.UNCOMMON))
    );

    private ModItems() {
    }

    public static void onBuildTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS || event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(REMATCH_CAGE.get());
        }
    }
}
