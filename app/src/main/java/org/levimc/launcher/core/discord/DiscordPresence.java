package org.levimc.launcher.core.discord;

import android.app.Application;

/**
 * Discord Rich Presence is intentionally disabled in the AetherPvP fork.
 *
 * This compatibility shim keeps existing lifecycle call sites safe while
 * removing the private Discord Social SDK dependency. No Discord service is
 * started and no user activity is transmitted.
 */
public final class DiscordPresence {
    private DiscordPresence() {}

    public static void initialize(Application application) {
        // Intentionally disabled in this fork.
    }

    public static boolean isConfigured() {
        return false;
    }

    public static void refresh() {
        // Intentionally disabled in this fork.
    }

    public static void shutdown() {
        // Intentionally disabled in this fork.
    }
}
