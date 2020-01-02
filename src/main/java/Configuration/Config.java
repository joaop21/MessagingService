package Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public static final String filePath = "./src/main/java/config.json";
    private final int[] network;

    public Config(int[] net) {
        this.network = net;
    }

    public static String getFilePath() {
        return filePath;
    }

    public int[] getNetwork(){return this.network;}

    public String toString(){

        StringBuilder sb = new StringBuilder();
        for(int i = 0 ; i < this.network.length ; i++)
            sb.append("Server: ").append(i+1).append("\n");
        return sb.toString();
    }

    public static Config loadConfig() throws IOException {

        Gson gson = new GsonBuilder().create();

        Path path = new File(filePath ).toPath();

        Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);

        return gson.fromJson(reader,  Config.class);
    }

    public static Config defaultConfig(){
        int[] net = {12345,23456,34567,45678,56789};
        return new Config(net);
    }
}
