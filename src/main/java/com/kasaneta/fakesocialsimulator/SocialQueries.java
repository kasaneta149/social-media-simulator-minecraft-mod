package com.kasaneta.fakesocialsimulator;

import com.kasaneta.fakesocialsimulator.network.ProfileEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

public final class SocialQueries {

    public static final int SEARCH_LIMIT = 8;
    public static final int LEADERBOARD_SIZE = 10;
    private static final long LEADERBOARD_TTL_MS = 5000L;

    private static SocialManager cachedFor;
    private static long cachedAt;
    private static List<ProfileEntry> cachedBoard = List.of();

    private record Match(int rank, UUID uuid, PlayerStats stats) {}

    private static final Comparator<Match> MATCH_ORDER = Comparator
            .comparingInt(Match::rank)
            .thenComparingLong(match -> -match.stats().getFollowersCount());

    private static final Comparator<Map.Entry<UUID, PlayerStats>> BOARD_ASCENDING = (a, b) -> {
        int byFollowers = Long.compare(a.getValue().getFollowersCount(), b.getValue().getFollowersCount());
        if (byFollowers != 0) return byFollowers;
        return Long.compare(a.getValue().getViewCount(), b.getValue().getViewCount());
    };

    private SocialQueries() {}

    private static ProfileEntry toEntry(String name, UUID uuid, PlayerStats stats, boolean online) {
        return new ProfileEntry(
                uuid,
                name,
                stats.getFollowersCount(),
                stats.getFollowingCount(),
                stats.getPostsCount(),
                stats.getViewCount(),
                stats.getLikeCount(),
                stats.getCommentCount(),
                online
        );
    }

    public static List<ProfileEntry> search(MinecraftServer server, String rawQuery) {
        String query = rawQuery.trim().toLowerCase(Locale.ROOT);
        SocialManager manager = SocialManager.get(server);
        List<ProfileEntry> result = new ArrayList<>();

        if (query.isEmpty()) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uuid = player.getUUID();
                result.add(toEntry(player.getName().getString(), uuid, manager.getStats(uuid), true));
            }
            result.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
            if (result.size() > SEARCH_LIMIT) {
                return new ArrayList<>(result.subList(0, SEARCH_LIMIT));
            }
            return result;
        }

        List<Match> matches = new ArrayList<>(SEARCH_LIMIT + 1);
        for (Map.Entry<UUID, PlayerStats> entry : manager.getAllStats().entrySet()) {
            PlayerStats stats = entry.getValue();
            String lower = stats.getNameLower();
            if (lower == null || lower.length() < query.length()) continue;
            int index = lower.indexOf(query);
            if (index < 0) continue;
            int rank = index == 0 ? (lower.length() == query.length() ? 0 : 1) : 2;
            matches.add(new Match(rank, entry.getKey(), stats));
            if (matches.size() > SEARCH_LIMIT) {
                matches.sort(MATCH_ORDER);
                matches.remove(matches.size() - 1);
            }
        }

        matches.sort(MATCH_ORDER);
        for (Match match : matches) {
            boolean online = server.getPlayerList().getPlayer(match.uuid()) != null;
            result.add(toEntry(match.stats().getName(), match.uuid(), match.stats(), online));
        }
        return result;
    }

    public static List<ProfileEntry> leaderboard(MinecraftServer server) {
        SocialManager manager = SocialManager.get(server);
        long now = System.currentTimeMillis();
        if (manager == cachedFor && now - cachedAt < LEADERBOARD_TTL_MS) {
            return cachedBoard;
        }

        PriorityQueue<Map.Entry<UUID, PlayerStats>> heap = new PriorityQueue<>(LEADERBOARD_SIZE + 1, BOARD_ASCENDING);
        for (Map.Entry<UUID, PlayerStats> entry : manager.getAllStats().entrySet()) {
            PlayerStats stats = entry.getValue();
            if (stats.getName() == null || stats.getFollowersCount() <= 0L) continue;
            heap.offer(entry);
            if (heap.size() > LEADERBOARD_SIZE) heap.poll();
        }

        List<Map.Entry<UUID, PlayerStats>> sorted = new ArrayList<>(heap);
        sorted.sort(BOARD_ASCENDING.reversed());

        List<ProfileEntry> board = new ArrayList<>(sorted.size());
        for (Map.Entry<UUID, PlayerStats> entry : sorted) {
            boolean online = server.getPlayerList().getPlayer(entry.getKey()) != null;
            board.add(toEntry(entry.getValue().getName(), entry.getKey(), entry.getValue(), online));
        }

        cachedFor = manager;
        cachedAt = now;
        cachedBoard = board;
        return board;
    }

    public static int rankOf(MinecraftServer server, UUID uuid) {
        SocialManager manager = SocialManager.get(server);
        PlayerStats me = manager.getStats(uuid);
        long followers = me.getFollowersCount();
        if (followers <= 0L || me.getName() == null) return 0;

        int rank = 1;
        for (PlayerStats other : manager.getAllStats().values()) {
            if (other != me && other.getName() != null && other.getFollowersCount() > followers) rank++;
        }
        return rank;
    }
}
