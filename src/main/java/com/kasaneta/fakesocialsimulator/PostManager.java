package com.kasaneta.fakesocialsimulator;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Random;
import java.util.UUID;

public class PostManager {
    private static final Random RANDOM = new Random();

    public static void publishPost(ServerPlayer player) {
        publishPostOffline(player.server, player.getUUID());
    }

    public static void publishPostOffline(MinecraftServer server, UUID uuid) {
        SocialManager manager = SocialManager.get(server);
        PlayerStats stats = manager.getStats(uuid);

        stats.incrementPosts();
        SocialAlgorithm.onPostPublished(stats, System.currentTimeMillis());
        manager.setDirty();
    }

    public static void deletePost(ServerPlayer player) {
        deletePostOffline(player.server, player.getUUID());
    }

    public static void deletePostOffline(MinecraftServer server, UUID uuid) {
        SocialManager manager = SocialManager.get(server);
        PlayerStats stats = manager.getStats(uuid);

        if (stats.getPostsCount() > 0) {
            stats.decrementPosts();
            if (Config.PUNISH_SPAMMERS.get()) {
                stats.decayDeletionSpamTo(System.currentTimeMillis());
                stats.addDeletionSpam(3.0);
                if (stats.getDeletionSpamLevel() > 20.0) {
                    double loss = -(0.5 + (RANDOM.nextDouble() * 1.0));
                    stats.addTrend(loss);
                }
            }
            manager.setDirty();
        }
    }
}
