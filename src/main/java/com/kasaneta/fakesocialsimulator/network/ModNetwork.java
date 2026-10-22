package com.kasaneta.fakesocialsimulator.network;

import com.kasaneta.fakesocialsimulator.SocialStatsMod;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@EventBusSubscriber(modid = SocialStatsMod.MODID)
public class ModNetwork {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                SyncSocialStatsPayload.TYPE,
                SyncSocialStatsPayload.STREAM_CODEC,
                ClientPayloadHandler::handleSyncSocialStats
        );

        registrar.playToServer(
                ProfileSearchRequestPayload.TYPE,
                ProfileSearchRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleSearch
        );

        registrar.playToServer(
                LeaderboardRequestPayload.TYPE,
                LeaderboardRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleLeaderboard
        );

        registrar.playToClient(
                ProfileSearchResultPayload.TYPE,
                ProfileSearchResultPayload.STREAM_CODEC,
                ClientPayloadHandler::handleSearchResult
        );

        registrar.playToClient(
                LeaderboardResultPayload.TYPE,
                LeaderboardResultPayload.STREAM_CODEC,
                ClientPayloadHandler::handleLeaderboardResult
        );

        LOGGER.info("[FakeSocialSimulator] Network payloads registered (2 to server, 3 to client)");
    }
}
