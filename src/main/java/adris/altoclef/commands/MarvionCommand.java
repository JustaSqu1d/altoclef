package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.speedrun.ModernBeatMinecraftTask;

public class MarvionCommand extends Command {
    public MarvionCommand() {
        super("gamer", "Beats the game (Modern version).");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.runUserTask(new ModernBeatMinecraftTask(), this::finish);
    }
}