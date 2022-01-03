/* Copyright (C) 2013-2022 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.setting;

import java.util.Locale;
import java.util.Properties;
import java.util.function.Function;

import de.learnlib.api.setting.LearnLibSettingsSource;
import net.automatalib.commons.util.WrapperUtil;
import net.automatalib.commons.util.settings.SettingsSource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LearnLibSettings {

    private static final Logger LOG = LoggerFactory.getLogger(LearnLibSettings.class);

    private static final LearnLibSettings INSTANCE = new LearnLibSettings();
    private final Properties properties;

    private LearnLibSettings() {
        properties = SettingsSource.readSettings(LearnLibSettingsSource.class);
    }

    public static LearnLibSettings getInstance() {
        return INSTANCE;
    }

    public String getProperty(LearnLibProperty property, String defaultValue) {
        return properties.getProperty(property.getPropertyKey(), defaultValue);
    }

    public @Nullable String getProperty(LearnLibProperty property) {
        return properties.getProperty(property.getPropertyKey());
    }

    public <E extends Enum<E>> E getEnumValue(LearnLibProperty property, Class<E> enumClazz, E defaultValue) {
        E value = getEnumValue(property, enumClazz);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    public <E extends Enum<E>> @Nullable E getEnumValue(LearnLibProperty property, Class<E> enumClazz) {
        // TODO: the assumption that enum constants are all-uppercase does not *always* hold!
        return getTypedValue(property, p -> Enum.valueOf(enumClazz, p.toUpperCase(Locale.ROOT)));
    }

    public boolean getBool(LearnLibProperty property, boolean defaultValue) {
        return WrapperUtil.booleanValue(getBoolean(property), defaultValue);
    }

    public @Nullable Boolean getBoolean(LearnLibProperty property) {
        return getTypedValue(property, Boolean::parseBoolean);
    }

    public int getInt(LearnLibProperty property, int defaultValue) {
        return WrapperUtil.intValue(getInteger(property), defaultValue);
    }

    public @Nullable Integer getInteger(LearnLibProperty property) {
        return getTypedValue(property, Integer::parseInt);
    }

    private <T> @Nullable T getTypedValue(LearnLibProperty property, Function<String, T> valueExtractor) {
        String prop = getProperty(property);

        if (prop == null) {
            return null;
        }

        try {
            return valueExtractor.apply(prop);
        } catch (IllegalArgumentException ex) {
            LOG.warn("Could not parse LearnLib property '" + property + "'.", ex);
            return null;
        }
    }

}
