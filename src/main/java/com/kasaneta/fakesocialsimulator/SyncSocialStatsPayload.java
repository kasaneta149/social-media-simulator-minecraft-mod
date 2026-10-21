package com.kasaneta.fakesocialsimulator.network;

import com.kasaneta.fakesocialsimulator.SocialStatsMod;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SyncSocialStatsPayload(UUID playerUUID, long posts, long followers, long following) implements CustomPacketPayload {

    public static final Type<SyncSocialStatsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SocialStatsMod.MODID, "sync_social_stats"));

    public static final StreamCodec<FriendlyByteBuf, SyncSocialStatsPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SyncSocialStatsPayload::playerUUID,
            ByteBufCodecs.VAR_LONG, SyncSocialStatsPayload::posts,
            ByteBufCodecs.VAR_LONG, SyncSocialStatsPayload::followers,
            ByteBufCodecs.VAR_LONG, SyncSocialStatsPayload::following,
            SyncSocialStatsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}