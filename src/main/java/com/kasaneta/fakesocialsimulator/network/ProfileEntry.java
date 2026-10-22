package com.kasaneta.fakesocialsimulator.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ProfileEntry(UUID uuid, String name, long followers, long following, long posts, long views, long likes, long comments, boolean online) {

    public static final int MAX_ENTRIES = 16;

    public static void write(FriendlyByteBuf buf, ProfileEntry entry) {
        buf.writeUUID(entry.uuid());
        buf.writeUtf(entry.name(), 16);
        buf.writeVarLong(entry.followers());
        buf.writeVarLong(entry.following());
        buf.writeVarLong(entry.posts());
        buf.writeVarLong(entry.views());
        buf.writeVarLong(entry.likes());
        buf.writeVarLong(entry.comments());
        buf.writeBoolean(entry.online());
    }

    public static ProfileEntry read(FriendlyByteBuf buf) {
        return new ProfileEntry(
                buf.readUUID(),
                buf.readUtf(16),
                buf.readVarLong(),
                buf.readVarLong(),
                buf.readVarLong(),
                buf.readVarLong(),
                buf.readVarLong(),
                buf.readVarLong(),
                buf.readBoolean()
        );
    }

    public static void writeList(FriendlyByteBuf buf, List<ProfileEntry> entries) {
        int size = Math.min(entries.size(), MAX_ENTRIES);
        buf.writeVarInt(size);
        for (int i = 0; i < size; i++) {
            write(buf, entries.get(i));
        }
    }

    public static List<ProfileEntry> readList(FriendlyByteBuf buf) {
        int size = Math.max(0, Math.min(buf.readVarInt(), MAX_ENTRIES));
        List<ProfileEntry> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(read(buf));
        }
        return list;
    }
}
