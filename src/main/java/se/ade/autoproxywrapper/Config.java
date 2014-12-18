package se.ade.autoproxywrapper;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by adrnil on 11/12/14.
 */
public class Config {
    private String fileName;
    public String forwardProxyHost = "someproxy.intra";
    public int forwardProxyPort = 3128;
    public int localListeningPort = 3128;

    public Config(String filename) {
        this.fileName = filename;
    }

    public Config readFromFile() {
        try {
            String configFile = readFileToStringUtf8(this.fileName);

            if(configFile != null && !configFile.isEmpty()) {
                JSONObject object = new JSONObject(configFile);
                JSONObject forwardProxy = object.getJSONObject("forwardProxy");

                this.forwardProxyHost = forwardProxy.getString("host");
                this.forwardProxyPort = forwardProxy.getInt("port");
                this.localListeningPort = object.getInt("localListeningPort");
            }
        } catch (JSONException e) {
            System.err.println("Unable to parse config: " + e.toString());
        }

        return this;
    }

    public Config saveToFile() {
        try {
            JSONObject rootObject = new JSONObject()
                .put("localListeningPort", this.localListeningPort)
                .put("forwardProxy", new JSONObject()
                    .put("host", this.forwardProxyHost)
                    .put("port", this.forwardProxyPort)

                );

            writeStringToFile(this.fileName, rootObject.toString(4));

        } catch (JSONException e) {
            System.err.println("Unable to save config: " + e.toString());
        }

        return this;
    }

    static String readFileToStringUtf8(String fileName) {
        try {
            Charset encoding = Charset.forName("UTF-8");
            byte[] encoded = Files.readAllBytes(Paths.get(fileName));
            return new String(encoded, encoding);
        } catch (IOException e) {
            System.err.println("Config read failed (" + fileName + "): " + e.toString());
            return null;
        }
    }

    static void writeStringToFile(String path, String data) {
        try {
            Files.write(Paths.get(path), data.getBytes());
        } catch (IOException e) {
            System.err.println("Config save failed: " + e.toString());
        }
    }
}
