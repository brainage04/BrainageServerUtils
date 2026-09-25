package io.github.brainage04.brainageserverutils.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.brainage04.brainageserverutils.BrainageServerUtils;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.Identifier;

/// Ranked enchantment preferences used by `/maxenchant` and `/kit` to decide between mutually exclusive enchantments.
///
/// Each ranking lists enchantments from most to least preferred. The file is read on every use, so edits apply
/// without a restart.
public final class EnchantmentRankings {
    public static final String FILE_NAME = "brainageserverutils.json";
    private static final String RANKINGS_KEY = "enchantment_rankings";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final List<List<String>> DEFAULT_RANKINGS = List.of(
            List.of("minecraft:sharpness", "minecraft:density", "minecraft:breach", "minecraft:smite",
                    "minecraft:bane_of_arthropods", "minecraft:impaling"),
            List.of("minecraft:protection", "minecraft:blast_protection", "minecraft:fire_protection",
                    "minecraft:projectile_protection"),
            List.of("minecraft:infinity", "minecraft:mending"),
            List.of("minecraft:fortune", "minecraft:silk_touch"),
            List.of("minecraft:multishot", "minecraft:piercing"),
            List.of("minecraft:depth_strider", "minecraft:frost_walker"),
            List.of("minecraft:loyalty", "minecraft:channeling", "minecraft:riptide")
    );

    private static Path path;

    private EnchantmentRankings() {
    }

    public static void initialize(Path configDirectory) {
        path = configDirectory.resolve(FILE_NAME);
        if (!Files.exists(path)) {
            writeDefaults();
        }
    }

    public static Path path() {
        return path;
    }

    public static List<List<Identifier>> defaults() {
        return DEFAULT_RANKINGS.stream()
                .map(ranking -> ranking.stream().map(Identifier::parse).toList())
                .toList();
    }

    /// Reads the rankings from disk, recreating the default file when it is missing.
    public static List<List<Identifier>> load() throws InvalidConfigException {
        if (!Files.exists(path)) {
            writeDefaults();
            return defaults();
        }

        JsonElement document;
        try (Reader reader = Files.newBufferedReader(path)) {
            document = JsonParser.parseReader(reader);
        } catch (IOException | JsonParseException exception) {
            throw new InvalidConfigException("could not be read: " + exception.getMessage());
        }
        return parse(document);
    }

    private static List<List<Identifier>> parse(JsonElement document) throws InvalidConfigException {
        if (!(document instanceof JsonObject object) || !(object.get(RANKINGS_KEY) instanceof JsonArray rankingsJson)) {
            throw new InvalidConfigException("must be an object with an \"" + RANKINGS_KEY + "\" array");
        }

        List<List<Identifier>> rankings = new ArrayList<>();
        Set<Identifier> seen = new HashSet<>();
        for (JsonElement rankingJson : rankingsJson) {
            if (!(rankingJson instanceof JsonArray entries)) {
                throw new InvalidConfigException("each ranking must be an array of enchantment IDs");
            }
            List<Identifier> ranking = new ArrayList<>();
            for (JsonElement entry : entries) {
                Identifier id = entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()
                        ? Identifier.tryParse(entry.getAsString())
                        : null;
                if (id == null) {
                    throw new InvalidConfigException("contains an invalid enchantment ID: " + entry);
                }
                if (!seen.add(id)) {
                    throw new InvalidConfigException("lists " + id + " in more than one place");
                }
                ranking.add(id);
            }
            rankings.add(List.copyOf(ranking));
        }
        return List.copyOf(rankings);
    }

    private static void writeDefaults() {
        JsonObject document = new JsonObject();
        document.add(RANKINGS_KEY, GSON.toJsonTree(DEFAULT_RANKINGS));
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(document, writer);
            }
        } catch (IOException exception) {
            BrainageServerUtils.LOGGER.error("Failed to write default enchantment rankings to {}", path, exception);
        }
    }

    public static final class InvalidConfigException extends Exception {
        public InvalidConfigException(String problem) {
            super(FILE_NAME + " " + problem);
        }
    }
}
