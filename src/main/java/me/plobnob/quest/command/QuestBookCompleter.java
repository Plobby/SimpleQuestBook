package me.plobnob.quest.command;

import lombok.RequiredArgsConstructor;
import me.plobnob.quest.provider.Quest;
import me.plobnob.quest.provider.QuestProvider;
import me.plobnob.quest.validation.PermissionValidator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static java.util.Collections.emptyList;
import static me.plobnob.quest.util.PluginConstants.PERM_QUESTBOOK;

/**
 * An autocomplete handler class for the questbook command.
 */
@RequiredArgsConstructor
public class QuestBookCompleter implements TabCompleter, PermissionValidator {

    // Quest provider (set by lombok)
    private final QuestProvider questProvider;

    /**
     *
     * Handler for the questbook command
     * @param sender The command sender
     * @param cmd The command used
     * @param alias The alias used
     * @param args Any args passed to the command
     * @return A list of all available autocomplete items
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission(PERM_QUESTBOOK))
            return emptyList();

        if (args.length == 1) {
            return List.of("open", "edit", "create", "delete");
        }

        return switch (args[0].toLowerCase()) {
            case "open", "create" -> emptyList();
            case "edit" -> {
                if (args.length == 2)
                    yield questProvider.getQuests().stream().map(Quest::getName).toList();
                if (args.length == 3)
                    yield List.of("displayName", "author", "difficulty", "description", "itemstack", "pages");
                yield emptyList();
            }
            case "delete" -> {
                if (args.length == 2)
                    yield questProvider.getQuests().stream().map(Quest::getName).toList();
                yield emptyList();
            }
            default -> emptyList();
        };
    }
}
