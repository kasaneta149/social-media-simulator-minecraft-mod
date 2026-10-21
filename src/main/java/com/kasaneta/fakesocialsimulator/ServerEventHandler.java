package com.kasaneta.fakesocialsimulator.event;

import com.kasaneta.fakesocialsimulator.PlayerStats;
import com.kasaneta.fakesocialsimulator.SocialManager;
import com.kasaneta.fakesocialsimulator.SocialStatsMod;
import com.kasaneta.fakesocialsimulator.network.SyncSocialStatsPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = SocialStatsMod.MODID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncPlayerStats(player);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer targetPlayer && event.getEntity() instanceof ServerPlayer observer) {
            SocialManager manager = SocialManager.get(targetPlayer.server);
            PlayerStats stats = manager.getStats(targetPlayer.getUUID());

            PacketDistributor.sendToPlayer(
                    observer,
                    new SyncSocialStatsPayload(
                            targetPlayer.getUUID(),
                            stats.getPostsCount(),
                            stats.getFollowersCount(),
                            stats.getFollowingCount()
                    )
            );
        }
    }

    public static void syncPlayerStats(ServerPlayer player) {
        SocialManager manager = SocialManager.get(player.server);
        PlayerStats stats = manager.getStats(player.getUUID());

        PacketDistributor.sendToPlayersTrackingEntity(
                player,
                new SyncSocialStatsPayload(
                        player.getUUID(),
                        stats.getPostsCount(),
                        stats.getFollowersCount(),
                        stats.getFollowingCount()
                )
        );
    }
}