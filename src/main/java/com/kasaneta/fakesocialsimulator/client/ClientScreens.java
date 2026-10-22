package com.kasaneta.fakesocialsimulator.client;

import com.kasaneta.fakesocialsimulator.network.ProfileEntry;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class ClientScreens {

    private ClientScreens() {}

    public static void openProfileViewer() {
        Minecraft.getInstance().setScreen(new ProfileViewerScreen());
    }

    public static void openLeaderboard() {
        Minecraft.getInstance().setScreen(new LeaderboardScreen());
    }

    public static void onSearchResult(List<ProfileEntry> entries) {
        if (Minecraft.getInstance().screen instanceof ProfileViewerScreen screen) {
            screen.setResults(entries);
        }
    }

    public static void onLeaderboardResult(List<ProfileEntry> entries, int yourRank) {
        if (Minecraft.getInstance().screen instanceof LeaderboardScreen screen) {
            screen.setData(entries, yourRank);
        }
    }
}
