package me.plobnob.quest.command;

import lombok.RequiredArgsConstructor;
import me.plobnob.quest.listener.QuestBookClickEvent;
import me.plobnob.quest.listener.QuestBookUpdatedEvent;
import me.plobnob.quest.provider.Quest;
import me.plobnob.quest.provider.QuestProvider;
import me.plobnob.quest.util.TextFormatter;
import me.plobnob.quest.validation.PermissionValidator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static me.plobnob.quest.util.PluginConstants.*;

/**
 * A handler class for the questbook command.
 * Includes opening, creating, editing and deleting quests.
 */
@RequiredArgsConstructor
public class QuestBookCommand implements CommandExecutor, PermissionValidator, TextFormatter {

    // Quest book providers (set by lombok)
    private final QuestBookClickEvent questBookClickEvent;
    private final QuestBookUpdatedEvent questBookUpdatedEvent;
    private final QuestProvider questProvider;

    /**
     * Handler for the questbook command
     * @param sender The command sender
     * @param command The command used
     * @param label The command label
     * @param args Any args passed to the command
     * @return Whether the command call was handled
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(PERM_QUESTBOOK))
            return noPermission(sender);

        if (args.length == 0)
            return callOpen(sender);

        return switch (args[0].toLowerCase()) {
            case "open" -> callOpen(sender);
            case "edit" -> callEdit(sender, args);
            case "create" -> callCreate(sender, args);
            case "delete" -> callDelete(sender, args);
            default -> noCommand(sender);
        };
    }

    /**
     * Utility function to handle opening the questbooks UI
     * @param sender The command sender
     * @return Whether the command call was handled
     */
    private boolean callOpen(CommandSender sender) {
        if (!sender.hasPermission(PERM_QUESTBOOK_OPEN))
            return noPermission(sender);

        if (!(sender instanceof Player player))
            return notPlayer(sender);

        questBookClickEvent.openInventory(player);
        return true;
    }

