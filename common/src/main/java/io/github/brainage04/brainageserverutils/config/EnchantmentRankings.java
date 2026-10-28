package io.github.brainage04.brainageserverutils.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import io.github.brainage04.brainagelib.storage.JsonFile;
import io.github.brainage04.brainageserverutils.BrainageServerUtils;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.Identifier;

/// The file is read on every use, so edits apply without a restart. Saving goes through BrainageLib's [JsonFile],
/// which keeps a file that failed to load: an unreadable `brainageserverutils.json` is never overwritten, and every
/// command that needs the rankings reports the problem instead.
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

    private static JsonFile<Document> file;

    private EnchantmentRankings() {
    }

    public static void initialize(Path configDirectory) {
        file = JsonFile.builder(configDirectory.resolve(FILE_NAME), GSON, Document.class)
                .decoder(EnchantmentRankings::decode)
                .description("BrainageServerUtils enchantment rankings")
                .logger(BrainageServerUtils.LOGGER)
                .build();
        switch (file.load()) {
            case JsonFile.LoadResult.Missing<Document> missing -> file.save(new Document(DEFAULT_RANKINGS));
            default -> {
            }
        }
    }

    public static Path path() {
        return file.path();
    }

    public static List<List<Identifier>> defaults() {
        return DEFAULT_RANKINGS.stream()
                .map(ranking -> ranking.stream().map(Identifier::parse).toList())
                .toList();
    }

    public static List<List<Identifier>> load() throws InvalidConfigException {
        return switch (file.load()) {
            case JsonFile.LoadResult.Loaded<Document>(Document document) ->
                    toIdentifiers(document.enchantment_rankings());
            case JsonFile.LoadResult.Missing<Document> missing -> {
                file.save(new Document(DEFAULT_RANKINGS));
                yield defaults();
            }
            case JsonFile.LoadResult.Invalid<Document> invalid ->
                    throw new InvalidConfigException(problem(invalid.exception()));
            case JsonFile.LoadResult.IoError<Document> ioError ->
                    throw new InvalidConfigException("could not be read: " + ioError.exception().getMessage());
        };
    }

    /// Rejects anything that is not a document of unique, valid enchantment IDs, keeping the strings as written.
    private static Document decode(JsonElement document) {
        if (!(document instanceof JsonObject object) || !(object.get(RANKINGS_KEY) instanceof JsonArray rankingsJson)) {
            throw new JsonParseException("must be an object with an \"" + RANKINGS_KEY + "\" array");
        }

        List<List<String>> rankings = new ArrayList<>();
        Set<Identifier> seen = new HashSet<>();
        for (JsonElement rankingJson : rankingsJson) {
            if (!(rankingJson instanceof JsonArray entries)) {
                throw new JsonParseException("each ranking must be an array of enchantment IDs");
            }
            List<String> ranking = new ArrayList<>();
            for (JsonElement entry : entries) {
                String value = entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()
                        ? entry.getAsString()
                        : null;
                Identifier id = value == null ? null : Identifier.tryParse(value);
                if (id == null) {
                    throw new JsonParseException("contains an invalid enchantment ID: " + entry);
                }
                if (!seen.add(id)) {
                    throw new JsonParseException("lists " + id + " in more than one place");
                }
                ranking.add(value);
            }
            rankings.add(List.copyOf(ranking));
        }
        return new Document(List.copyOf(rankings));
    }

    private static List<List<Identifier>> toIdentifiers(List<List<String>> rankings) {
        return rankings.stream()
                .map(ranking -> ranking.stream().map(Identifier::parse).toList())
                .toList();
    }

    private static String problem(JsonParseException exception) {
        // Gson's syntax errors are the unreadable-file case; the decoder's own messages are configuration problems.
        return exception instanceof JsonSyntaxException
                ? "could not be read: " + exception.getMessage()
                : exception.getMessage();
    }

    /// The stored document; the component name is the JSON field, so the file's format is unchanged.
    private record Document(List<List<String>> enchantment_rankings) {
    }

    public static final class InvalidConfigException extends Exception {
        public InvalidConfigException(String problem) {
            super(FILE_NAME + " " + problem);
        }
    }
}
