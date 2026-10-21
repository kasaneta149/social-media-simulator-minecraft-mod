package com.kasaneta.fakesocialsimulator;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class SocialAdvancementManager {

    private record Milestone(long count, String category, String id) {}

    private static final Milestone[] MILESTONES = new Milestone[]{
        new Milestone(1L, "followers", "follower_1"),
        new Milestone(5L, "followers", "follower_5"),
        new Milestone(25L, "followers", "follower_25"),
        new Milestone(50L, "followers", "follower_50"),
        new Milestone(90L, "followers", "follower_90"),
        new Milestone(100L, "followers", "follower_100"),
        new Milestone(250L, "followers", "follower_250"),
        new Milestone(500L, "followers", "follower_500"),
        new Milestone(1000L, "followers", "follower_1k"),
        new Milestone(5000L, "followers", "follower_5k"),
        new Milestone(10000L, "followers", "follower_10k"),
        new Milestone(25000L, "followers", "follower_25k"),
        new Milestone(50000L, "followers", "follower_50k"),
        new Milestone(75000L, "followers", "follower_75k"),
        new Milestone(100000L, "followers", "follower_100k"),
        new Milestone(250000L, "followers", "follower_250k"),
        new Milestone(500000L, "followers", "follower_500k"),
        new Milestone(750000L, "followers", "follower_750k"),
        new Milestone(1000000L, "followers", "follower_1m"),
        new Milestone(10000000L, "followers", "follower_10m"),
        new Milestone(100000000L, "followers", "follower_100m"),
        new Milestone(1000000000L, "followers", "follower_1b"),

        new Milestone(100L, "views", "views_100"),
        new Milestone(1000L, "views", "views_1k"),
        new Milestone(10000L, "views", "views_10k"),
        new Milestone(100000L, "views", "views_100k"),
        new Milestone(1000000L, "views", "views_1m"),
        new Milestone(10000000L, "views", "views_10m"),
        new Milestone(100000000L, "views", "views_100m"),
        new Milestone(1000000000L, "views", "views_1b"),
        new Milestone(10000000000L, "views", "views_10b"),

        new Milestone(10L, "likes", "likes_10"),
        new Milestone(100L, "likes", "likes_100"),
        new Milestone(1000L, "likes", "likes_1k"),
        new Milestone(10000L, "likes", "likes_10k"),
        new Milestone(100000L, "likes", "likes_100k"),
        new Milestone(1000000L, "likes", "likes_1m"),
        new Milestone(10000000L, "likes", "likes_10m"),
        new Milestone(100000000L, "likes", "likes_100m"),
        new Milestone(1000000000L, "likes", "likes_1b"),
        new Milestone(10000000000L, "likes", "likes_10b"),

        new Milestone(1L, "comments", "comments_1"),
        new Milestone(10L, "comments", "comments_10"),
        new Milestone(100L, "comments", "comments_100"),
        new Milestone(1000L, "comments", "comments_1k"),
        new Milestone(10000L, "comments", "comments_10k"),
        new Milestone(100000L, "comments", "comments_100k"),
        new Milestone(1000000L, "comments", "comments_1m"),
        new Milestone(10000000L, "comments", "comments_10m"),

        new Milestone(1L, "posts", "posts_1"),
        new Milestone(10L, "posts", "posts_10"),
        new Milestone(50L, "posts", "posts_50"),
        new Milestone(100L, "posts", "posts_100"),
        new Milestone(500L, "posts", "posts_500"),
        new Milestone(1000L, "posts", "posts_1k"),
        new Milestone(5000L, "posts", "posts_5k"),
        new Milestone(10000L, "posts", "posts_10k"),
        new Milestone(50000L, "posts", "posts_50000"),
        new Milestone(100000L, "posts", "posts_100k")
    };

    public static void checkAdvancements(ServerPlayer player, PlayerStats stats) {
        for (Milestone milestone : MILESTONES) {
            long currentVal = switch (milestone.category) {
                case "followers" -> stats.getFollowersCount();
                case "views" -> stats.getViewCount();
                case "likes" -> stats.getLikeCount();
                case "comments" -> stats.getCommentCount();
                case "posts" -> stats.getPostsCount();
                default -> 0L;
            };

            if (currentVal >= milestone.count) {
                grantAdvancement(player, milestone.category, milestone.id);
            }
        }
    }

    private static void grantAdvancement(ServerPlayer player, String category, String id) {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(SocialStatsMod.MODID, category + "/" + id);
        AdvancementHolder holder = player.server.getAdvancements().get(loc);

        if (holder != null) {
            var progress = player.getAdvancements().getOrStartProgress(holder);
            if (!progress.isDone()) {
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(holder, criterion);
                }
            }
        }
    }
}