package dev.sgtpyr.notebook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @TempDir
    Path tempDir;

    private Path createNotebook() throws Exception {
        Path file = tempDir.resolve("stories.json");
        Files.writeString(file, """
            [
              {"title":"First Story","body":"A story about friendship."},
              {"title":"Second Story","body":"Learning and growing."}
            ]
            """, StandardCharsets.UTF_8);
        return file;
    }

    private String captureOutput(String... args) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (PrintStream replacement = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(replacement);
            Main.run(args);
        } finally {
            System.setOut(original);
        }

        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    void listsStories() throws Exception {
        Path file = createNotebook();
        String output = captureOutput("list", "--file", file.toString(), "1");

        assertTrue(output.contains("First Story"));
        assertFalse(output.contains("Second Story"));
        assertTrue(output.contains("Showing 1 of 2 stories."));
    }

    @Test
    void searchesStoryTitlesAndBodies() throws Exception {
        Path file = createNotebook();
        String output = captureOutput(
            "search", "--file", file.toString(), "friendship"
        );

        assertTrue(output.contains("First Story"));
        assertFalse(output.contains("Second Story"));
        assertTrue(output.contains("1 matching stories."));
    }

    @Test
    void showsSelectedStory() throws Exception {
        Path file = createNotebook();
        String output = captureOutput(
            "show", "--file", file.toString(), "2"
        );

        assertTrue(output.contains("Second Story"));
        assertTrue(output.contains("Learning and growing."));
    }

    @Test
    void validatesNotebook() throws Exception {
        Path file = createNotebook();
        String output = captureOutput(
            "validate", "--file", file.toString()
        );

        assertTrue(output.contains("Validation passed. Stories checked: 2"));
    }

    @Test
    void exportsStoriesWithoutChangingSource() throws Exception {
        Path file = createNotebook();
        String original = Files.readString(file);
        Path export = tempDir.resolve("export.json");

        String output = captureOutput(
            "export", "--file", file.toString(), export.toString()
        );

        assertTrue(output.contains("Exported 2 stories"));
        assertTrue(Files.exists(export));
        assertEquals(original, Files.readString(file));
        assertTrue(Files.readString(export).contains("First Story"));
    }

    @Test
    void rejectsUnknownCommands() {
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> Main.run(new String[]{"unknown"})
        );

        assertTrue(error.getMessage().contains("Unknown command"));
    }

    @Test
    void rejectsMissingStoryNumbers() throws Exception {
        Path file = createNotebook();

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> Main.run(new String[]{
                "show", "--file", file.toString(), "99"
            })
        );

        assertTrue(error.getMessage().contains("out of range"));
    }
}
