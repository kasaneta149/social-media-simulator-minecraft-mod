package com.kasaneta.fakesocialsimulator.command;

import com.kasaneta.fakesocialsimulator.SocialStatsMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = SocialStatsMod.MODID, value = Dist.CLIENT)
public class CommandRegistry {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        ToggleNametagCommand.register(event.getDispatcher());
    }
}
