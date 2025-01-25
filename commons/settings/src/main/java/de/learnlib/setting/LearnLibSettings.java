/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
import java.util.ServiceLoader;
import java.util.function.Function;

import de.learnlib.logging.Category;
import net.automatalib.common.util.WrapperUtil;
import net.automatalib.common.util.setting.SettingsSource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LearnLibSettings {

    private static final Logger LOG = LoggerFactory.getLogger(LearnLibSettings.class);

    private static final LearnLibSettings INSTANCE = new LearnLibSettings();
    private final Properties properties;

    private LearnLibSettings() {
        properties = SettingsSource.readSettings(ServiceLoader.load(LearnLibSettingsSource.class));
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
            LOG.warn(Category.CONFIG, String.format("Could not parse LearnLib property '%s'.", property), ex);
            return null;
        }
    }

}
