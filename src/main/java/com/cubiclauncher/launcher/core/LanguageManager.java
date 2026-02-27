/*
 * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cubiclauncher.launcher.core;

import com.cubiclauncher.launcher.util.GsonProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages language translations from JSON files.
 */
public class LanguageManager {
    private static final Logger log = LoggerFactory.getLogger(LanguageManager.class);
    private static volatile LanguageManager instance;
    private Map<String, String> translations = new HashMap<>();
    private String currentLanguage = "en_us";
    private static final Gson Gson = GsonProvider.PRETTY;
    private LanguageManager() {
        loadLanguage(currentLanguage);
    }

    public static synchronized LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    /**
     * Loads the specified language from the resources.
     * 
     * @param langCode The language code (e.g., "en_us", "es_es")
     */
    public void loadLanguage(String langCode) {
        Path langDir = PathManager.getInstance().getLangPath();
        File localFile = langDir.resolve(langCode + ".json").toFile();
        String resourcePath = "/com.cubiclauncher.launcher/lang/" + langCode + ".json";

        if (!localFile.exists()) {
            exportDefaultLanguage(langCode, localFile);
        }

        if (localFile.exists()) {
            log.info("Loading language from local file: {}", localFile.getAbsolutePath());
            try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(localFile.toPath()),
                    StandardCharsets.UTF_8)) {
                loadFromJson(reader, langCode);
                return;
            } catch (Exception e) {
                log.error("Error loading local language file: {}", localFile.getAbsolutePath(), e);
            }
        }

        // Fallback or if local loading failed
        log.info("Loading language from resources: {}", langCode);
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                log.error("Language file not found in resources: {}", resourcePath);
                if (!langCode.equals("en_us")) {
                    log.warn("Falling back to en_us");
                    loadLanguage("en_us");
                }
                return;
            }
            loadFromJson(new InputStreamReader(is, StandardCharsets.UTF_8), langCode);
        } catch (Exception e) {
            log.error("Error loading language from resources: {}", resourcePath, e);
        }
    }

    private void loadFromJson(InputStreamReader reader, String langCode) {
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> loadedTranslations = Gson.fromJson(reader, type);

        if (loadedTranslations != null) {
            this.translations.clear();
            this.translations = loadedTranslations;
            this.currentLanguage = langCode;
        }
    }

    private void exportDefaultLanguage(String langCode, File destination) {
        log.info("Exporting default language {} to {}", langCode, destination.getAbsolutePath());
        String resourcePath = "/com.cubiclauncher.launcher/lang/" + langCode + ".json";
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) {
                Files.copy(is, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                log.warn("Default language resource not found for export: {}", resourcePath);
            }
        } catch (Exception e) {
            log.error("Failed to export default language: {}", langCode, e);
        }
    }

    /**
     * Gets a translation for the given key.
     * 
     * @param key The translation key
     * @return The translated string, or the key itself if not found
     */
    public String get(String key) {
        return translations.getOrDefault(key, key);
    }

    /**
     * Gets a translation with formatted arguments.
     * 
     * @param key  The translation key
     * @param args The arguments to format
     * @return The translated and formatted string
     */
    public String get(String key, Object... args) {
        String translation = get(key);
        try {
            return String.format(translation, args);
        } catch (Exception e) {
            log.warn("Error formatting translation for key {}: {}", key, e.getMessage());
            return translation;
        }
    }
}
