package com.kasaneta.fakesocialsimulator.network;

import com.kasaneta.fakesocialsimulator.SocialStatsMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SocialStatsMod.MODID)
public class ModNetwork {

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                SyncSocialStatsPayload.TYPE,
                SyncSocialStatsPayload.STREAM_CODEC,
                ClientPayloadHandler::handleSyncSocialStats
        );
    }
}