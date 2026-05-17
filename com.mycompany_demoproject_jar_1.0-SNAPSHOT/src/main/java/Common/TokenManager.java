package Common;

import java.util.prefs.Preferences;

public class TokenManager {

    private static final String PREF_NODE = "com.mycompany.demoproject";
    private static final String TOKEN_KEY = "auth_token";

    public static void saveLocalToken(String tokenValue) {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.put(TOKEN_KEY, tokenValue);
    }

    public static String getLocalToken() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        return prefs.get(TOKEN_KEY, null);
    }

    public static void clearLocalToken() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.remove(TOKEN_KEY);
    }
}
