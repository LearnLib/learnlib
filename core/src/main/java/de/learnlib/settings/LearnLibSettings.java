/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.settings;

import java.util.Properties;
import java.util.logging.Logger;

import net.automatalib.commons.util.settings.SettingsSource;

public class LearnLibSettings {

	private static final Logger LOG = Logger.getLogger(LearnLibSettings.class.getName());
	
	private static final LearnLibSettings INSTANCE = new LearnLibSettings();

	public static LearnLibSettings getInstance() {
		return INSTANCE;
	}


	private final Properties properties;
	
	private LearnLibSettings() {
		properties = SettingsSource.readSettings(LearnLibSettingsSource.class);
	}


	public String getProperty(String propName) {
		return properties.getProperty("learnlib." + propName);
	}

	public String getProperty(String propName, String defaultValue) {
		return properties.getProperty("learnlib." + propName, defaultValue);
	}
	
	
	public <E extends Enum<E>> E getEnumValue(String propName, Class<E> enumClazz) {
		String prop = getProperty(propName);
		if(prop == null) {
			return null;
		}
		
		// TODO: the assumption that enum constants are all-uppercase does not *always* hold!
		return Enum.valueOf(enumClazz, prop.toUpperCase());
	}
	
	public <E extends Enum<E>> E getEnumValue(String propName, Class<E> enumClazz, E defaultValue) {
		E value = getEnumValue(propName, enumClazz);
		if(value != null) {
			return value;
		}
		return defaultValue;
	}
	
	public boolean getBool(String propName, boolean defaultValue) {
		Boolean b = getBoolean(propName);
		if(b != null) {
			return b.booleanValue();
		}
		return defaultValue;
	}
	
	public Boolean getBoolean(String propName) {
		String prop = getProperty(propName);
		if(prop != null) {
			return Boolean.parseBoolean(prop);
		}
		return null;
	}
	
	public int getInt(String propName, int defaultValue) {
		Integer prop = getInteger(propName);
		if(prop != null) {
			return prop.intValue();
		}
		return defaultValue;
	}
	
	public Integer getInteger(String propName) {
		String prop = getProperty(propName);
		if(prop != null) {
			try {
				int val = Integer.parseInt(prop);
				return val;
			}
			catch(NumberFormatException ex) {
				LOG.warning("Could not parse LearnLib integer property '" + propName + "': " + ex.getMessage());
			}
		}
		return null;
	}

}
