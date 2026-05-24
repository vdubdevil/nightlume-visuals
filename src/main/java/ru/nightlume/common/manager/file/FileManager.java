package ru.nightlume.common.manager.file;

import java.io.File;
import java.io.IOException;

public class FileManager {

    public static final File ROOT_DIR = new File(System.getProperty("user.home"), "Nightlume");
    public static final File CONFIG_DIR = new File(ROOT_DIR, "configs");
    public static final File THEME_DIR = new File(ROOT_DIR, "themes");
    public static final File BIND_DIR = new File(ROOT_DIR, "binds");
    public static final File ACCOUNTS_FILE = new File(ROOT_DIR, "accounts.nl");

    public void init() {
        if (!ROOT_DIR.exists()) ROOT_DIR.mkdirs();
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        if (!THEME_DIR.exists()) THEME_DIR.mkdirs();
        if (!BIND_DIR.exists()) BIND_DIR.mkdirs();

        try {
            if (!ACCOUNTS_FILE.exists()) {
                ACCOUNTS_FILE.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Failed to create Nightlume ecosystem files!");
            e.printStackTrace();
        }
    }
}