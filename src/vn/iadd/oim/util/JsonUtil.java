/**
 * 
 */
package vn.iadd.oim.util;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author DaiNV
 *
 */
public class JsonUtil {
	private static boolean isSerializeNulls = false;

	private static final GsonBuilder gb = new GsonBuilder();
	private static Gson gson;

	public static void setIsSerializeNulls(boolean value) {
		isSerializeNulls = value;
	}

	public static Gson getGson() {
		if (isSerializeNulls) {
			gb.serializeNulls();
		}
		gson = gb.create();
		return gson;
	}

	/**
	 * Convert list object to json
	 *
	 * @param     <T>
	 * @param lst
	 * @return
	 */
	public static final <T> String toJson(List<T> lst) {
		String str = null;
		try {
			str = getGson().toJson(lst);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return str;
	}

	/**
	 * Convert object to json
	 *
	 * @param obj
	 * @return
	 */
	public static final <T> String toJson(T obj) {
		String str = null;
		try {
			str = getGson().toJson(obj);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return str;
	}

	/**
	 * Parse json string to object Type type = new TypeToken<MatchObject
	 * <MatchInfo>>() {}.getType();
	 *
	 * @param json
	 * @param type
	 * @return
	 */
	public static final <T> T fromJson(String json, Type type) {
		T obj = null;
		try {
			obj = getGson().fromJson(json, type);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return obj;
	}
}
