package com.github.brainage04.brainageserverutils.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public final class FeedbackUtils {
    private FeedbackUtils() {
    }

    public static void sendFeedback(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal(message), false);
    }
}
