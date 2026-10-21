package com.kasaneta.fakesocialsimulator.client;

import com.kasaneta.fakesocialsimulator.Config;
import com.kasaneta.fakesocialsimulator.SocialStatsMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

@EventBusSubscriber(modid = SocialStatsMod.MODID, value = Dist.CLIENT)
public class NametagRenderer {

    private static boolean clientToggleEnabled = true;

    public static void toggleClientNametag() {
        clientToggleEnabled = !clientToggleEnabled;
    }

    public static boolean isClientToggleEnabled() {
        return clientToggleEnabled;
    }

    private static boolean isEnabledByServer() {
        try {
            return Config.SHOW_NAMETAG_STATS.get();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientStatsCache.clear();
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!clientToggleEnabled) return;

        if (!(event.getEntity() instanceof Player player)) return;

        ClientStatsCache.PlayerMetrics stats = ClientStatsCache.get(player.getUUID());
        if (stats == null) return;

        if (!isEnabledByServer()) return;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        poseStack.translate(0.0D, player.getBbHeight() + 0.5D, 0.0D);

        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);
        poseStack.translate(0.0D, -12.0D, 0.0D);

        Component text = stats.text(Minecraft.getInstance().getLanguageManager().getSelected());

        Font font = Minecraft.getInstance().font;
        float xOffset = -font.width(text) / 2.0F;

        int backgroundColor = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;

        font.drawInBatch(
                text,
                xOffset,
                0,
                0xFFFFFFFF,
                false,
                poseStack.last().pose(),
                event.getMultiBufferSource(),
                Font.DisplayMode.SEE_THROUGH,
                backgroundColor,
                event.getPackedLight()
        );

        poseStack.popPose();
    }
}
