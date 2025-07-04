package me.plobnob.quest.listener;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.plobnob.quest.provider.Quest;
import me.plobnob.quest.provider.QuestProvider;
import me.plobnob.quest.util.TextFormatter;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A listener class to handle player sign book events to register changed questbook content.
 */
@RequiredArgsConstructor
public class QuestBookUpdatedEvent implements Listener, TextFormatter {

    // In-memory map for binding book metadata to a quest it should update
    @Getter
    private final Map<BookMeta, Quest> distributedBooks = new HashMap<>();

    // Quest provider (set by lombok)
    private final QuestProvider questProvider;

    /**
     * A listener to handle players finishing editing or signing books.
     * @param event The event parameters
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onQuestBookUpdated(PlayerEditBookEvent event) {
        BookMeta oldMeta = event.getPreviousBookMeta();
        if (!distributedBooks.containsKey(oldMeta))
            return;

        Quest quest = distributedBooks.get(oldMeta);
        BookMeta newMeta = event.getNewBookMeta();

        quest.setPages(newMeta.pages()
                .stream()
                .filter(component -> component instanceof TextComponent)
                .map(component -> ((TextComponent)component).content())
                .toList());

        event.getPlayer().sendMessage(format("<yellow>Quest content has successfully been updated!</yellow>"));
        questProvider.saveQuests();

        distributedBooks.remove(oldMeta);
        if (!event.isSigning()) {
            distributedBooks.put(newMeta, quest);
        } else {
            Arrays.stream(event.getPlayer().getInventory().getContents())
                    .filter(item -> item != null && item.hasItemMeta() && item.getItemMeta() instanceof BookMeta)
                    .filter(item -> item.getItemMeta() == newMeta)
                    .findFirst()
                    .ifPresent(item -> {
                        event.setCancelled(true);
                        event.getPlayer().getInventory().removeItem(item);
                        // TODO Does this work and is it needed?
                    });
        }
    }

}
