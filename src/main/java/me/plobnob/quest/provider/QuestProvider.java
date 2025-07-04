package me.plobnob.quest.provider;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to manage and cache the contents of quests available for players
 */
public class QuestProvider {

    // Member variables - not injected with lombok due to instantiation order requirements
    @Getter
    public List<Quest> quests;
    private FileConfiguration questsConfig;
    private final Plugin plugin;
    private final File questsFile;

    /**
     * Default constructor.
     * @param plugin The plugin instance
     */
    public QuestProvider(Plugin plugin) {
        this.plugin = plugin;
        this.questsFile = new File(plugin.getDataFolder(), "quests.yml");
        loadConfig();
    }

    /**
     * Function to register a given quest.
     * @param quest The quest to register
     */
    public void registerQuest(Quest quest) {
        quests.add(quest);
        saveConfig();
    }

    /**
     * Function to unregister a given quest.
     * @param quest The quest to unregister
     */
    public void unregisterQuest(Quest quest) {
        quests.remove(quest);
        saveConfig();
    }

    /**
     * Function to save quests - primarily after an edit to the class list has been made.
     */
    public void saveQuests() {
        saveConfig();
    }

    /**
     * Utility function to load the config.
     */
    @SuppressWarnings("unchecked")
    private void loadConfig() {
        if (!questsFile.exists()) {
            if (!questsFile.getParentFile().mkdirs())
                plugin.getComponentLogger().warn("Failed to generate directory path for the quests.yml file - this could be problematic!");
        }
        questsConfig = YamlConfiguration.loadConfiguration(questsFile);

        try {
            quests = (List<Quest>) questsConfig.getList("quests");
            if (quests == null)
                quests = new ArrayList<>();
        } catch (Exception ex) {
            plugin.getComponentLogger().error("Failed to load quests config!");
        }
    }

    /**
     * Utility function to save the config.
     */
    private void saveConfig() {
        questsConfig.set("quests", quests);

        try {
            questsConfig.save(questsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
