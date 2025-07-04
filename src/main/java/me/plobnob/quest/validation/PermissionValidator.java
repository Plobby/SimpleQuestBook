package me.plobnob.quest.validation;

import org.bukkit.command.CommandSender;

import static me.plobnob.quest.util.PluginConstants.*;

/**
 * Utility interface to handle basic permission checks.
 */
public interface PermissionValidator {

    /**
     * Function to handle a no permission instance
     * @param sender The command sender
     * @return true - command was handled
     */
    default boolean noPermission(CommandSender sender) {
        sender.sendMessage(TEXT_NO_PERMISSION);
        return true;
    }

    /**
     * Function to handle a no valid command instance
     * @param sender The command sender
     * @return true - command was handled
     */
    default boolean noCommand(CommandSender sender) {
        sender.sendMessage(TEXT_NO_COMMAND);
        return true;
    }

    /**
     * Function to handle a not a player instance
     * @param sender The command sender
     * @return true - command was handled
     */
    default boolean notPlayer(CommandSender sender) {
        sender.sendMessage(TEXT_NOT_PLAYER);
        return true;
    }

}
