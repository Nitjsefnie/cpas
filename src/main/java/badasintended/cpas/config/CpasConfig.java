package badasintended.cpas.config;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import badasintended.cpas.Cpas;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class CpasConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(Cpas.MOD_ID + ".json");

    public static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Identifier.class, new Identifier.Serializer())
        .registerTypeAdapter(Entry.class, new Entry.Serializer())
        .create();

    private static CpasConfig config = null;

    public static CpasConfig get() {
        return get(false);
    }

    public static CpasConfig get(boolean reload) {
        if (config == null || reload) {
            try {
                if (Files.notExists(CONFIG_PATH)) {
                    config = new CpasConfig();
                    Files.write(CONFIG_PATH, GSON.toJson(config).getBytes());
                } else {
                    config = GSON.fromJson(String.join("\n", Files.readAllLines(CONFIG_PATH)), CpasConfig.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new JsonParseException(new IllegalArgumentException());
            }
        }
        return config;
    }

    public static void save() {
        try {
            Files.write(CONFIG_PATH, GSON.toJson(get()).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Entry getEntry(ScreenHandlerType<?> type) {
        Identifier id = Registry.SCREEN_HANDLER.getId(type);
        CpasConfig config = get();
        if (!config.entries.containsKey(id)) {
            config.entries.put(id, new Entry());
            save();
        }
        return config.entries.get(id);
    }

    private boolean showHelp = true;

    private Map<Identifier, Entry> entries = new HashMap<>();

    public void setEntries(Map<Identifier, Entry> entries) {
        this.entries = entries;
    }

    public Map<Identifier, Entry> getEntries() {
        return entries;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public void setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
    }

    public static class Entry {

        private boolean enabled = true;
        private boolean auto = true;
        private int x = 0;
        private int y = 0;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAuto() {
            return auto;
        }

        public void setAuto(boolean auto) {
            this.auto = auto;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public static class Serializer implements JsonDeserializer<Entry>, JsonSerializer<Entry> {

            @Override
            public Entry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                Entry result = new Entry();

                if (json.isJsonPrimitive()) {
                    boolean b = json.getAsBoolean();
                    result.enabled = result.auto = b;
                } else {
                    JsonArray array = json.getAsJsonArray();
                    if (array.size() != 2) {
                        throw new JsonParseException("invalid array size of " + array.size() + ", 2 is required", new IllegalArgumentException());
                    }
                    result.enabled = true;
                    result.auto = false;
                    result.x = array.get(0).getAsInt();
                    result.y = array.get(1).getAsInt();
                }
                return result;
            }

            @Override
            public JsonElement serialize(Entry src, Type typeOfSrc, JsonSerializationContext context) {
                if (!src.enabled) {
                    return new JsonPrimitive(false);
                } else if (src.auto) {
                    return new JsonPrimitive(true);
                } else {
                    JsonArray array = new JsonArray();
                    array.add(src.x);
                    array.add(src.y);
                    return array;
                }
            }

        }

    }

}
