package ru.nightlume;

import ru.nightlume.api.event.EventBus;
import ru.nightlume.common.manager.command.CommandManager;
import ru.nightlume.common.manager.file.ConfigManager;
import ru.nightlume.common.manager.file.FileManager;
import ru.nightlume.common.manager.hud.HudManager;
import ru.nightlume.common.manager.input.InputManager;
import ru.nightlume.common.manager.module.ModuleManager;
import ru.nightlume.common.manager.theme.ThemeManager;

public class Nightlume {

    private static Nightlume instance;

    private final FileManager fileManager;
    private final ConfigManager configManager;
    private final CommandManager commandManager;

    private final ModuleManager moduleManager;
    private final InputManager inputManager;
    private final HudManager hudManager;
    private final ThemeManager themeManager;

    public Nightlume() {
        instance = this;
        this.fileManager = new FileManager();
        this.configManager = new ConfigManager();
        this.commandManager = new CommandManager();

        this.moduleManager = new ModuleManager();
        this.inputManager = new InputManager();
        this.hudManager = new HudManager();
        this.themeManager = new ThemeManager();
    }

    public static void init() {
        if (instance == null) {
            new Nightlume().start();
        }
    }

    public void start() {
        fileManager.init();
        commandManager.init();
        moduleManager.init();

        EventBus.register(moduleManager);
        EventBus.register(hudManager);

        configManager.loadConfig("default");
    }

    public void shutdown() {
        configManager.saveConfig("default"); // Auto-save on quit
        EventBus.unregister(moduleManager);
        EventBus.unregister(hudManager);
    }

    public static Nightlume getInstance() { return instance; }
    public FileManager getFileManager() { return fileManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public CommandManager getCommandManager() { return commandManager; }
    public ModuleManager getModuleManager() { return moduleManager; }
    public InputManager getInputManager() { return inputManager; }
    public HudManager getHudManager() { return hudManager; }
    public ThemeManager getThemeManager() { return themeManager; }
}