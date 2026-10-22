package com.kasaneta.fakesocialsimulator.network;

import com.kasaneta.fakesocialsimulator.SocialStatsMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record ProfileSearchResultPayload(List<ProfileEntry> entries) implements CustomPacketPayload {

    public static final Type<ProfileSearchResultPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SocialStatsMod.MODID, "profile_search_result"));

    public static final StreamCodec<FriendlyByteBuf, ProfileSearchResultPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> ProfileEntry.writeList(buf, payload.entries()),
            buf -> new ProfileSearchResultPayload(ProfileEntry.readList(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
