package com.hp.rematchcage.config;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

public final class RematchMappingManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile List<MappingRule> cachedRules = Collections.emptyList();

    private RematchMappingManager() {
    }

    public static void reload() {
        if (!RematchCageConfig.COMMON_SPEC.isLoaded()) {
            cachedRules = Collections.emptyList();
            return;
        }
        List<? extends String> rawList = RematchCageConfig.COMMON.itemEntityMappings.get();
        List<MappingRule> parsed = new ArrayList<>();
        for (String rawLine : rawList) {
            if (rawLine == null) {
                continue;
            }
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int eqIndex = line.indexOf('=');
            if (eqIndex <= 0 || eqIndex >= line.length() - 1) {
                LOGGER.warn("[RematchCage] Ignored malformed mapping entry (missing '='): {}", line);
                continue;
            }
            String itemToken = line.substring(0, eqIndex).trim();
            String entitiesPart = line.substring(eqIndex + 1).trim();
            ItemMatcher itemMatcher = parseItemMatcher(itemToken);
            if (itemMatcher == null) {
                LOGGER.warn("[RematchCage] Ignored mapping entry with invalid item selector '{}': {}", itemToken, line);
                continue;
            }
            String[] entityTokens = entitiesPart.split("[,;]");
            List<EntityMatcher> entityMatchers = new ArrayList<>();
            for (String entityToken : entityTokens) {
                String trimmed = entityToken.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                EntityMatcher matcher = parseEntityMatcher(trimmed);
                if (matcher != null) {
                    entityMatchers.add(matcher);
                } else {
                    LOGGER.warn("[RematchCage] Ignored invalid entity selector '{}' in entry: {}", trimmed, line);
                }
            }
            if (!entityMatchers.isEmpty()) {
                parsed.add(new MappingRule(itemMatcher, List.copyOf(entityMatchers)));
            }
        }
        cachedRules = List.copyOf(parsed);
        LOGGER.debug("[RematchCage] Loaded {} item-to-entities mapping rules", cachedRules.size());
    }

    private static List<MappingRule> rules() {
        List<MappingRule> current = cachedRules;
        if (current.isEmpty() && RematchCageConfig.COMMON_SPEC.isLoaded()) {
            reload();
            current = cachedRules;
        }
        return current;
    }

    public static boolean isMappedEntity(EntityType<?> entityType) {
        if (entityType == null) {
            return false;
        }
        for (MappingRule rule : rules()) {
            if (rule.matchesEntity(entityType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesActivationItem(EntityType<?> entityType, ItemStack stack) {
        if (entityType == null || stack.isEmpty()) {
            return false;
        }
        for (MappingRule rule : rules()) {
            if (rule.matchesEntity(entityType) && rule.itemMatcher().matches(stack)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<ItemStack> findRepresentativeItem(EntityType<?> entityType) {
        if (entityType == null) {
            return Optional.empty();
        }
        for (MappingRule rule : rules()) {
            if (rule.matchesEntity(entityType)) {
                ItemStack rep = rule.itemMatcher().representativeStack();
                if (!rep.isEmpty()) {
                    return Optional.of(rep);
                }
            }
        }
        return Optional.empty();
    }

    private static ItemMatcher parseItemMatcher(String token) {
        if (token.startsWith("#")) {
            Identifier id = Identifier.tryParse(token.substring(1).trim());
            if (id == null) {
                return null;
            }
            return new TagItemMatcher(TagKey.create(Registries.ITEM, id));
        }
        Identifier id = Identifier.tryParse(token);
        if (id == null) {
            return null;
        }
        return new DirectItemMatcher(id);
    }

    private static EntityMatcher parseEntityMatcher(String token) {
        if (token.startsWith("#")) {
            Identifier id = Identifier.tryParse(token.substring(1).trim());
            if (id == null) {
                return null;
            }
            return new TagEntityMatcher(TagKey.create(Registries.ENTITY_TYPE, id));
        }
        Identifier id = Identifier.tryParse(token);
        if (id == null) {
            return null;
        }
        return new DirectEntityMatcher(id);
    }

    private record MappingRule(ItemMatcher itemMatcher, List<EntityMatcher> entityMatchers) {
        boolean matchesEntity(EntityType<?> type) {
            for (EntityMatcher matcher : this.entityMatchers) {
                if (matcher.matches(type)) {
                    return true;
                }
            }
            return false;
        }
    }

    private interface ItemMatcher {
        boolean matches(ItemStack stack);

        ItemStack representativeStack();
    }

    private record DirectItemMatcher(Identifier itemId) implements ItemMatcher {
        @Override
        public boolean matches(ItemStack stack) {
            Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return this.itemId.equals(key);
        }

        @Override
        public ItemStack representativeStack() {
            return BuiltInRegistries.ITEM.getOptional(this.itemId)
                .filter(item -> item != Items.AIR)
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
        }
    }

    private record TagItemMatcher(TagKey<Item> tagKey) implements ItemMatcher {
        @Override
        public boolean matches(ItemStack stack) {
            return stack.is(this.tagKey);
        }

        @Override
        public ItemStack representativeStack() {
            return BuiltInRegistries.ITEM.get(this.tagKey)
                .flatMap(named -> named.stream().map(Holder::value).filter(item -> item != Items.AIR).findFirst())
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
        }
    }

    private interface EntityMatcher {
        boolean matches(EntityType<?> type);
    }

    private record DirectEntityMatcher(Identifier entityId) implements EntityMatcher {
        @Override
        public boolean matches(EntityType<?> type) {
            Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            return this.entityId.equals(key);
        }
    }

    private record TagEntityMatcher(TagKey<EntityType<?>> tagKey) implements EntityMatcher {
        @Override
        public boolean matches(EntityType<?> type) {
            return BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type).is(this.tagKey);
        }
    }
}
