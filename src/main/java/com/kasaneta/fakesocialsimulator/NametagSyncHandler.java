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
public class NametagSyncHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SocialManager.get(player.server).getStats(player.getUUID()).invalidateNametagSync();
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer observer) {
            syncStatsToPlayer(target, observer);
        }
    }

    public static void syncStatsToPlayer(ServerPlayer source, ServerPlayer receiver) {
        PlayerStats stats = SocialManager.get(source.server).getStats(source.getUUID());

        PacketDistributor.sendToPlayer(
                receiver,
                new SyncSocialStatsPayload(
                        source.getUUID(),
                        stats.getPostsCount(),
                        stats.getFollowersCount(),
                        stats.getFollowingCount()
                )
        );
    }

    public static void syncPlayerToAll(ServerPlayer player, PlayerStats stats) {
        SyncSocialStatsPayload payload = new SyncSocialStatsPayload(
                player.getUUID(),
                stats.getPostsCount(),
                stats.getFollowersCount(),
                stats.getFollowingCount()
        );

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payload);
        stats.markNametagSynced();
    }

    public static void syncPlayerToAll(ServerPlayer player) {
        syncPlayerToAll(player, SocialManager.get(player.server).getStats(player.getUUID()));
    }
}
