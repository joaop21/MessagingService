import java.io.IOException;

import Configuration.Config;

public class ConfigTester {
    public static void main(String[] args) throws IOException {
        Config c = Config.loadConfig();

        System.out.println(c.toString());
    }
}
