package com.hp.rematchcage.item;

import com.hp.rematchcage.block.RematchCageBlockEntity;
import com.hp.rematchcage.config.RematchMappingManager;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class RematchCageBlockItem extends BlockItem {
    public RematchCageBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        Item.TooltipContext context,
        TooltipDisplay display,
        Consumer<Component> builder,
        TooltipFlag flag
    ) {
        TypedEntityData<BlockEntityType<?>> entityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (entityData != null) {
            CompoundTag beTag = entityData.copyTagWithoutId();
            Optional<String> rawId = beTag.getString(RematchCageBlockEntity.TAG_ENTITY_ID);
            if (rawId.isPresent()) {
                Identifier id = Identifier.tryParse(rawId.get());
                if (id != null) {
                    Optional<EntityType<?>> entityOpt = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
                    if (entityOpt.isPresent()) {
                        EntityType<?> entityType = entityOpt.get();
                        builder.accept(Component.translatable("tooltip.rematchcage.stored_entity", entityType.getDescription()).withStyle(ChatFormatting.AQUA));
                        Optional<ItemStack> rep = RematchMappingManager.findRepresentativeItem(entityType);
                        rep.ifPresent(itemStack -> builder.accept(
                            Component.translatable("tooltip.rematchcage.activation_item", itemStack.getHoverName()).withStyle(ChatFormatting.GOLD)
                        ));
                        return;
                    }
                }
            }
        }
        builder.accept(Component.translatable("tooltip.rematchcage.creative_hint").withStyle(ChatFormatting.GRAY));
    }
}
