package redefenix.punicao;

import org.bukkit.ChatColor;

import java.util.List;

public final class Messages {
    private Messages() {}

    public static String get(String path, String fallback, String... replacements) {
        String message = Main.getInstance().getConfig().getString("messages." + path, fallback);
        return color(applyReplacements(message, replacements));
    }

    public static List<String> getList(String path, List<String> fallback, String... replacements) {
        List<String> messages = Main.getInstance().getConfig().getStringList("messages." + path);
        if (messages.isEmpty()) messages = fallback;

        return messages.stream()
                .map(message -> color(applyReplacements(message, replacements)))
                .toList();
    }

    private static String applyReplacements(String message, String... replacements) {
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return message;
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}

