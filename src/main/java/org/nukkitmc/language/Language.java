package org.nukkitmc.language;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Language {
    private static List<LanguageProvider> providers = new ArrayList<>();
    private static Locale userLocale = Locale.getDefault();

    public static void addProvider(LanguageProvider p) {
        providers.add(p);
    }

    public static String get(Locale locale, String key) {
        for (LanguageProvider provider : providers) {
            String got = provider.get(locale, key);
            if (!Objects.equals(got, key)) return got;
        }
        return key;
    }

    public static String get(Locale locale, String key, Object... args) {
        for (LanguageProvider provider : providers) {
            String got = provider.get(locale, key, args);
            if (!Objects.equals(got, key)) return got;
        }
        return key;
    }

    public static String get(String key) {
        return get(userLocale, key);
    }

    public static String get(String key, Object... args) {
        return get(userLocale, key, args);
    }
}
