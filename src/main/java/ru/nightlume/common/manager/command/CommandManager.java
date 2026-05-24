package ru.nightlume.common.manager.command;

import ru.nightlume.common.manager.command.impl.BindCommand;
import ru.nightlume.common.manager.command.impl.ConfigCommand;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final String PREFIX = ".";
    private final List<Command> commands = new ArrayList<>();

    public void init() {
        commands.add(new BindCommand());
        commands.add(new ConfigCommand());
    }

    public boolean processCommand(String message) {
        if (!message.startsWith(PREFIX)) return false;

        String[] split = message.substring(PREFIX.length()).split(" ");
        String commandName = split[0];
        String[] args = new String[split.length - 1];
        System.arraycopy(split, 1, args, 0, split.length - 1);

        for (Command command : commands) {
            if (command.getName().equalsIgnoreCase(commandName)) {
                command.execute(args);
                return true;
            }
        }
        return true;
    }
}