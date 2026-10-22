package com.kasaneta.fakesocialsimulator.network;

import com.kasaneta.fakesocialsimulator.PlayerStats;
import com.kasaneta.fakesocialsimulator.SocialManager;
import com.kasaneta.fakesocialsimulator.SocialQueries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleSearch(final ProfileSearchRequestPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            MinecraftServer server = player.server;
            PlayerStats self = SocialManager.get(server).getStats(player.getUUID());
            if (!self.allowQuery(System.currentTimeMillis())) return;
            PacketDistributor.sendToPlayer(player, new ProfileSearchResultPayload(SocialQueries.search(server, payload.query())));
        });
    }

    public static void handleLeaderboard(final LeaderboardRequestPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            MinecraftServer server = player.server;
            PlayerStats self = SocialManager.get(server).getStats(player.getUUID());
            if (!self.allowQuery(System.currentTimeMillis())) return;
            PacketDistributor.sendToPlayer(player, new LeaderboardResultPayload(
                    SocialQueries.leaderboard(server),
                    SocialQueries.rankOf(server, player.getUUID())
            ));
        });
    }
}
