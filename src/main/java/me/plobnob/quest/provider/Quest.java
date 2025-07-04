package me.plobnob.quest.provider;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import me.plobnob.quest.util.TextFormatter;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.WritableBookMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data class to contain quest information.
 * Serialize through the Bukkit ConfigurationSerializable interface
 */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@SerializableAs("Quest")
public class Quest implements ConfigurationSerializable, TextFormatter {

    // Member variables - all provided as getter and setter with lombok
    private String name;
    private String displayName;
    private String author;
    private String difficulty;
    private String description;
    private ItemStack displayStack;
    private List<String> pages;

    /*
        Suppressions:
            - Unchecked for List generic inference
            - Unused for deserialize constructor invocation
     */
    @SuppressWarnings({"unchecked", "unused"})
    public Quest(Map<String, Object> map) {
        name = (String) map.get("name");
        displayName = (String) map.get("displayName");
        author = (String) map.get("author");
        difficulty = (String) map.get("difficulty");
        description = (String) map.get("description");
        displayStack = (ItemStack) map.get("displayStack");
        pages = (List<String>) map.get("pages");
    }

    /**
     * Function to get the display stack.
     * Preference for this over default lombok to infer a default if the given stack is null or air
     * @return The generated itemstack for display
     */
    public ItemStack getDisplayStack() {
        if (displayStack == null || displayStack.getType() == Material.AIR) {
            displayStack = new ItemStack(Material.BOOK, 1);
        }

        ItemMeta meta = displayStack.getItemMeta();
        if (meta != null) {
            meta.displayName(format("<blue>%s</blue>", getDisplayName()));
            List<Component> lore = new ArrayList<>();
            lore.add(format("<gray>Difficulty: </gray>%s", difficulty));
            if (description != null)
                lore.addAll(wrappedFormat("<gray>Description: </gray>" + description, 60));
            meta.lore(lore);
        }
        displayStack.setItemMeta(meta);

        return displayStack;
    }

    /**
     * Function to generate a writable book for editing questbook content.
     * @return A generated itemstack for editing quest contents
     */
    public ItemStack writableBook() {
        ItemStack stack = new ItemStack(Material.WRITABLE_BOOK, 1);
        if (!(stack.getItemMeta() instanceof WritableBookMeta bookMeta))
            return null;
        bookMeta.displayName(format(displayName));
        bookMeta.setPages(pages);
        stack.setItemMeta(bookMeta);
        return stack;
    }

    /**
     * Function to generate a content book for quests.
     * This is adapter using placeholder API for the active player.
     * @param player The player to substitute values for.
     * @return The built book
     */
    public Book writeBookFor(Player player) {
        return Book.builder()
                .title(format(displayName))
                .author(format(author))
                .pages(pages.stream()
                        .map(page -> PlaceholderAPI.setPlaceholders(player, page))
                        .map(this::format)
                        .toList())
                .build();
    }

    /**
     * Utility function to serialize a class instance into a map for storing in yaml
     * @return A serialized map
     */
    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of(
                "name", getName(),
                "displayName", getDisplayName(),
                "author", getAuthor(),
                "difficulty", getDifficulty(),
                "description", getDescription(),
                "displayStack", getDisplayStack(),
                "pages", getPages()
        );
    }
}
