package ru.nightlume.common.manager.command.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import ru.nightlume.Nightlume;
import ru.nightlume.common.manager.command.Command;
import ru.nightlume.module.Module;

public class BindCommand extends Command {

    public BindCommand() {
        super("bind");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            print("Usage: .bind module <name> <key> | .bind delete <name>");
            return;
        }

        if (args[0].equalsIgnoreCase("module") && args.length >= 3) {
            Module target = Nightlume.getInstance().getModuleManager().getModuleByName(args[1]);
            if (target != null) {

                target.setKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
                print("Bound " + target.getName());
            }
        }

        if (args[0].equalsIgnoreCase("delete") && args.length >= 2) {
            Module target = Nightlume.getInstance().getModuleManager().getModuleByName(args[1]);
            if (target != null) {
                target.setKey(0);
                print("Unbound " + target.getName());
            }
        }
    }

    private void print(String text) {
        if (Minecraft.getInstance().ingameGUI != null && Minecraft.getInstance().ingameGUI.getChatGUI() != null) {
            Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(new StringTextComponent("§7[§bNightlume§7] §f" + text));
        }
    }
}