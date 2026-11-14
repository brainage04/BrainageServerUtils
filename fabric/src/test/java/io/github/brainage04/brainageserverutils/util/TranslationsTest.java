package io.github.brainage04.brainageserverutils.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class TranslationsTest {
    private static final Path ROOT = Path.of("..");
    private static final List<Path> SOURCES =
            List.of(
                    ROOT.resolve("common/src/main/java"),
                    ROOT.resolve("fabric/src/main/java"),
                    ROOT.resolve("neoforge/src/main/java"));
    private static final Path ENGLISH =
            ROOT.resolve("common/src/main/resources" + Translations.ENGLISH_FILE);
    private static final Pattern KEY_LITERAL =
            Pattern.compile("\"([a-z_]+\\.brainageserverutils\\.[a-z0-9_.]+)\"");
    private static final Pattern HARD_CODED_TEXT =
            Pattern.compile("Component\\.literal\\(|\\.append\\(\\s*\"");

    @Test
    void everyKeyTheCodeUsesHasEnglishText() throws IOException {
        Set<String> used = usedKeys();
        assertFalse(used.isEmpty(), "Expected the sources to use translation keys");
        Set<String> missing = new TreeSet<>(used);
        missing.removeAll(englishKeys());
        assertEquals(Set.of(), missing, "Keys used in code but missing from " + ENGLISH);
    }

    @Test
    void everyEnglishTextIsUsed() throws IOException {
        Set<String> unused = new TreeSet<>(englishKeys());
        // The game builds game rule keys from the rule identifier (GameRule.java:64).
        unused.removeIf(key -> key.startsWith("gamerule."));
        unused.removeAll(usedKeys());
        assertEquals(Set.of(), unused, "Keys in " + ENGLISH + " that no code uses");
    }

    @Test
    void repliesAreNotHardCoded() throws IOException {
        Map<Path, String> offenders = new TreeMap<>();
        for (Path source : javaSources()) {
            Matcher matcher = HARD_CODED_TEXT.matcher(Files.readString(source));
            if (matcher.find()) {
                offenders.put(source, matcher.group());
            }
        }
        assertEquals(Map.of(), offenders, "Player-facing text must come from " + ENGLISH);
    }

    @Test
    void englishTextFillsInArguments() {
        assertEquals(
                "Healed 3 players",
                Translations.englishText("commands.brainageserverutils.heal.success.multiple", 3));
    }

    @Test
    void unknownKeyIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Translations.english("commands.brainageserverutils.missing"));
    }

    private static Set<String> usedKeys() throws IOException {
        Set<String> keys = new TreeSet<>();
        for (Path source : javaSources()) {
            Matcher matcher = KEY_LITERAL.matcher(Files.readString(source));
            while (matcher.find()) {
                keys.add(matcher.group(1));
            }
        }
        return keys;
    }

    private static Set<String> englishKeys() throws IOException {
        try (Reader reader = Files.newBufferedReader(ENGLISH)) {
            Map<String, String> english =
                    new Gson().fromJson(reader, new TypeToken<Map<String, String>>() {});
            return english.keySet();
        }
    }

    private static List<Path> javaSources() throws IOException {
        List<Path> sources = new ArrayList<>();
        for (Path root : SOURCES) {
            assertTrue(Files.isDirectory(root), "Expected sources in " + root.toAbsolutePath());
            try (Stream<Path> files = Files.walk(root)) {
                files.filter(path -> path.toString().endsWith(".java")).forEach(sources::add);
            }
        }
        return sources;
    }
}
