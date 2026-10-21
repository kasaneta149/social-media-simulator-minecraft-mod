package com.kasaneta.fakesocialsimulator;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;
import java.util.Random;

@EventBusSubscriber(modid = SocialStatsMod.MODID)
public class SocialEventHandler {

    private static final Random RANDOM = new Random();
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        tickCounter++;
        if (tickCounter >= 8) {
            tickCounter = 0;
            FollowerEngine.processTick(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && isRealPlayer(player)) {
            Map<String, Double> customMap = Config.getSpecificBlockBreakMap();
            if (!customMap.isEmpty()) {
                String blockId = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock()).toString();
                Double value = customMap.get(blockId);
                if (value != null) {
                    applyValueOrPenalty(player, PlayerStats.ActionType.BLOCK_BREAK, value);
                    return;
                }
            }
            reward(player, PlayerStats.ActionType.BLOCK_BREAK, Config.BLOCK_BREAK_ACTIVITY.get());
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isRealPlayer(player)) {
            reward(player, PlayerStats.ActionType.BLOCK_PLACE, Config.BLOCK_PLACE_ACTIVITY.get());
        }
    }

    @SubscribeEvent
    public static void onCraftItem(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isRealPlayer(player)) {
            Map<String, Double> customMap = Config.getSpecificItemCraftMap();
            if (!customMap.isEmpty()) {
                String itemId = BuiltInRegistries.ITEM.getKey(event.getCrafting().getItem()).toString();
                Double value = customMap.get(itemId);
                if (value != null) {
                    applyValueOrPenalty(player, PlayerStats.ActionType.CRAFT, value);
                    return;
                }
            }
            reward(player, PlayerStats.ActionType.CRAFT, Config.CRAFT_ITEM_ACTIVITY.get());
        }
    }

    @SubscribeEvent
    public static void onSmeltItem(PlayerEvent.ItemSmeltedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isRealPlayer(player)) {
            reward(player, PlayerStats.ActionType.SMELT, Config.SMELT_ITEM_ACTIVITY.get());
        }
    }

    @SubscribeEvent
    public static void onAnimalBreed(BabyEntitySpawnEvent event) {
        if (event.getCausedByPlayer() instanceof ServerPlayer player && isRealPlayer(player)) {
            reward(player, PlayerStats.ActionType.BREED, Config.BREED_ANIMAL_ACTIVITY.get());
        }
    }

    @SubscribeEvent
    public static void onEatOrDrink(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player && isRealPlayer(player)) {
            reward(player, PlayerStats.ActionType.EAT_DRINK, Config.EAT_DRINK_ACTIVITY.get());
        }
    }

    @SubscribeEvent
    public static void onFish(ItemFishedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isRealPlayer(player)) {
            reward(player, PlayerStats.ActionType.FISH, Config.FISH_ACTIVITY.get());
        }
    }

    @SubscribeEvent
    public static void onAnimalTame(AnimalTameEvent event) {
        if (event.getTamer() instanceof ServerPlayer player && isRealPlayer(player)) {
            double mult = reward(player, PlayerStats.ActionType.TAME, Config.TAME_ANIMAL_ACTIVITY.get());
            PlayerStats stats = statsOf(player);
            double hs = SocialAlgorithm.hypeScale(stats.getFollowersCount());
            stats.addTrend((0.3 + (RANDOM.nextDouble() * 0.5)) * mult * hs);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isRealPlayer(player)) {
            double mult = reward(player, PlayerStats.ActionType.DIMENSION, Config.DIMENSION_CHANGE_ACTIVITY.get());
            PlayerStats stats = statsOf(player);
            double hs = SocialAlgorithm.hypeScale(stats.getFollowersCount());
            stats.addTrend((0.5 + (RANDOM.nextDouble() * 1.0)) * mult * hs);
        }
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isRealPlayer(player)) {
            if (event.getAdvancement().id().getNamespace().equals(SocialStatsMod.MODID)) return;

            if (event.getAdvancement().value().display().isPresent()) {
                PlayerStats stats = statsOf(player);
                long now = System.currentTimeMillis();

                double mult = SocialAlgorithm.rewardAction(stats, PlayerStats.ActionType.ADVANCEMENT,
                        Config.ADVANCEMENT_ACTIVITY.get(), now);
                double hs = SocialAlgorithm.hypeScale(stats.getFollowersCount());

                stats.setLastAchievementTime(now);
                stats.setMomentBoost(1.0 + 0.3 * mult * hs);
                stats.addTrend((0.25 + (RANDOM.nextDouble() * 0.5)) * mult * hs);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        long now = System.currentTimeMillis();

        if (event.getSource().getEntity() instanceof ServerPlayer player && isRealPlayer(player)) {
            PlayerStats stats = statsOf(player);
            EntityType<?> victimType = event.getEntity().getType();
            boolean boss = victimType == EntityType.ENDER_DRAGON || victimType == EntityType.WITHER;

            Double value = null;
            Map<String, Double> customMap = Config.getSpecificEntityKillMap();
            if (!customMap.isEmpty()) {
                value = customMap.get(BuiltInRegistries.ENTITY_TYPE.getKey(victimType).toString());
            }

            if (value != null) {
                applyValueOrPenalty(player, boss ? PlayerStats.ActionType.BOSS_KILL : PlayerStats.ActionType.MOB_KILL, value);
            } else if (boss) {
                double mult = reward(player, PlayerStats.ActionType.BOSS_KILL, Config.BOSS_KILL_ACTIVITY.get());
                double hs = SocialAlgorithm.hypeScale(stats.getFollowersCount());
                stats.addTrend((20.0 + (RANDOM.nextDouble() * 15.0)) * mult * hs);
            } else if (event.getEntity() instanceof Monster) {
                reward(player, PlayerStats.ActionType.MOB_KILL, Config.MOB_KILL_ACTIVITY.get());
            }
        }

        if (event.getEntity() instanceof ServerPlayer deadPlayer && isRealPlayer(deadPlayer)) {
            PlayerStats stats = statsOf(deadPlayer);
            double hs = SocialAlgorithm.hypeScale(stats.getFollowersCount());

            if (now - stats.getLastDeathTime() < 300000) {
                stats.setDeathStreak(stats.getDeathStreak() + 1);
            } else {
                stats.setDeathStreak(1);
            }
            stats.setLastDeathTime(now);

            if (stats.getDeathStreak() >= 3) {
                stats.addTrend(-(10.0 + (RANDOM.nextDouble() * 10.0)) * hs);
                stats.setDeathStreak(0);
            } else {
                stats.addTrend(-(1.0 + (RANDOM.nextDouble() * 2.0)) * hs);
            }
        }
    }

    private static boolean isRealPlayer(ServerPlayer player) {
        return !(player instanceof FakePlayer);
    }

    private static PlayerStats statsOf(ServerPlayer player) {
        return SocialManager.get(player.server).getStats(player.getUUID());
    }

    private static double reward(ServerPlayer player, PlayerStats.ActionType type, double baseAmount) {
        return SocialAlgorithm.rewardAction(statsOf(player), type, baseAmount, System.currentTimeMillis());
    }

    private static void applyValueOrPenalty(ServerPlayer player, PlayerStats.ActionType type, double value) {
        if (value >= 0) {
            reward(player, type, value);
        } else {
            statsOf(player).addTrend(value);
        }
    }
}
