package com.hp.rematchcage.config;

import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class RematchCageConfig {
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        var common = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = common.getLeft();
        COMMON_SPEC = common.getRight();

        var client = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = client.getLeft();
        CLIENT_SPEC = client.getRight();
    }

    private RematchCageConfig() {
    }

    public static void register(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    public static final class Common {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> itemEntityMappings;
        public final ForgeConfigSpec.IntValue placementSearchRadius;
        public final ForgeConfigSpec.BooleanValue snapToGround;
        public final ForgeConfigSpec.IntValue maxGroundSnapDistance;
        public final ForgeConfigSpec.BooleanValue preserveCustomName;
        public final ForgeConfigSpec.BooleanValue playEffects;

        Common(ForgeConfigSpec.Builder builder) {
            builder.comment(
                "重战牢笼映射与生成设置",
                "Rematch cage item-to-entities mappings and placement settings"
            ).push("general");

            this.itemEntityMappings = builder.comment(
                "唤醒物品对生物群组的映射表。格式：'物品ID或#物品标签 = 生物ID1, 生物ID2, #生物标签'",
                "当右侧任意生物死亡时，会在原地生成展示该生物模型的重战牢笼；使用左侧对应物品右键牢笼即可重新召唤该生物。",
                "Item-to-entities mapping list. Format: 'modid:item_or_#tag = modid:entity_a, modid:entity_b, #modid:entity_tag'",
                "When a mapped entity dies, a Rematch Cage displaying its model spawns at the spot; right-click it with the mapped item to summon the entity again."
            ).defineListAllowEmpty(
                List.of("item_entity_mappings"),
                () -> List.of(
                    "minecraft:heart_of_the_sea = minecraft:elder_guardian",
                    "minecraft:nether_star = block_factorys_bosses:infernal_dragon, block_factorys_bosses:yeti, block_factorys_bosses:sandworm, block_factorys_bosses:underworld_knight, block_factorys_bosses:kraken",
                    "legendary_monsters:eye_of_air = legendary_monsters:cloud_golem",
                    "legendary_monsters:eye_of_annihilation = legendary_monsters:the_obliterator",
                    "legendary_monsters:eye_of_bones = legendary_monsters:skeletosaurus",
                    "legendary_monsters:eye_of_chorus = legendary_monsters:endersent",
                    "legendary_monsters:eye_of_frost = legendary_monsters:frostbitten_golem",
                    "legendary_monsters:eye_of_ghost = legendary_monsters:posessed_paladin",
                    "legendary_monsters:eye_of_magma = legendary_monsters:lava_eater",
                    "legendary_monsters:eye_of_many_ribs = legendary_monsters:ancient_guardian",
                    "legendary_monsters:eye_of_moss = legendary_monsters:overgrown_colossus",
                    "legendary_monsters:eye_of_sandstorm = legendary_monsters:dune_sentinel",
                    "legendary_monsters:eye_of_shulker = legendary_monsters:shulker_mimic",
                    "legendary_monsters:eye_of_soul = legendary_monsters:withered_abomination",
                    "minecraft:ghast_tear = legendary_monsters:warped_fungussus",
                    "twilightforest:magic_map_focus = twilightforest:naga, twilightforest:lich, twilightforest:minoshroom, twilightforest:hydra, twilightforest:knight_phantom, twilightforest:ur_ghast, twilightforest:alpha_yeti, twilightforest:snow_queen",
                    "aether:enchanted_gravitite = aether:slider, aether:valkyrie_queen, aether:sun_spirit"
                ),
                entry -> entry instanceof String s && s.contains("=")
            );

            this.placementSearchRadius = builder.comment(
                "死亡位置不可替换时，在周围寻找可放置重战牢笼空位的最大水平半径（格）",
                "Maximum search radius in blocks to find a replaceable spot when the exact death block is occupied"
            ).defineInRange("placement_search_radius", 6, 1, 16);

            this.snapToGround = builder.comment(
                "当飞行生物在半空死亡时，是否优先向下寻找最近的地面放置牢笼以便玩家右键交互",
                "Snap cage placement down to the nearest ground below when a flying mob dies mid-air"
            ).define("snap_to_ground", true);

            this.maxGroundSnapDistance = builder.comment(
                "半空死亡时向下寻找地面的最大格数",
                "Maximum downward blocks checked when snapping mid-air deaths to ground"
            ).defineInRange("max_ground_snap_distance", 32, 1, 128);

            this.preserveCustomName = builder.comment(
                "重新召唤出的生物是否保留死亡时的自定义名称",
                "Preserve the dead entity's custom name when resummoning from the cage"
            ).define("preserve_custom_name", false);

            this.playEffects = builder.comment(
                "生成重战牢笼与唤醒生物时是否播放音效和灵魂粒子",
                "Play sound and soul particle effects when creating or activating a Rematch Cage"
            ).define("play_effects", true);

            builder.pop();
        }
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue renderEntityModel;
        public final ForgeConfigSpec.DoubleValue spinDegreesPerTick;
        public final ForgeConfigSpec.DoubleValue bobAmplitude;
        public final ForgeConfigSpec.DoubleValue baseEntityScale;
        public final ForgeConfigSpec.BooleanValue xrayGlowOutline;
        public final ForgeConfigSpec.DoubleValue xrayGlowRange;

        Client(ForgeConfigSpec.Builder builder) {
            builder.comment(
                "重战牢笼客户端模型展示设置",
                "Client rendering settings for the miniature entity inside Rematch Cage"
            ).push("rendering");

            this.renderEntityModel = builder.comment(
                "是否在重战牢笼内部渲染旋转的微缩生物模型",
                "Render the spinning miniature entity model inside the Rematch Cage"
            ).define("render_entity_model", true);

            this.spinDegreesPerTick = builder.comment(
                "内部生物模型每刻自转角度",
                "Spin rotation degrees per tick for the displayed entity"
            ).defineInRange("spin_degrees_per_tick", 1.8, 0.0, 30.0);

            this.bobAmplitude = builder.comment(
                "内部生物模型上下悬浮幅度（格）",
                "Vertical bobbing amplitude in blocks for the displayed entity"
            ).defineInRange("bob_amplitude", 0.03, 0.0, 0.2);

            this.baseEntityScale = builder.comment(
                "内部生物模型目标基准尺寸（大型 Boss 会根据碰撞箱自动等比缩小）",
                "Base target display size inside the cage (large bosses automatically scale down to fit)"
            ).defineInRange("base_entity_scale", 0.53, 0.1, 1.2);

            this.xrayGlowOutline = builder.comment(
                "是否渲染可穿透方块看见的重战牢笼发光轮廓",
                "Render a see-through glowing outline around Rematch Cages visible through blocks"
            ).define("xray_glow_outline", true);

            this.xrayGlowRange = builder.comment(
                "穿透方块发光轮廓的最大可见距离（格）",
                "Maximum visibility distance in blocks for the see-through glowing outline"
            ).defineInRange("xray_glow_range", 64.0, 0.0, 256.0);

            builder.pop();
        }
    }
}
