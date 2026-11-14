package io.github.brainage04.brainageserverutils.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;

/// Clients do not need this mod, so an unmodified client has none of its keys. Every message
/// therefore carries the English text from the mod's own `en_us.json` as its fallback, which the
/// component codec sends along (`TranslatableContents.java:41`) and the client shows when the key
/// is missing (`TranslatableContents.java:90`). A client with the mod installed uses its language.
public final class Translations {
    static final String ENGLISH_FILE = "/assets/brainageserverutils/lang/en_us.json";
    private static final Map<String, String> ENGLISH = loadEnglish();

    private Translations() {}

    public static MutableComponent text(String key, Object... args) {
        return Component.translatableWithFallback(key, english(key), args);
    }

    /// Unlike `ComponentUtils.formatList`'s default gray separator (`ComponentUtils.java:19`), the
    /// commas take the style of the message they are part of.
    public static Component joined(Collection<? extends Component> parts) {
        return ComponentUtils.formatList(parts, ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR);
    }

    /// The English text with the arguments filled in, for messages that are logged as well as
    /// shown.
    public static String englishText(String key, Object... args) {
        return String.format(Locale.ROOT, english(key), args);
    }

    static String english(String key) {
        String english = ENGLISH.get(key);
        if (english == null) {
            throw new IllegalArgumentException(
                    "No English text for " + key + " in " + ENGLISH_FILE);
        }
        return english;
    }

    private static Map<String, String> loadEnglish() {
        try (InputStream stream = Translations.class.getResourceAsStream(ENGLISH_FILE)) {
            if (stream == null) {
                throw new IllegalStateException(ENGLISH_FILE + " is missing from the mod");
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return Map.copyOf(
                        new Gson().fromJson(reader, new TypeToken<Map<String, String>>() {}));
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
