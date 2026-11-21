package com.hp.rematchcage.compat.jade;

import com.hp.rematchcage.Rematchcage;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public enum RematchCageJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = Rematchcage.id("rematch_cage");
    private static final String KEY_ACTIVATION_ITEM = "RematchActivationItem";
    private static final String KEY_UNMAPPED = "RematchUnmapped";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof RematchCageBlockEntity cage)) {
            return;
        }
        EntityType<?> entityType = cage.getEntityType();
        if (entityType == null) {
            return;
        }
        Optional<ItemStack> rep = RematchMappingManager.findRepresentativeItem(entityType);
        if (rep.isPresent()) {
            ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(rep.get().getItem());
            if (itemKey != null) {
                data.putString(KEY_ACTIVATION_ITEM, itemKey.toString());
            }
        } else {
            data.putBoolean(KEY_UNMAPPED, true);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof RematchCageBlockEntity cage)) {
            return;
        }
        if (cage.getEntityId() == null) {
            tooltip.add(Component.translatable("tooltip.rematchcage.jade.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        EntityType<?> entityType = cage.getEntityType();
        if (entityType == null) {
            tooltip.add(Component.translatable("message.rematchcage.missing_entity").withStyle(ChatFormatting.RED));
            return;
        }

        Component entityDisplay = cage.getCustomName() != null ? cage.getCustomName() : entityType.getDescription();
        tooltip.add(Component.translatable("tooltip.rematchcage.stored_entity", entityDisplay).withStyle(ChatFormatting.AQUA));

        CompoundTag serverData = accessor.getServerData();
        ItemStack activationStack = ItemStack.EMPTY;
        boolean unmapped = serverData.getBoolean(KEY_UNMAPPED);

        if (serverData.contains(KEY_ACTIVATION_ITEM, Tag.TAG_STRING)) {
            ResourceLocation itemId = ResourceLocation.tryParse(serverData.getString(KEY_ACTIVATION_ITEM));
            if (itemId != null && ForgeRegistries.ITEMS.containsKey(itemId)) {
                Item item = ForgeRegistries.ITEMS.getValue(itemId);
                if (item != null && item != Items.AIR) {
                    activationStack = new ItemStack(item);
                }
            }
        } else if (!unmapped) {
            Optional<ItemStack> localRep = RematchMappingManager.findRepresentativeItem(entityType);
            if (localRep.isPresent()) {
                activationStack = localRep.get();
            } else {
                unmapped = true;
            }
        }

        if (!activationStack.isEmpty()) {
            IElementHelper helper = IElementHelper.get();
            tooltip.add(List.of(
                helper.smallItem(activationStack),
                helper.text(Component.translatable("tooltip.rematchcage.activation_item", activationStack.getHoverName()).withStyle(ChatFormatting.GOLD))
            ));
        } else if (unmapped) {
            tooltip.add(Component.translatable("tooltip.rematchcage.jade.unmapped").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
