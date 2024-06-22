package me.loloed.bot.api.impl;

import net.minecraft.server.level.ServerPlayer;

public interface IServerBot {
    ServerPlayer getSpawner();

    ServerBotSettings getSettings();

    ServerPlayer getVanillaPlayer();

    void destroy();
}
