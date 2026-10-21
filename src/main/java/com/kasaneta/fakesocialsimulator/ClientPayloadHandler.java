package com.kasaneta.fakesocialsimulator.network;

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
}