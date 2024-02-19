package tomokao.utilitymod;

import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class UtilityConfig {
    private static final Path
            CONFIG_DIR_PATH = FabricLoader.getInstance().getConfigDir().resolve("utilitymod"),
            MODULE_CONFIG_PATH = CONFIG_DIR_PATH.resolve("modules.json");

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Name {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Description {
        String value();
    }

    static {
        load();
    }

    private static List<ImmutablePair<String, Object>> getModuleConfigs() {
        return UtilityModules.getModules()
                .stream()
                .map(m -> new ImmutablePair<>(m.id, getModuleConfig(m)))
                .filter(t -> t.right != null)
                .toList();
    }

    private static Object getModuleConfig(UtilityModules.Module module) {
        try {
            return module.getClass().getField("config").get(module);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return null;
        }
    }

    public static void load() {
        if (Files.notExists(MODULE_CONFIG_PATH)) return;

        String modulesJson;
        try {
            modulesJson = Files.readString(MODULE_CONFIG_PATH);
        } catch (IOException e) {
            System.err.println("Could not load utility config: " + e);
            return;
        }
        var moduleConfigs = getModuleConfigs();
        var gsonBuilder = new GsonBuilder();
        moduleConfigs.forEach(pair -> {
            var config = pair.right;
            gsonBuilder.registerTypeAdapter(config.getClass(), (InstanceCreator<?>) type -> config);
        });
        var gson = gsonBuilder.create();
        var modulesJsonObject = JsonParser.parseString(modulesJson).getAsJsonObject();
        moduleConfigs.forEach(pair -> {
            var id = pair.left;
            var config = pair.right;
            assert gson.fromJson(modulesJsonObject.get(id), config.getClass()) == config;
        });
    }

    public static void save() {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        var jsonObject = new JsonObject();
        getModuleConfigs().forEach(pair -> {
            var id = pair.left;
            var config = pair.right;
            jsonObject.add(id, gson.toJsonTree(config));
        });
        try {
            Files.writeString(MODULE_CONFIG_PATH, gson.toJson(jsonObject));
        } catch (IOException e) {
            System.err.println("Could not save utility config: " + e);
        }
    }
}
