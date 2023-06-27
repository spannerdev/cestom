package com.spanner.cestom.command;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;

public class StopCommand extends Command {
    public StopCommand() {
        super("stop", "shutdown");
        setCondition((sender, commandString) ->
                sender instanceof ConsoleSender
             || sender.hasPermission(Permissions.STOP)
        );
        setDefaultExecutor((sender, context) -> {
            MinecraftServer.stopCleanly();
        });
    }
}
