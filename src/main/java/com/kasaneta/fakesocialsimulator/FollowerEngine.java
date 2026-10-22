package com.kasaneta.fakesocialsimulator;

import com.kasaneta.fakesocialsimulator.event.NametagSyncHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;

public class FollowerEngine {

    private static final long ADVANCEMENT_CHECK_INTERVAL_MS = 4000L;

    public static void processTick(MinecraftServer server) {
        SocialManager manager = SocialManager.get(server);
        long now = System.currentTimeMillis();
        SocialAlgorithm.beginCycle(now);
        boolean needsSave = false;

        for (Map.Entry<UUID, PlayerStats> entry : manager.getAllStats().entrySet()) {
            UUID uuid = entry.getKey();
            PlayerStats stats = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            boolean isOnline = (player != null);

            if (isOnline && stats.getName() == null) {
                stats.setName(player.getName().getString());
            }

            if (SocialAlgorithm.runCycle(stats, isOnline, now)) {
                needsSave = true;
            }

            if (!isOnline) continue;

            if (now >= stats.getNextAdvancementCheck()) {
                stats.setNextAdvancementCheck(now + ADVANCEMENT_CHECK_INTERVAL_MS);
                SocialAdvancementManager.checkAdvancements(player, stats);
            }

            if (stats.needsNametagSync()) {
                NametagSyncHandler.syncPlayerToAll(player, stats);
            }
        }

        if (needsSave) manager.setDirty();
    }
}
