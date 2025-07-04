package me.plobnob.quest.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.davidmoten.text.utils.WordWrap;
import org.jetbrains.annotations.NotNull;

import java.util.IllegalFormatException;
import java.util.List;

/**
 * Utility interface to allow simplistic text formatting.
 */
public interface TextFormatter {

    default @NotNull Component format(String text, Object... params) {
        try {
            text = String.format(text, params);
        } catch (IllegalFormatException ex) {
            System.err.println("Invalid format was passed to questbook contents - this may not render correctly!");
        }
        return MiniMessage.miniMessage().deserialize(text);
    }

    default List<Component> wrappedFormat(String text, int lineLength, Object... params) {
        try {
            text = String.format(text, params);
        } catch (IllegalFormatException ex) {
            System.err.println("Invalid format was passed to questbook contents - this may not render correctly!");
        }

        return WordWrap
                .from(text)
                .maxWidth(lineLength)
                .breakWords(false)
                .wrapToList()
                .stream()
                .map(line -> MiniMessage.miniMessage().deserialize(line))
                .toList();
    }

}
