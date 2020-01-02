import Configuration.Config;

import java.io.IOException;

public class ConfigTester {
    public static void main(String[] args) throws IOException {
        Config c = Config.loadConfig();

        System.out.println(c.toString());
    }
}
