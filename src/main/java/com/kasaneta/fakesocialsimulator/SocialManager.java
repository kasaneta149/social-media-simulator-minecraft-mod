package com.kasaneta.fakesocialsimulator;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SocialManager extends SavedData {
    private final Map<UUID, PlayerStats> playerStatsMap = new HashMap<>();

    public PlayerStats getStats(UUID uuid) {
        return playerStatsMap.computeIfAbsent(uuid, k -> {
            setDirty();
            return new PlayerStats();
        });
    }

    public Map<UUID, PlayerStats> getAllStats() {
        return playerStatsMap;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, PlayerStats> entry : playerStatsMap.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("uuid", entry.getKey());
            entry.getValue().save(playerTag);
            list.add(playerTag);
        }
        tag.put("players", list);
        return tag;
    }

    public static SocialManager load(CompoundTag tag, HolderLookup.Provider provider) {
        SocialManager manager = new SocialManager();
        ListTag list = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag playerTag = list.getCompound(i);
            UUID uuid = playerTag.getUUID("uuid");
            manager.playerStatsMap.put(uuid, PlayerStats.load(playerTag));
        }
        return manager;
    }

    public static SavedData.Factory<SocialManager> factory() {
        return new SavedData.Factory<>(SocialManager::new, SocialManager::load, null);
    }

    public static SocialManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), "fakesocialsimulator_stats");
    }
}