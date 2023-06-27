package com.spanner.cestom;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.listener.TabCompleteListener;
import org.fusesource.jansi.AnsiConsole;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.tinylog.core.LogEntry;
import org.tinylog.writers.AbstractFormatPatternWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CestomTerminalWriter extends AbstractFormatPatternWriter {
    private static final String PROMPT = "> ";
    private static Terminal terminal;
    private static LineReader reader;
    private static boolean running = false;

    public CestomTerminalWriter(Map<String, String> properties) {
        super(properties);
    }

    public static void start() {
        final Thread thread = new Thread(null, () -> {
            try {
                terminal = TerminalBuilder.terminal();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader = LineReaderBuilder.builder()
                    .completer(new MinestomCompleter())
                    .terminal(terminal)
                    .build();
            running = true;

            while (running) {
                String command;
                try {
                    command = reader.readLine(PROMPT);
                    var commandManager = MinecraftServer.getCommandManager();
                    commandManager.execute(commandManager.getConsoleSender(), command);
                } catch (UserInterruptException e) {
                    // Handle Ctrl + C
                    System.exit(0);
                    return;
                } catch (EndOfFileException e) {
                    return;
                }
            }
        }, "Jline");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void write(LogEntry logEntry) throws Exception {
        String rendered = render(logEntry);
        String formatted = TerminalColorConverter.format(rendered);
        if (reader != null) {
            reader.printAbove(formatted);
        } else {
            AnsiConsole.out().print(formatted);
        }
    }

    @Override
    public void flush() throws Exception {

    }

    @Override
    public void close() throws Exception {

    }

    // Borrowed from Minestom (thank you)
    //     Minestom/src/main/java/net/minestom/server/terminal/MinestomTerminal.java
    private static final class MinestomCompleter implements Completer {
        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            final var commandManager = MinecraftServer.getCommandManager();
            final var consoleSender = commandManager.getConsoleSender();
            if (line.wordIndex() == 0) {
                final String commandString = line.word().toLowerCase();
                candidates.addAll(
                        commandManager.getDispatcher().getCommands().stream()
                                .map(Command::getName)
                                .filter(name -> commandString.isBlank() || name.toLowerCase().startsWith(commandString))
                                .map(Candidate::new)
                                .toList()
                );
            } else {
                final String text = line.line();
                final Suggestion suggestion = TabCompleteListener.getSuggestion(consoleSender, text);
                if (suggestion != null) {
                    suggestion.getEntries().stream()
                            .map(SuggestionEntry::getEntry)
                            .map(Candidate::new)
                            .forEach(candidates::add);
                }
            }
        }
    }

    // Borrowed from Minestom (thank you)
    //     Minestom/src/main/java/net/minestom/server/terminal/TerminalColorConverter.java
    final class TerminalColorConverter {
        private static final boolean SUPPORT_HEX_COLOR = true;
        private static final boolean SUPPORT_COLOR = true;

        private static final String RGB_ANSI = "\u001B[38;2;%d;%d;%dm";
        private static final String ANSI_RESET = "\u001B[m";
        private static final String LOOKUP = "0123456789abcdefklmnor";
        private static final String[] ANSI_CODES = new String[]{
                getAnsiColor(NamedTextColor.BLACK, "\u001B[0;30m"), // Black §0
                getAnsiColor(NamedTextColor.DARK_BLUE, "\u001B[0;34m"), // Dark Blue §1
                getAnsiColor(NamedTextColor.DARK_GREEN, "\u001B[0;32m"), // Dark Green §2
                getAnsiColor(NamedTextColor.DARK_AQUA, "\u001B[0;36m"), // Dark Aqua §3
                getAnsiColor(NamedTextColor.DARK_RED, "\u001B[0;31m"), // Dark Red §4
                getAnsiColor(NamedTextColor.DARK_PURPLE, "\u001B[0;35m"), // Dark Purple §5
                getAnsiColor(NamedTextColor.GOLD, "\u001B[0;33m"), // Gold §6
                getAnsiColor(NamedTextColor.GRAY, "\u001B[0;37m"), // Gray §7
                getAnsiColor(NamedTextColor.DARK_GRAY, "\u001B[0;30;1m"),  // Dark Gray §8
                getAnsiColor(NamedTextColor.BLUE, "\u001B[0;34;1m"),  // Blue §9
                getAnsiColor(NamedTextColor.GREEN, "\u001B[0;32;1m"),  // Green §a
                getAnsiColor(NamedTextColor.AQUA, "\u001B[0;36;1m"),  // Aqua §b
                getAnsiColor(NamedTextColor.RED, "\u001B[0;31;1m"),  // Red §c
                getAnsiColor(NamedTextColor.LIGHT_PURPLE, "\u001B[0;35;1m"),  // Light Purple §d
                getAnsiColor(NamedTextColor.YELLOW, "\u001B[0;33;1m"),  // Yellow §e
                getAnsiColor(NamedTextColor.WHITE, "\u001B[0;37;1m"),  // White §f
                "\u001B[5m", // Obfuscated §k
                "\u001B[1m", // Bold §l
                "\u001B[9m", // Strikethrough §m
                "\u001B[4m", // Underline §n
                "\u001B[3m", // Italic §o
                ANSI_RESET, // Reset §r
        };
        private static final Pattern RGB_PATTERN = Pattern.compile(LegacyComponentSerializer.SECTION_CHAR + "#([\\da-fA-F]{6})");
        private static final Pattern NAMED_PATTERN = Pattern.compile(LegacyComponentSerializer.SECTION_CHAR + "([\\da-fk-orA-FK-OR])");

        private TerminalColorConverter() {
        }

        private static String getAnsiColor(NamedTextColor color, String fallback) {
            return getAnsiColorFromHexColor(color.value(), fallback);
        }

        private static String getAnsiColorFromHexColor(int color, String fallback) {
            return SUPPORT_HEX_COLOR ? String.format(RGB_ANSI, (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF) : fallback;
        }

        private static String getAnsiColorFromHexColor(int color) {
            return getAnsiColorFromHexColor(color, "");
        }

        /**
         * Format the colored string to an ansi-colored one.
         *
         * @param string the string to format
         * @return the formatted string
         */
        public static String format(String string) {
            if (string.indexOf(LegacyComponentSerializer.SECTION_CHAR) == -1) {
                return string;
            }

            string = RGB_PATTERN.matcher(string).replaceAll(match -> {
                if (SUPPORT_COLOR) {
                    String hex = match.group(1);
                    return getAnsiColorFromHexColor(Integer.parseInt(hex, 16));
                } else {
                    return "";
                }
            });

            Matcher matcher = NAMED_PATTERN.matcher(string);
            StringBuilder builder = new StringBuilder();
            while (matcher.find()) {
                int format = LOOKUP.indexOf(Character.toLowerCase(matcher.group().charAt(1)));
                if (format != -1) {
                    matcher.appendReplacement(builder, SUPPORT_COLOR ? ANSI_CODES[format] : "");
                } else {
                    matcher.appendReplacement(builder, matcher.group());
                }
            }
            matcher.appendTail(builder);

            if (SUPPORT_COLOR) {
                builder.append(ANSI_RESET);
            }
            return builder.toString();
        }
    }

}
