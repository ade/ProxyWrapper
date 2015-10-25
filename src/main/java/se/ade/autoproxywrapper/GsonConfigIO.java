package se.ade.autoproxywrapper;

import com.google.gson.Gson;
import se.ade.autoproxywrapper.model.GsonConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GsonConfigIO {

    private static final String FILE = System.getProperty("user.dir") + File.separator + "AutoProxyWrapper2.json";

    private static final Gson GSON = new Gson();

    public static GsonConfig load() {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(FILE));
            String json = new String(bytes, "UTF-8");
            return GSON.fromJson(json, GsonConfig.class);
        } catch (java.io.IOException e) {
            System.err.println("Could not load config: " + e.getMessage());
            return null;
        }
    }

    public static void save(GsonConfig gsonConfig) {
        try {
            Files.write(Paths.get(FILE), GSON.toJson(gsonConfig).getBytes());
        } catch (IOException e) {
            System.err.println("Could not save config:" + e.getMessage());
        }
    }

}
