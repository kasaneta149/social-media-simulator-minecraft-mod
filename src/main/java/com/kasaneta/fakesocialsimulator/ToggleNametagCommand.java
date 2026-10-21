package com.kasaneta.fakesocialsimulator.command;

import com.kasaneta.fakesocialsimulator.client.NametagRenderer;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ToggleNametagCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("socialnametag")
            .executes(context -> {
                NametagRenderer.toggleClientNametag();
                boolean state = NametagRenderer.isClientToggleEnabled();
                
                Component stateText = Component.translatable(state
                        ? "command.fakesocialsimulator.nametag.on"
                        : "command.fakesocialsimulator.nametag.off")
                        .withStyle(state ? ChatFormatting.GREEN : ChatFormatting.RED);

                context.getSource().sendSuccess(() -> Component.translatable(
                    "command.fakesocialsimulator.nametag", stateText
                ), false);
                return 1;
            })
        );
    }
}