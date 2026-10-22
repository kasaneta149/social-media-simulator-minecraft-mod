package com.kasaneta.fakesocialsimulator;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@Mod(SocialStatsMod.MODID)
public class SocialStatsMod {
    public static final String MODID = "fakesocialsimulator";

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<PlayerStats>> PLAYER_STATS =
            ATTACHMENT_TYPES.register("player_stats", () ->
                    AttachmentType.builder(PlayerStats::new)
                            .serialize(PlayerStats.CODEC)
                            .copyOnDeath()
                            .build()
            );

    public SocialStatsMod(IEventBus modEventBus, ModContainer modContainer) {
        ATTACHMENT_TYPES.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBlocks.BLOCK_ENTITIES.register(modEventBus);

        modEventBus.addListener(SocialStatsMod::addCreativeTabContents);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.SUBSCRIPTION_BLOCK_ITEM.get());
            event.accept(ModBlocks.POST_BLOCK_ITEM.get());
            event.accept(ModBlocks.PROFILE_BLOCK_ITEM.get());
            event.accept(ModBlocks.PROFILE_VIEWER_BLOCK_ITEM.get());
            event.accept(ModBlocks.LEADERBOARD_BLOCK_ITEM.get());
        }
    }
}
