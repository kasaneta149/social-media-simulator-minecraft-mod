package com.kasaneta.fakesocialsimulator.network;

import com.kasaneta.fakesocialsimulator.SocialStatsMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ProfileSearchRequestPayload(String query) implements CustomPacketPayload {

    public static final Type<ProfileSearchRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SocialStatsMod.MODID, "profile_search_request"));

    public static final StreamCodec<FriendlyByteBuf, ProfileSearchRequestPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.query(), 16),
            buf -> new ProfileSearchRequestPayload(buf.readUtf(16))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
