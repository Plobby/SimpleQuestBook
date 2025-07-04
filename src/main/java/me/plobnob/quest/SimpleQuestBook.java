package me.plobnob.quest;

import me.plobnob.quest.command.QuestBookCommand;
import me.plobnob.quest.command.QuestBookCompleter;
import me.plobnob.quest.listener.QuestBookClickEvent;
import me.plobnob.quest.listener.QuestBookUpdatedEvent;
import me.plobnob.quest.provider.Quest;
import me.plobnob.quest.provider.QuestProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import static me.plobnob.quest.util.PluginConstants.CMD_QUESTBOOK;

/**
 * Main entry point for the plugin.
 */
public final class SimpleQuestBook extends JavaPlugin {

    // Member variables - saved for unregistering on plugin disable
    QuestBookClickEvent questBookClickEvent;
    QuestBookUpdatedEvent questBookUpdatedEvent;

    /**
     * OnEnable event for plugin.
     */
    @Override
    public void onEnable() {
        // Plugin startup
        getComponentLogger().info("Enabling SimpleQuestBook");

        // Configuration
        ConfigurationSerialization.registerClass(Quest.class, "Quest");
        QuestProvider questProvider = new QuestProvider(this);

        // Listeners
        PluginManager pm = Bukkit.getServer().getPluginManager();
        questBookClickEvent = new QuestBookClickEvent(questProvider);
        pm.registerEvents(questBookClickEvent, this);
        questBookUpdatedEvent = new QuestBookUpdatedEvent(questProvider);
        pm.registerEvents(questBookUpdatedEvent, this);

        // Commands
        PluginCommand cmdQuestbook = getCommand(CMD_QUESTBOOK);
        if (cmdQuestbook == null) {
            getComponentLogger().error("Unexpected error occurred while loading SimpleQuestBook, disabling plugin!");
            pm.disablePlugin(this);
            return;
        }

        QuestBookCommand questBookCommand = new QuestBookCommand(questBookClickEvent, questBookUpdatedEvent, questProvider);
        QuestBookCompleter questBookCompleter = new QuestBookCompleter(questProvider);

        cmdQuestbook.setExecutor(questBookCommand);
        cmdQuestbook.setTabCompleter(questBookCompleter);
    }

    /**
     * OnDisable event for plugin.
     */
    @Override
    public void onDisable() {
        // Plugin shutdown
        getComponentLogger().info("Disabling SimpleQuestBook");

        // Unregister any active cache bindings for handlers - inventories will need to be closed
        questBookUpdatedEvent.getDistributedBooks().clear();
        questBookClickEvent.clearBindings();
    }

}
