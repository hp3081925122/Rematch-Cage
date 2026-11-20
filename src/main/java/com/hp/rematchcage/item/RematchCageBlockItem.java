package com.hp.rematchcage.item;

import com.hp.rematchcage.block.RematchCageBlockEntity;
import com.hp.rematchcage.config.RematchMappingManager;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class RematchCageBlockItem extends BlockItem {
    public RematchCageBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag beTag = BlockItem.getBlockEntityData(stack);
        if (beTag != null && beTag.contains(RematchCageBlockEntity.TAG_ENTITY_ID, Tag.TAG_STRING)) {
            ResourceLocation id = ResourceLocation.tryParse(beTag.getString(RematchCageBlockEntity.TAG_ENTITY_ID));
            if (id != null && ForgeRegistries.ENTITY_TYPES.containsKey(id)) {
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(id);
                if (entityType != null) {
                    tooltip.add(Component.translatable("tooltip.rematchcage.stored_entity", entityType.getDescription()).withStyle(ChatFormatting.AQUA));
                    Optional<ItemStack> rep = RematchMappingManager.findRepresentativeItem(entityType);
                    rep.ifPresent(itemStack -> tooltip.add(
                        Component.translatable("tooltip.rematchcage.activation_item", itemStack.getHoverName()).withStyle(ChatFormatting.GOLD)
                    ));
                    return;
                }
            }
        }
        tooltip.add(Component.translatable("tooltip.rematchcage.creative_hint").withStyle(ChatFormatting.GRAY));
    }
}
