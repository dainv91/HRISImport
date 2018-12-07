package vn.iadd.oim.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Config utils
 * 
 * @author DaiNV
 * @since 20180411
 */
public class ConfigUtils {

	/**
	 * Config name
	 */
	private static final String CONFIG_NAME = "/abcd_server.properties";
	
	/**
	 * Properties
	 */
	private static Properties props;
	
	static {
		loadProp(CONFIG_NAME);
	}
	
	/**
	 * Load all properties
	 * 
	 * @author DaiNV
	 * @since 20180411
	 */
	public static void loadProp(String configFile) {
		props = new Properties();
		try {
			props.load(ConfigUtils.class.getResourceAsStream(configFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get config using key
	 * @param key String
	 * @return String
	 * @author DaiNV
	 * @since 20180411
	 */
	public static String getConfig(String key) {
		return props.getProperty(key);
	}
	
	/**
	 * Update config value
	 * @param key String
	 * @param value String
	 * @author DaiNV
	 * @since 20180608
	 */
	public static void setConfig(String key, String value) {
		props.setProperty(key, value);
	}
	
	/**
	 * Save config files
	 * @author DaiNV
	 * @throws URISyntaxException 
	 * @since 20180608
	 */
	public static void save() throws IOException, URISyntaxException {
		FileOutputStream out = new FileOutputStream(new File(ConfigUtils.class.getResource(CONFIG_NAME).toURI()));
		props.store(out, null);
	}
	
	/**
	 * 
	 * @return Lookup code, table - column
	 */
	public static Map<String, Map.Entry<String, String>> getAllLookupKeysWithTableAndColumn() {
		return getAllLookupKeysWithTableAndColumnV2();
	}
	
	public static Map<String, Map.Entry<String, String>> getAllLookupKeysWithTableAndColumnV2() {
		Map<String, Map.Entry<String, String>> result = new HashMap<>();
		final String lookup = "Lookup.";
		for (Object keyObj: props.keySet()) {
			String key = (String) keyObj;
			if (!key.toUpperCase().startsWith(lookup.toUpperCase())) {
				continue;
			}
			String value = props.getProperty(key, "");
			if (isEmpty(value)) {
				continue;
			}
			String[] arr = value.split(",");
			String table = arr[0].trim();
			String column = null;
			if (arr.length > 1) {
				column = arr[1].trim();
			}
			Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>(table, column);
			result.put(key, entry);
		}
		return result;
	}
	
	private static boolean isEmpty(String str) {
		return str == null || str.trim().equals("");
	}
	
	static void testSave() throws Exception {
		ConfigUtils.loadProp(CONFIG_NAME);
		final String key = "WS_SERVER_LOCATION";
		String result = getConfig(key);
		System.out.println(result);
		setConfig(key, "value2 updated");
		save();
	}
	
	public static void main(String[] args) throws Exception {
//		ConfigUtils.loadProp();
//		Map<?,?> obj = getAllLookupKeysWithTableAndColumnV2();
//		System.out.println(obj.size() + "===" + obj);
		testSave();
	}
}
