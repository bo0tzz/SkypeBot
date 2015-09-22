package io.mazenmc.skypebot.utils;

import com.skype.ChatMessage;
import com.skype.SkypeException;
import io.mazenmc.skypebot.SkypeBot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Resource {

    public static final String COMMAND_PREFIX = "@";
    public static final String[] GROUP_ADMINS = {"stuntguy3000", "heycallmethemezand", "itssircuddles", "boet.de.willigen"};
    public static final String KEY_GITHUB = Utils.readFirstLine("key_github");
    public static final String KEY_TRIDENT = Utils.readFirstLine("key_trident");
    public static final String KEY_URBAND = Utils.readFirstLine("key_ud");
    public static final String REGEX_ALL = "(.+)";
    public static final String REGEX_DOUBLE = "(-?[0-9]+\\.[0-9]+)";
    public static final String REGEX_INT = "(-?[0-9]+)";
    public static final String REGEX_WORD = "(\\b+)";
    public static final String VERSION = "1.7";
    public static final Pattern SPOTIFY_HTTP_REGEX = Pattern.compile("open\\.spotify\\.com/track/([A-z0-9]){22}");
    public static final Pattern SPOTIFY_URI_REGEX = Pattern.compile("spotify:track:([A-z0-9]){22}");
    public static final Pattern TWITTER_REGEX = Pattern.compile("twitter\\.com\\/([A-z0-9]+)\\/status\\/([0-9]{18})");
    public static final Pattern WIKIPEDIA_REGEX = Pattern.compile("en\\.wikipedia\\.(?:org|com)\\/wiki\\/(.+)");

    private static Map<String, Callback<String>> callbacks = new HashMap<>();

    public static void sendMessage(String message) {
        SkypeBot.getInstance().sendMessage(message);
    }

    public static void sendMessage(ChatMessage chatMessage, String message) {
        try {
            SkypeBot.getInstance().sendMessage("(" + chatMessage.getSenderDisplayName().replaceAll("[^A-Za-z0-9 ><.»«]", "") + ") " + message);
        } catch (SkypeException ex) {
            sendMessage("Error occurred! " + ex.getMessage());
        }
    }

    public static void sendMessages(String... message) {
        StringBuilder sb = new StringBuilder();

        for (String s : message) {
            sb.append(s)
                    .append('\n');
        }

        sendMessage(sb.toString());
    }

    public static void assignCallback(String id, Callback<String> callback) {
        callbacks.put(id, callback);
    }

    public static Callback<String> getCallback(String name) {
        return callbacks.get(name);
    }
}
