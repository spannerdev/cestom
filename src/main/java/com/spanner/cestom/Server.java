package com.spanner.cestom;

import com.spanner.cestom.command.StopCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;

import static net.minestom.server.MinecraftServer.LOGGER;

public class Server {

    private static final String VERSION = "&VERSION";
    private static final String BRAND = "Cestom";


    public static void main(String[] args) {

        Config.load();
        MinecraftServer server = MinecraftServer.init();

        setupMode();

        MinecraftServer.getCommandManager().register(new StopCommand());

        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
            if (MinecraftServer.getInstanceManager().getInstances().isEmpty()) {
                event.getPlayer().kick(Component.text("No instances available", NamedTextColor.RED));
            }
        });

        CestomTerminalWriter.start();

        LOGGER.info(BRAND + " " + VERSION);
        LOGGER.info("Server is in " + Config.getMode() + " mode");
        LOGGER.info("Listening to " + Config.getHost() + ":" + Config.getPort());

        server.start(Config.getListenAddress());
    }

    private static void setupMode() {
        switch (Config.getMode()) {
            case OFFLINE -> {}
            case ONLINE -> { MojangAuth.init(); }
            case BUNGEECORD -> { BungeeCordProxy.enable(); }
            case VELOCITY -> { VelocityProxy.enable(Config.getVelocitySecret()); }
        }
    }

}
