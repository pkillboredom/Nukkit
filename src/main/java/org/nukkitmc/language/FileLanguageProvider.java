package org.nukkitmc.language;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by MagicDroidX on 2016/5/2.
 * Package org.nukkitmc.language in project Nukkit.
 *
 * File language provider, that reads 'org/nukkitmc/language/xxx.lang' file and loads as languages.
 */
public class FileLanguageProvider implements LanguageProvider {

    private static final Pattern pattern = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
    private static final Splitter splitter = Splitter.on('=').limit(2);
    private static final Locale default_locale = Locale.US;
    private Map<Locale, Map<String, String>> loaded = new HashMap<>();

    @Override
    public void checkLocale(Locale locale) {
        try {
            InputStream stream = getInputStreamFromLocale(locale);
            if (stream == null) {
                locale = Locale.forLanguageTag(locale.getCountry());
                stream = getInputStreamFromLocale(locale);
            }
            if (stream == null) {
                locale = default_locale;
                stream = getInputStreamFromLocale(locale);
            }
            if (stream == null) return; //// TODO: 2016/5/14 error:no default locale
            loaded.put(locale, new HashMap<>());
            for (String line : IOUtils.readLines(stream, StandardCharsets.UTF_8)) {
                if (line.isEmpty() || line.charAt(0) == '#') continue;
                String[] kv = Iterables.toArray(splitter.split(line), String.class);
                if (kv == null || kv.length != 2) continue;
                String key = kv[0];
                String value = kv[1];
                value = pattern.matcher(value).replaceAll("%$1s");
                loaded.get(locale).put(key, value);
            }
        } catch (IOException ignore) {}
    }

    private InputStream getInputStreamFromLocale(Locale locale) {
        return FileLanguageProvider.class.getResourceAsStream("/org/nukkitmc/language/" + locale.toString() + ".lang");
    }

    @Override
    public synchronized String get(Locale locale, String key) {
        return this.getFormat(locale, key);
    }

    @Override
    public synchronized String get(Locale locale, String key, Object... args) {
        String format = this.getFormat(locale, key);

        try {
            return String.format(format, args);
        } catch (IllegalFormatException var5) {
            return "Format error: " + format;
        }
    }

    private String getFormat(Locale locale, String key) {
        if (!loaded.containsKey(locale)) checkLocale(locale);
        if (!loaded.containsKey(locale)) {
            checkLocale(default_locale);
            locale = default_locale;
        }
        if (!loaded.containsKey(locale)) return key == null?"":key;
        String format = this.loaded.get(locale).get(key);
        return format == null ? key : format;
    }

}
