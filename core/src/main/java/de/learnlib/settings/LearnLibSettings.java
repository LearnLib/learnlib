/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 *
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.settings;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

public class LearnLibSettings {

	private static final Logger LOG = Logger.getLogger(LearnLibSettings.class.getName());
	
	private static final LearnLibSettings INSTANCE = new LearnLibSettings();

	public static LearnLibSettings getInstance() {
		return INSTANCE;
	}


	private final Properties properties = new Properties();
	
	private LearnLibSettings() {
		try {
			Enumeration<URL> resourceUrls = getClass().getClassLoader().getResources("learnlib.properties");
			while(resourceUrls.hasMoreElements()) {
				URL url = resourceUrls.nextElement();
				try(BufferedInputStream is = new BufferedInputStream(url.openStream())) {
					properties.load(is);
				}
				catch(IOException ex) {
					LOG.severe("Could not read property file " + url + ": " + ex.getMessage());
				}
			}
		}
		catch(IOException ex) {
			LOG.severe("Could not enumerate learnlib.properties files: " + ex.getMessage());
		}

		// System properties (specified via command line) override all other properties
		properties.putAll(System.getProperties());
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
