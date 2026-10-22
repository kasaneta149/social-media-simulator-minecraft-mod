package com.kasaneta.fakesocialsimulator.network;

import com.kasaneta.fakesocialsimulator.client.ClientScreens;
import com.kasaneta.fakesocialsimulator.client.ClientStatsCache;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

    public static void handleSyncSocialStats(final SyncSocialStatsPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientStatsCache.update(
                    payload.playerUUID(),
                    payload.posts(),
                    payload.followers(),
                    payload.following()
            );
        });
    }

    public static void handleSearchResult(final ProfileSearchResultPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> ClientScreens.onSearchResult(payload.entries()));
    }

    public static void handleLeaderboardResult(final LeaderboardResultPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> ClientScreens.onLeaderboardResult(payload.entries(), payload.yourRank()));
    }
}
