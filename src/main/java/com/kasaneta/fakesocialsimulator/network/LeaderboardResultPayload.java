package com.kasaneta.fakesocialsimulator.network;

import com.kasaneta.fakesocialsimulator.SocialStatsMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record LeaderboardResultPayload(List<ProfileEntry> entries, int yourRank) implements CustomPacketPayload {

    public static final Type<LeaderboardResultPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SocialStatsMod.MODID, "leaderboard_result"));

    public static final StreamCodec<FriendlyByteBuf, LeaderboardResultPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ProfileEntry.writeList(buf, payload.entries());
                buf.writeVarInt(payload.yourRank());
            },
            buf -> {
                List<ProfileEntry> entries = ProfileEntry.readList(buf);
                return new LeaderboardResultPayload(entries, buf.readVarInt());
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
