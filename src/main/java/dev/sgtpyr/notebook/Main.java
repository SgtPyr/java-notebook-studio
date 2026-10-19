package dev.sgtpyr.notebook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Main {
    private static final Path DEFAULT_FILE = Path.of(
        "/storage/emulated/0/vide coding project/for you/stories/stories.json"
    );

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    private static final Type STORY_LIST_TYPE =
        new TypeToken<List<Story>>() {}.getType();

    private Main() {}

    public static void main(String[] args) {
        try {
            run(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (IOException | JsonParseException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    static void run(String[] args) throws IOException {
        if (args.length == 0 || args[0].equals("help")) {
            printHelp();
            return;
        }

        String command = args[0].toLowerCase(Locale.ROOT);
        Path file = DEFAULT_FILE;
        int start = 1;

        if (args.length >= 3 && args[1].equals("--file")) {
            file = Path.of(args[2]);
            start = 3;
        }

        switch (command) {
            case "list" -> {
                List<Story> stories = load(file);
                int limit = start < args.length ? positiveInt(args[start], "limit") : 10;
                for (int i = 0; i < Math.min(limit, stories.size()); i++) {
                    System.out.printf("%d. %s%n", i + 1, stories.get(i).title);
                }
                System.out.printf("Showing %d of %d stories.%n",
                    Math.min(limit, stories.size()), stories.size());
            }
            case "search" -> {
                if (start >= args.length) {
                    throw new IllegalArgumentException("Usage: search <query>");
                }
                String query = String.join(" ", slice(args, start)).toLowerCase(Locale.ROOT);
                List<Story> stories = load(file);
                int found = 0;
                for (int i = 0; i < stories.size(); i++) {
                    Story story = stories.get(i);
                    if (contains(story.title, query) || contains(story.body, query)) {
                        System.out.printf("%d. %s%n", i + 1, story.title);
                        found++;
                    }
                }
                System.out.printf("%d matching stories.%n", found);
            }
            case "show" -> {
                if (start >= args.length) {
                    throw new IllegalArgumentException("Usage: show <number>");
                }
                int number = positiveInt(args[start], "story number");
                List<Story> stories = load(file);
                if (number > stories.size()) {
                    throw new IllegalArgumentException("Story number out of range: " + number);
                }
                Story story = stories.get(number - 1);
                System.out.println(story.title);
                System.out.println("=".repeat(Math.max(1, story.title.length())));
                System.out.println(story.body);
            }
            case "validate" -> {
                List<Story> stories = load(file);
                for (int i = 0; i < stories.size(); i++) {
                    Story story = stories.get(i);
                    if (blank(story.title) || blank(story.body)) {
                        throw new IllegalArgumentException(
                            "Story " + (i + 1) + " has an empty title or body."
                        );
                    }
                }
                System.out.printf("Validation passed. Stories checked: %d%n", stories.size());
            }
            case "export" -> {
                if (start >= args.length) {
                    throw new IllegalArgumentException("Usage: export <output.json>");
                }
                Path output = Path.of(args[start]).toAbsolutePath();
                List<Story> stories = load(file);
                if (output.normalize().equals(file.toAbsolutePath().normalize())) {
                    throw new IllegalArgumentException("Export path must differ from the source file.");
                }
                if (output.getParent() != null) {
                    Files.createDirectories(output.getParent());
                }
                Files.writeString(output, GSON.toJson(stories) + System.lineSeparator(),
                    StandardCharsets.UTF_8);
                System.out.printf("Exported %d stories to %s%n", stories.size(), output);
            }
            default -> throw new IllegalArgumentException("Unknown command: " + command);
        }
    }

    private static List<Story> load(Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Notebook file not found: " + file);
        }
        String json = Files.readString(file, StandardCharsets.UTF_8);
        List<Story> stories = GSON.fromJson(json, STORY_LIST_TYPE);
        if (stories == null) {
            throw new IllegalArgumentException("Notebook JSON is empty or null.");
        }
        for (int i = 0; i < stories.size(); i++) {
            if (stories.get(i) == null) {
                throw new IllegalArgumentException("Story " + (i + 1) + " is null.");
            }
            if (stories.get(i).title == null || stories.get(i).body == null) {
                throw new IllegalArgumentException(
                    "Story " + (i + 1) + " must contain title and body fields."
                );
            }
        }
        return stories;
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static int positiveInt(String value, String label) {
        try {
            int number = Integer.parseInt(value);
            if (number < 1) {
                throw new NumberFormatException();
            }
            return number;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a positive integer.");
        }
    }

    private static String[] slice(String[] values, int start) {
        return java.util.Arrays.copyOfRange(values, start, values.length);
    }

    private static void printHelp() {
        System.out.println("""
            Java Notebook Studio

            Commands:
              list [limit]          List stories (default: 10)
              search <query>        Search titles and bodies
              show <number>         Display one story
              validate              Validate notebook structure and content
              export <output.json>  Export stories to another JSON file
              help                  Show this help

            Optional source override:
              --file <path>         Read from a different notebook JSON file

            Examples:
              gradle run --args="list"
              gradle run --args="search friendship"
              gradle run --args="show 1"
              gradle run --args="validate"
            """);
    }

    static final class Story {
        String title;
        String body;
    }
}
