package com.hp.rematchcage.compat.jade;

import com.hp.rematchcage.Rematchcage;
import com.hp.rematchcage.block.RematchCageBlockEntity;
import com.hp.rematchcage.config.RematchMappingManager;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

public enum RematchCageJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final Identifier UID = Rematchcage.id("rematch_cage");
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
            Identifier itemKey = BuiltInRegistries.ITEM.getKey(rep.get().getItem());
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
        boolean unmapped = serverData.getBooleanOr(KEY_UNMAPPED, false);

        Optional<String> syncedItemId = serverData.getString(KEY_ACTIVATION_ITEM);
        if (syncedItemId.isPresent()) {
            Identifier itemId = Identifier.tryParse(syncedItemId.get());
            if (itemId != null) {
                activationStack = BuiltInRegistries.ITEM.getOptional(itemId)
                    .filter(item -> item != Items.AIR)
                    .map(ItemStack::new)
                    .orElse(ItemStack.EMPTY);
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
            tooltip.add(List.of(
                JadeUI.item(activationStack, 0.75F),
                JadeUI.text(Component.translatable("tooltip.rematchcage.activation_item", activationStack.getHoverName()).withStyle(ChatFormatting.GOLD))
            ));
        } else if (unmapped) {
            tooltip.add(Component.translatable("tooltip.rematchcage.jade.unmapped").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
