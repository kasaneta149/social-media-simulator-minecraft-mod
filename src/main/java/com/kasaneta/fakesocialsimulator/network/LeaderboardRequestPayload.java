package com.kasaneta.fakesocialsimulator.network;

import com.kasaneta.fakesocialsimulator.SocialStatsMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LeaderboardRequestPayload() implements CustomPacketPayload {

    public static final Type<LeaderboardRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SocialStatsMod.MODID, "leaderboard_request"));

    public static final StreamCodec<FriendlyByteBuf, LeaderboardRequestPayload> STREAM_CODEC = StreamCodec.unit(new LeaderboardRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
