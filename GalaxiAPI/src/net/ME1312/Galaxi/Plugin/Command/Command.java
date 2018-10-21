package net.ME1312.Galaxi.Plugin.Command;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Exception.IllegalStringValueException;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Plugin.PluginInfo;

/**
 * Command Layout Class
 */
public abstract class Command {
    private String desc = null;
    private String[] exDesc = new String[0];
    private String[] usage = new String[0];
    private CompletionHandler autocomplete = (this instanceof CompletionHandler)?(CompletionHandler)this:null;
    private PluginInfo plugin;

    /**
     * Creates a new Command
     *
     * @param plugin Plugin
     */
    public Command(PluginInfo plugin) {
        if (Util.isNull(plugin)) throw new NullPointerException();
        this.plugin = plugin;
    }

    /**
     * Run Command
     *
     * @param sender Command Sender
     * @param handle Command Name
     * @param args Arguments
     */
    public abstract void command(CommandSender sender, String handle, String[] args);

    /**
     * Gets the AutoComplete handler for this Command
     *
     * @return AutoComplete Handler
     */
    public CompletionHandler autocomplete() {
        return this.autocomplete;
    }

    /**
     * Sets an AutoComplete handler for this Command
     *
     * @param value AutoComplete Handler
     * @return The Command
     */
    public Command autocomplete(CompletionHandler value) {
        this.autocomplete = value;
        return this;
    }

    /**
     * Gets the Plugin that registering this Command
     *
     * @return Plugin Info
     */
    public PluginInfo plugin() {
        return this.plugin;
    }

    /**
     * Gets the Description of this Command
     *
     * @return Command Description
     */
    public String description() {
        return this.desc;
    }

    /**
     * Set the Description of this Command
     *
     * @param value Value
     * @return The Command
     */
    public Command description(String value) {
        if (value != null) {
            if (value.length() == 0) throw new StringIndexOutOfBoundsException("Cannot use empty string for description");
            if (value.contains("\n")) throw new IllegalStringValueException("String contains illegal character(s)");
        }
        this.desc = value;
        return this;
    }

    /**
     * Get the Help Page for this Command
     *
     * @return Help Page
     */
    public String[] help() {
        if (exDesc.length == 0 && desc != null) {
            return new String[]{desc};
        } else {
            return exDesc;
        }
    }

    /**
     * Set the Help Page for this Command
     *
     * @param lines Help Page Lines
     * @return The Command
     */
    public Command help(String... lines) {
        for (String line : lines) {
            if (line.contains("\n")) throw new IllegalStringValueException("String contains illegal character(s)");
        }
        this.exDesc = lines;
        return this;
    }

    /**
     * Get the Usage of this Command
     *
     * @return Command Usage
     */
    public String[] usage() {
        return this.usage;
    }

    /**
     * Set the Usage of this Command
     *
     * @param args Argument Placeholders
     * @return The Command
     */
    public Command usage(String... args) {
        for (String arg : args) {
            if (arg.length() == 0) throw new StringIndexOutOfBoundsException("Cannot use empty string for usage");
            if (arg.contains(" ") || arg.contains("\n")) throw new IllegalStringValueException("String contains illegal character(s)");
        }
        this.usage = args;
        return this;
    }

    /**
     * Register this Command
     *
     * @param handles Aliases
     */
    public void register(String... handles) {
        Galaxi.getInstance().getPluginManager().addCommand(this, handles);
    }
}
