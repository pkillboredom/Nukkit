package org.nukkitmc.language;

import java.util.Locale;

/**
 * Created by Snake1999 on 2016/5/13.
 * Package org.nukkitmc.language in project nukkit.
 */
public interface LanguageProvider {

    String get(Locale locale, String key);

    String get(Locale locale, String key, Object... args);

}
