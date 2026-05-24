package ru.nightlume.common.manager.command.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import ru.nightlume.Nightlume;
import ru.nightlume.common.manager.command.Command;

public class ConfigCommand extends Command {

    public ConfigCommand() { super("config"); }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            print("Usage: .config load <name> | .config save <name>");
            return;
        }

        String action = args[0];
        String name = args[1];

        if (action.equalsIgnoreCase("save")) {
            Nightlume.getInstance().getConfigManager().saveConfig(name);
            print("Saved config: " + name + ".cfg");
        } else if (action.equalsIgnoreCase("load")) {
            Nightlume.getInstance().getConfigManager().loadConfig(name);
            print("Loaded config: " + name + ".cfg");
        }
    }

    private void print(String text) {
        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(new StringTextComponent("§7[§bNightlume§7] §f" + text));
    }
}