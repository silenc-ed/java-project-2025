package Common;

import java.io.InputStream;
import java.util.Properties;

public class ConfigHelper {
    private static Properties properties = new Properties();

    static {
        try (InputStream input = ConfigHelper.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Xin lỗi, không thể tìm thấy application.properties");
            } else {
                properties.load(input);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
