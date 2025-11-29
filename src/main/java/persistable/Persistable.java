package persistable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import suite.Constants;
import utility.ConfigBuilder;
import utility.FileUtilities;
import utility.JsonLoader;

public class Persistable implements Serializable {

    private static final long serialVersionUID = -7227610820978123662L;

    public boolean debug;
    public String cacheDir;
    public boolean focusOnSearch;
    public boolean notifications;
    public int revision;

    private ConfigBuilder builder = new ConfigBuilder(this.getClass());

    public Persistable load() {
        Persistable instance = new Persistable();
        JsonLoader loader = new SaveLoader(instance);
        File file = new File(loader.filePath());
        System.out.println("Loading persistable");
        if (!file.exists()) {
            try {
                if (file.getParentFile().mkdirs()) {
                    file.createNewFile();
                    instance.save();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loader.load();
        return instance;
    }

    public void save() {
        System.out.println("Saving persistable");
        builder.add("debug", debug);
        builder.add("cache-dir", cacheDir == null || cacheDir.isEmpty() ? "" : cacheDir);
        builder.add("focus-search", focusOnSearch);
        builder.add("revision", revision);
        builder.add("notifications", notifications);
        builder.save();
    }

    public void delete() {
        FileUtilities.delete(Persistable.class.getName() + ".json");
    }

    /**
     * Static inner class so it doesn't implicitly hold the outer-instance.
     * Holds a reference to the Persistable we want to populate.
     */
    static class SaveLoader extends JsonLoader {

        private final Persistable persistable;

        public SaveLoader(Persistable persistable) {
            this.persistable = persistable;
        }

        @Override
        public void load(JsonObject reader, Gson builder) {
            if (reader.has("debug")) {
                persistable.debug = reader.get("debug").getAsBoolean();
            }
            if (reader.has("cache-dir")) {
                persistable.cacheDir = reader.get("cache-dir").getAsString();
            }
            if (reader.has("focus-search")) {
                persistable.focusOnSearch = reader.get("focus-search").getAsBoolean();
            }
            if (reader.has("revision")) {
                persistable.revision = reader.get("revision").getAsInt();
            }
            if (reader.has("notifications")) {
                persistable.notifications = reader.get("notifications").getAsBoolean();
            }
        }

        @Override
        public String filePath() {
            return Constants.DEFAULT_SAVE_DIR + File.separator + Persistable.class.getSimpleName() + ".json";
        }

        @Override
        public JsonLoader load() {
            try (FileReader fileReader = new FileReader(Paths.get(filePath()).toFile())) {
                JsonParser parser = new JsonParser();
                JsonObject element = (JsonObject) parser.parse(fileReader);
                Gson gson = new GsonBuilder().create();
                load(element, gson);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }
    }
}
