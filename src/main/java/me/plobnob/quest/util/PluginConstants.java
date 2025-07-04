package me.plobnob.quest.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class PluginConstants {

    // Command constants
    public static final String CMD_QUESTBOOK = "questbook";

    // Permission constants
    public static final String PERM_QUESTBOOK = "questbook";
    public static final String PERM_QUESTBOOK_OPEN = "questbook.open";
    public static final String PERM_QUESTBOOK_VIEW_QUEST = "questbook.view.%s";
    public static final String PERM_QUESTBOOK_VIEW_ALL = "questbook.view.all";
    public static final String PERM_QUESTBOOK_EDIT = "questbook.edit";
    public static final String PERM_QUESTBOOK_CREATE = "questbook.create";
    public static final String PERM_QUESTBOOK_DELETE = "questbook.delete";

    // Text constants (permission checks)
    public static final Component TEXT_NO_PERMISSION = MiniMessage.miniMessage()
            .deserialize("<red>You do not have permission to execute that command!</red>");
    public static final Component TEXT_NO_COMMAND = MiniMessage.miniMessage()
            .deserialize("<red>The command you specified was not valid!</red>");
    public static final Component TEXT_NOT_PLAYER = MiniMessage.miniMessage()
            .deserialize("<red>Only a player can use this command!</red>");

    // Title constants (item naming)
    public static final Component TITLE_QUESTBOOK = MiniMessage.miniMessage().deserialize("<yellow>Quests</yellow>");
    public static final Component TITLE_EMPTY = MiniMessage.miniMessage().deserialize("<black></black>");

}
