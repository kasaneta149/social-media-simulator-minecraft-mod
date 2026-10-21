package com.kasaneta.fakesocialsimulator;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_FOLLOWING_COUNT;
    public static final ModConfigSpec.DoubleValue FOLLOWER_DIFFICULTY;
    public static final ModConfigSpec.DoubleValue NATURAL_CHURN_RATE;
    public static final ModConfigSpec.BooleanValue PUNISH_SPAMMERS;
    public static final ModConfigSpec.DoubleValue MIN_TREND;
    public static final ModConfigSpec.DoubleValue MAX_TREND;
    public static final ModConfigSpec.DoubleValue ONLINE_TREND_DECAY;
    public static final ModConfigSpec.DoubleValue OFFLINE_TREND_DECAY;

    public static final ModConfigSpec.DoubleValue BASE_FOLLOW_BACK_CHANCE;
    public static final ModConfigSpec.DoubleValue MIN_FOLLOW_BACK_CHANCE;
    public static final ModConfigSpec.DoubleValue UNFOLLOW_BACK_CHANCE;

    public static final ModConfigSpec.BooleanValue ENABLE_PASSIVE_POPULARITY;
    public static final ModConfigSpec.IntValue PASSIVE_START_THRESHOLD;
    public static final ModConfigSpec.IntValue PASSIVE_FULL_THRESHOLD;
    public static final ModConfigSpec.DoubleValue PASSIVE_ACTIVITY_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue ENABLE_PERMANENT_FOLLOWERS;
    public static final ModConfigSpec.DoubleValue PERMANENT_FOLLOWER_CHANCE;

    public static final ModConfigSpec.DoubleValue BLOCK_BREAK_ACTIVITY;
    public static final ModConfigSpec.DoubleValue BLOCK_PLACE_ACTIVITY;
    public static final ModConfigSpec.DoubleValue CRAFT_ITEM_ACTIVITY;
    public static final ModConfigSpec.DoubleValue SMELT_ITEM_ACTIVITY;
    public static final ModConfigSpec.DoubleValue EAT_DRINK_ACTIVITY;
    public static final ModConfigSpec.DoubleValue FISH_ACTIVITY;
    public static final ModConfigSpec.DoubleValue TAME_ANIMAL_ACTIVITY;
    public static final ModConfigSpec.DoubleValue BREED_ANIMAL_ACTIVITY;
    public static final ModConfigSpec.DoubleValue DIMENSION_CHANGE_ACTIVITY;
    public static final ModConfigSpec.DoubleValue ADVANCEMENT_ACTIVITY;
    public static final ModConfigSpec.DoubleValue BOSS_KILL_ACTIVITY;
    public static final ModConfigSpec.DoubleValue MOB_KILL_ACTIVITY;

    public static final ModConfigSpec.DoubleValue VIEW_DRAIN_RATE;
    public static final ModConfigSpec.DoubleValue LIKE_DRAIN_RATE;
    public static final ModConfigSpec.DoubleValue COMMENT_DRAIN_RATE;

    public static final ModConfigSpec.BooleanValue SHOW_NAMETAG_STATS;

    public static final ModConfigSpec.BooleanValue ENABLE_ACTION_FATIGUE;
    public static final ModConfigSpec.DoubleValue RANDOMNESS_STRENGTH;
    public static final ModConfigSpec.DoubleValue POST_SATURATION_THRESHOLD;
    public static final ModConfigSpec.DoubleValue POST_SATURATION_HALF_LIFE;
    public static final ModConfigSpec.BooleanValue ENABLE_EXODUS;
    public static final ModConfigSpec.DoubleValue EXODUS_CHANCE_PER_HOUR;
    public static final ModConfigSpec.DoubleValue EXODUS_MAX_LOSS_RATIO;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> SPECIFIC_BLOCK_BREAK;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SPECIFIC_ENTITY_KILL;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SPECIFIC_ITEM_CRAFT;

    static {
        BUILDER.push("general_settings");
        MAX_FOLLOWING_COUNT = BUILDER.comment("Maximum number of accounts a player can follow").defineInRange("max_following_count", 5000, 1, 2147483647);
        FOLLOWER_DIFFICULTY = BUILDER.comment("How hard it is to gain followers (3.5 = reference balance, 7.0 = twice as hard)").defineInRange("follower_difficulty", 3.5, 0.1, 100.0);
        NATURAL_CHURN_RATE = BUILDER.comment("Multiplier for natural unfollows (1.0 = normal, 0 = none)").defineInRange("natural_churn_rate", 1.0, 0.0, 50.0);
        PUNISH_SPAMMERS = BUILDER.comment("Enable the anti-spam filter (posting in bursts earns less and less)").define("punish_spammers", true);
        MIN_TREND = BUILDER.comment("Minimum hype (trend) level").defineInRange("min_trend", -150.0, -1000000.0, 0.0);
        MAX_TREND = BUILDER.comment("Maximum hype (trend) level").defineInRange("max_trend", 300.0, 0.0, 2000000000.0);
        ONLINE_TREND_DECAY = BUILDER.comment("Hype half-life in minutes while the player is active: hype is halved after this time").defineInRange("online_hype_half_life_minutes", 40.0, 1.0, 100000.0);
        OFFLINE_TREND_DECAY = BUILDER.comment("Hype half-life in minutes while the player is offline or idle").defineInRange("offline_hype_half_life_minutes", 20.0, 1.0, 100000.0);
        BUILDER.pop();

        BUILDER.push("follow_back_settings");
        BASE_FOLLOW_BACK_CHANCE = BUILDER.comment("Base chance that an account follows you back").defineInRange("base_follow_back_chance", 0.12, 0.0, 1.0);
        MIN_FOLLOW_BACK_CHANCE = BUILDER.comment("Minimum follow-back chance (anti mass-follow)").defineInRange("min_follow_back_chance", 0.01, 0.0, 1.0);
        UNFOLLOW_BACK_CHANCE = BUILDER.comment("Chance of losing a follower when you unfollow an account").defineInRange("unfollow_back_chance", 0.33, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("popularity_settings");
        ENABLE_PASSIVE_POPULARITY = BUILDER.comment("Enable passive popularity (large accounts keep receiving some views and followers without activity)").define("enable_passive_popularity", true);
        PASSIVE_START_THRESHOLD = BUILDER.comment("Follower count at which passive popularity starts").defineInRange("passive_start_threshold", 25000, 1, 1000000);
        PASSIVE_FULL_THRESHOLD = BUILDER.comment("Follower count at which passive popularity reaches full strength").defineInRange("passive_full_threshold", 95000, 2, 1000000);
        PASSIVE_ACTIVITY_MULTIPLIER = BUILDER.comment("Multiplier for passive popularity").defineInRange("passive_activity_multiplier", 1.0, 0.0, 50.0);
        BUILDER.pop();

        BUILDER.push("permanent_followers");
        ENABLE_PERMANENT_FOLLOWERS = BUILDER.comment("Enable permanent followers (followers who never unfollow)").define("enable_permanent_followers", true);
        PERMANENT_FOLLOWER_CHANCE = BUILDER.comment("Chance for a new follower to be permanent (0.005 = 0.5%)").defineInRange("permanent_follower_chance", 0.0003, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("action_rewards");
        BLOCK_BREAK_ACTIVITY = BUILDER.comment("Default activity gained for breaking a block").defineInRange("block_break", 0.05, 0.0, 1000.0);
        BLOCK_PLACE_ACTIVITY = BUILDER.comment("Default activity gained for placing a block").defineInRange("block_place", 0.05, 0.0, 1000.0);
        CRAFT_ITEM_ACTIVITY = BUILDER.comment("Default activity gained for crafting an item").defineInRange("craft_item", 0.2, 0.0, 1000.0);
        SMELT_ITEM_ACTIVITY = BUILDER.comment("Default activity gained for smelting an item").defineInRange("smelt_item", 0.1, 0.0, 1000.0);
        EAT_DRINK_ACTIVITY = BUILDER.comment("Activity gained for eating or drinking").defineInRange("eat_drink", 0.3, 0.0, 1000.0);
        FISH_ACTIVITY = BUILDER.comment("Activity gained for fishing").defineInRange("fish", 1.5, 0.0, 1000.0);
        TAME_ANIMAL_ACTIVITY = BUILDER.comment("Activity gained for taming an animal").defineInRange("tame_animal", 5.0, 0.0, 1000.0);
        BREED_ANIMAL_ACTIVITY = BUILDER.comment("Activity gained for breeding animals").defineInRange("breed_animal", 2.0, 0.0, 1000.0);
        DIMENSION_CHANGE_ACTIVITY = BUILDER.comment("Activity gained for changing dimension").defineInRange("dimension_change", 10.0, 0.0, 1000.0);
        ADVANCEMENT_ACTIVITY = BUILDER.comment("Activity gained for completing an advancement").defineInRange("advancement", 0.0, 0.0, 1000.0);
        BOSS_KILL_ACTIVITY = BUILDER.comment("Default activity gained for killing a boss").defineInRange("boss_kill", 1000.0, 0.0, 100000.0);
        MOB_KILL_ACTIVITY = BUILDER.comment("Default activity gained for killing a monster").defineInRange("mob_kill", 1.0, 0.0, 1000.0);
        BUILDER.pop();

        BUILDER.push("custom_bonus_malus");

        SPECIFIC_BLOCK_BREAK = BUILDER
                .comment("Bonus/malus for breaking specific blocks. Format: 'modid:block_id;value'",
                         "A negative value lowers hype instead (bad buzz).",
                         "Example: ['minecraft:diamond_ore;10.0', 'minecraft:ancient_debris;25.0', 'minecraft:spawner;50.0']")
                .defineListAllowEmpty("specific_block_break",
                        List.of("minecraft:diamond_ore;10.0", "minecraft:ancient_debris;25.0", "minecraft:spawner;40.0"),
                        obj -> obj instanceof String);

        SPECIFIC_ENTITY_KILL = BUILDER
                .comment("Bonus/malus for killing specific entities. Format: 'modid:entity_id;value'",
                         "Malus example (killing a villager): 'minecraft:villager;-15.0'",
                         "Bonus example (Warden): 'minecraft:warden;500.0'")
                .defineListAllowEmpty("specific_entity_kill",
                        List.of("minecraft:villager;-15.0", "minecraft:iron_golem;-10.0", "minecraft:warden;500.0"),
                        obj -> obj instanceof String);

        SPECIFIC_ITEM_CRAFT = BUILDER
                .comment("Bonus/malus for crafting specific items. Format: 'modid:item_id;value'",
                         "Example: ['minecraft:beacon;150.0', 'minecraft:netherite_chestplate;80.0']")
                .defineListAllowEmpty("specific_item_craft",
                        List.of("minecraft:beacon;150.0", "minecraft:netherite_ingot;50.0"),
                        obj -> obj instanceof String);

        BUILDER.pop();

        BUILDER.push("drain_settings");
        VIEW_DRAIN_RATE = BUILDER
                .comment("Percentage of the pending views pool released at each engine cycle.",
                         "0.06 = 6% per cycle (medium speed).",
                         "0.01 = 1% per cycle (very slow, spread out over time).",
                         "0.50 = 50% per cycle (almost instant).")
                .defineInRange("view_drain_rate", 0.06, 0.001, 1.0);

        LIKE_DRAIN_RATE = BUILDER
                .comment("Percentage of the pending likes pool released at each engine cycle (only used for rare like removals).")
                .defineInRange("like_drain_rate", 0.04, 0.001, 1.0);

        COMMENT_DRAIN_RATE = BUILDER
                .comment("Percentage of the pending comments pool released at each engine cycle (only used for rare comment removals).")
                .defineInRange("comment_drain_rate", 0.02, 0.001, 1.0);
        BUILDER.pop();

        BUILDER.push("realism_settings");
        ENABLE_ACTION_FATIGUE = BUILDER
                .comment("Anti-loop: repeating the same action earns less and less, varying activities earns more.")
                .define("enable_action_fatigue", true);
        RANDOMNESS_STRENGTH = BUILDER
                .comment("Strength of unpredictability (0 = very regular, 1.0 = normal, 2.0 = very chaotic).")
                .defineInRange("randomness_strength", 1.0, 0.0, 3.0);
        POST_SATURATION_THRESHOLD = BUILDER
                .comment("Number of recent posts at which a new post only earns 50% of its views.",
                         "Beyond that, returns collapse until posts earn nothing at all (spam).")
                .defineInRange("post_saturation_threshold", 6.0, 1.0, 1000.0);
        POST_SATURATION_HALF_LIFE = BUILDER
                .comment("Time in minutes for the posting fatigue to be halved.")
                .defineInRange("post_saturation_half_life_minutes", 15.0, 1.0, 1440.0);
        ENABLE_EXODUS = BUILDER
                .comment("Allow exodus events: an ultra rare event where part of the followers leave, spread over several hours.")
                .define("enable_exodus", true);
        EXODUS_CHANCE_PER_HOUR = BUILDER
                .comment("Base chance of an exodus per hour (0.0005 = about once every 2000 hours, roughly 3 months).",
                         "Increases when hype is very negative.")
                .defineInRange("exodus_chance_per_hour", 0.0005, 0.0, 1.0);
        EXODUS_MAX_LOSS_RATIO = BUILDER
                .comment("Maximum follower loss (0.80 = never more than 80% lost).",
                         "An account can never fall below (1 - this value) of its follower record.")
                .defineInRange("exodus_max_loss_ratio", 0.80, 0.05, 0.95);
        BUILDER.pop();

        BUILDER.push("display_settings");
        SHOW_NAMETAG_STATS = BUILDER
                .comment("Show the posts, followers and following counters above player names.",
                         "Each player can also hide them for themselves with the /socialnametag command.")
                .define("show_nametag_stats", true);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static List<? extends String> blockRawCache = null;
    private static Map<String, Double> blockMapCache = new HashMap<>();
    private static List<? extends String> entityRawCache = null;
    private static Map<String, Double> entityMapCache = new HashMap<>();
    private static List<? extends String> itemRawCache = null;
    private static Map<String, Double> itemMapCache = new HashMap<>();

    public static Map<String, Double> getSpecificBlockBreakMap() {
        List<? extends String> raw = SPECIFIC_BLOCK_BREAK.get();
        if (raw != blockRawCache) {
            blockRawCache = raw;
            blockMapCache = parseConfigMap(raw);
        }
        return blockMapCache;
    }

    public static Map<String, Double> getSpecificEntityKillMap() {
        List<? extends String> raw = SPECIFIC_ENTITY_KILL.get();
        if (raw != entityRawCache) {
            entityRawCache = raw;
            entityMapCache = parseConfigMap(raw);
        }
        return entityMapCache;
    }

    public static Map<String, Double> getSpecificItemCraftMap() {
        List<? extends String> raw = SPECIFIC_ITEM_CRAFT.get();
        if (raw != itemRawCache) {
            itemRawCache = raw;
            itemMapCache = parseConfigMap(raw);
        }
        return itemMapCache;
    }

    public static Map<String, Double> parseConfigMap(List<? extends String> rawList) {
        Map<String, Double> result = new HashMap<>();
        for (String entry : rawList) {
            String[] parts = entry.split(";");
            if (parts.length == 2) {
                try {
                    String id = parts[0].trim().toLowerCase(Locale.ROOT);
                    double val = Double.parseDouble(parts[1].trim());
                    result.put(id, val);
                } catch (NumberFormatException ignored) {}
            }
        }
        return result;
    }
}
