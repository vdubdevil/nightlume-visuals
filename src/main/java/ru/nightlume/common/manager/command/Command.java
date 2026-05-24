package ru.nightlume.common.manager.command;

public abstract class Command {
    private final String name;
    public Command(String name) { this.name = name; }
    public abstract void execute(String[] args);
    public String getName() { return name; }
}