package me.plobnob.quest.listener;

import lombok.RequiredArgsConstructor;
import me.plobnob.quest.provider.Quest;
import me.plobnob.quest.provider.QuestProvider;
import net.kyori.adventure.inventory.Book;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.plobnob.quest.util.PluginConstants.*;

/**
 * A listener class to observe inventory click event and handle GUI events for the questbook system.
 */
@RequiredArgsConstructor
public class QuestBookClickEvent implements Listener {

    // Utility member variables (itemstacks and active inventory tracking)
    private final ItemStack borderItem = buildBorderItem();
    private final ItemStack questBookItem = buildQuestBookItem();
    private final Map<UUID, InventoryView> activeInventories = new HashMap<>();

    // Quest provider (set by lombok)
    private final QuestProvider questProvider;

    /**
     * A listener to handle players clicking inside the questbook GUI.
     * @param event The event parameters
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onQuestInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        UUID uuid = player.getUniqueId();

        if (!activeInventories.containsKey(uuid))
            return;

        InventoryView storedView = activeInventories.get(uuid);
        if (player.getOpenInventory() != storedView) {
            activeInventories.remove(uuid);
            return;
        }

        event.setCancelled(true);

        ItemStack clickedStack = event.getCurrentItem();
        if (clickedStack == null)
            return;

        Quest clickedQuest = questProvider.getQuests()
                .stream()
                .filter(quest -> clickedStack.equals(quest.getDisplayStack()))
                .findFirst()
                .orElse(null);

        if (clickedQuest == null)
            return;

        player.closeInventory();
        activeInventories.remove(uuid);

        Book book = clickedQuest.writeBookFor(player);
        player.openBook(book);
    }

    /**
     * Utility function to open the questbook inventory for a given player.
     * Binds the inventory instance into a map to handle inventory click events.
     * @param player The player to open the inventory for
     */
    public void openInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 54, TITLE_QUESTBOOK);

        for (int i = 0; i <= 8; i++)
            inventory.setItem(i, borderItem);
        for (int i = 45; i <= 53; i++)
            inventory.setItem(i, borderItem);
        inventory.setItem(4, questBookItem);

        int index = 9;
        for (Quest quest : questProvider.getQuests()) {
            if (!player.hasPermission(PERM_QUESTBOOK_VIEW_ALL) && !player.hasPermission(String.format(PERM_QUESTBOOK_VIEW_QUEST, quest.getName())))
                continue;
            if (index > 36)
                break;
            inventory.setItem(index++, quest.getDisplayStack());
        }

        InventoryView view = player.openInventory(inventory);
        activeInventories.put(player.getUniqueId(), view);
    }

    /**
     * Utility function to clear all active inventory bindings and close inventories (in the event of a reload).
     */
    public void clearBindings() {
        for (Map.Entry<UUID, InventoryView> entry : activeInventories.entrySet()) {
            entry.getValue().close();
        }
        activeInventories.clear();
    }

    /**
     * Utility function to build border items for the questbook UI.
     * @return A border itemstack
     */
    private ItemStack buildBorderItem() {
        ItemStack stack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(TITLE_EMPTY);
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * Utility function to build questbook title item for the questbook UI.
     * @return A questbook title itemstack
     */
    private ItemStack buildQuestBookItem() {
        ItemStack stack = new ItemStack(Material.BOOK, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(TITLE_QUESTBOOK);
        meta.setEnchantmentGlintOverride(true);
        stack.setItemMeta(meta);
        return stack;
    }

}