    /**
     * Utility function to handle editing a quest
     * @param sender The command sender
     * @param args Any args passed to the edit command
     * @return Whether the command call was handled
     */
    private boolean callEdit(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERM_QUESTBOOK_EDIT))
            return noPermission(sender);

        if (args.length < 3) {
            sender.sendMessage(format("<yellow>Usage: /questbook edit <questname> <displayName | author | difficulty | description | itemstack | pages> [value]</yellow>"));
            return true;
        }

        if (args.length < 4 && List.of("displayname", "author", "difficulty", "description").contains(args[2].toLowerCase())) {
            sender.sendMessage(format("<yellow>Usage: /questbook edit <questname> <%s> <value>", args[2].toLowerCase()));
            return true;
        }

        Quest targetQuest = questProvider.getQuests()
                .stream()
                .filter(quest -> args[1].equalsIgnoreCase(quest.getName()))
                .findFirst()
                .orElse(null);

        if (targetQuest == null) {
            sender.sendMessage(format("<red>Error: Could not find the quest </red><gray>%s</gray><red>!</red>", args[2]));
            return true;
        }

        String lastArgument = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        switch (args[2].toLowerCase()) {
            case "displayname" -> {
                targetQuest.setDisplayName(lastArgument);
                sender.sendMessage(format("<yellow>Display name was successfully changed!</yellow>"));
            }
            case "author" -> {
                targetQuest.setAuthor(lastArgument);
                sender.sendMessage(format("<yellow>Author was successfully changed!</yellow>"));
            }
            case "difficulty" -> {
                targetQuest.setDifficulty(lastArgument);
                sender.sendMessage(format("<yellow>Difficulty was successfully changed!</yellow>"));
            }
            case "description" -> {
                targetQuest.setDescription(lastArgument);
                sender.sendMessage(format("<yellow>Description was successfully changed!</yellow>"));
            }
            case "itemstack" -> {
                if (!(sender instanceof Player player))
                    return notPlayer(sender);
                ItemStack stack = player.getInventory().getItem(player.getActiveItemHand());
                ItemStack renderStack = stack.clone();
                targetQuest.setDisplayStack(renderStack);
                sender.sendMessage(format("<yellow>Itemstack was successfully changed!</yellow>"));
            }
            case "pages" -> {
                if (!(sender instanceof Player player))
                    return notPlayer(sender);

                ItemStack book = targetQuest.writableBook();
                if (book == null || !(book.getItemMeta() instanceof BookMeta bookMeta)) {
                    sender.sendMessage(format("<red>Error: Could not generate an editable book for this quest!</red>"));
                    return true;
                }

                Map<Integer, ItemStack> added = player.getInventory().addItem(book);
                if (!added.isEmpty()) {
                    sender.sendMessage(format("<red>Error: Please make space in your inventory to receive an editable book for this quest!</red>"));
                    return true;
                }

                sender.sendMessage(format("<yellow>A book and quill has been added to your inventory.</yellow>"));
                sender.sendMessage(format("<yellow>Editing this book will update the quest contents.</yellow>"));
                sender.sendMessage(format("<yellow>Signing the book will finalise this edit session and remove the book.</yellow>"));
                questBookUpdatedEvent.getDistributedBooks().put(bookMeta, targetQuest);
                return true;
            }
            default -> {
                sender.sendMessage(format("<yellow>Usage: /questbook edit <questname> <displayName | author | difficulty | itemstack | pages> [value]</yellow>"));
                return true;
            }
        }

        questProvider.saveQuests();
        return true;
    }

    /**
     * Utility function to handle creating a quest
     * @param sender The command sender
     * @param args Any args passed to the create command
     * @return Whether the command call was handled
     */
    private boolean callCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERM_QUESTBOOK_CREATE))
            return noPermission(sender);

        if (!(sender instanceof Player player))
            return notPlayer(sender);

        if (args.length < 2) {
            sender.sendMessage(format("<yellow>Usage: /questbook create <questname></yellow>"));
            return true;
        }

        String questName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String dirtyName = questName.replace(" ", "_");
        if (dirtyName.length() > 48) {
            sender.sendMessage(format("<red>Error: Name cannot be more than 84 characters in length!</red>"));
            return true;
        }

        Optional<Quest> foundQuest = questProvider.getQuests()
                .stream()
                .filter(quest -> dirtyName.equalsIgnoreCase(quest.getName()))
                .findFirst();

        if (foundQuest.isPresent()) {
            sender.sendMessage(format("<red>Error: A quest with the name <gray>%s</gray> already exists!</red>", foundQuest.get().getName()));
            return true;
        }

        ItemStack stack = player.getInventory().getItem(player.getActiveItemHand());
        ItemStack renderStack = stack.clone();

        Quest quest = new Quest();
        quest.setAuthor(sender.getName());
        quest.setName(dirtyName);
        quest.setDisplayName(questName);
        quest.setDifficulty("<red>Hard</red>");
        quest.setDisplayStack(renderStack);
        quest.setPages(List.of("This is the default book contents. You can edit this to provide an appropriate description as you wish!"));

        questProvider.registerQuest(quest);

        sender.sendMessage(format("<yellow>Successfully created a new quest with the name</yellow> <gray>%s</gray><yellow>!</yellow>", dirtyName));
        return true;
    }

    /**
     * Utility function to handle deleting a quest
     * @param sender The command sender
     * @param args Any args passed to the delete command
     * @return Whether the command call was handled
     */
    private boolean callDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERM_QUESTBOOK_DELETE))
            return noPermission(sender);

        if (args.length < 2) {
            sender.sendMessage(format("<yellow>Usage: /questbook delete <questname></yellow>"));
            return true;
        }

        String questName = args[1];
        questProvider.getQuests()
                .stream()
                .filter(quest -> questName.equalsIgnoreCase(quest.getName()))
                .findFirst()
                .ifPresent(quest -> {
                    sender.sendMessage(format("<yellow>Successfully deleted the quest </yellow><gray>%s</gray><yellow>!</yellow>", quest.getName()));
                    questProvider.unregisterQuest(quest);
                });
        return true;
    }

}
