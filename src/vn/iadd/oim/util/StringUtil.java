package vn.iadd.oim.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringUtil {

	public static String trim(String str) {
		if (isEmpty(str)) {
			return null;
		}
		return str.trim();
	}
	
	public static boolean isEmpty(String str) {
		if (str == null || str.trim().isEmpty()) {
			return true;
		}
		return false;
	}
	
	public static String escape(Object obj) {
		if (obj == null) {
			return "NULL";
		}
		if (obj.getClass().isAssignableFrom(Date.class)) {
			final String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(obj);
			final String date = "TO_DATE('"+dateStr+"', 'YYYY-MM-DD HH24:MI:SS')";
			return date;
		}
		
		return "'" + obj + "'";
	}
	
	public static int toNumber(String str, int defaultValue) {
		if (str == null || str.trim().isEmpty()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(str);
		} catch (Exception ex) {
			// Skip
		}
		return defaultValue;
	}
	
	public static Date toDate(java.sql.Timestamp time, String fmt) {
		if (time == null) {
			return null;
		}
		return time;
	}
	
	public static Date toDate(String dateStr, String fmt) {
		if (StringUtil.isEmpty(dateStr)) {
			return null;
		}
		Date d = null;
		try {
			d = new SimpleDateFormat(fmt).parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return d;
	}
	
	public static <T>String toInQuery(List<T> values) {
		if (values == null || values.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (T obj : values) {
			final String str = escape(obj);
			sb.append(str).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
	public static Map<String, List<String>> splitMultipleKeyValue(final String str, final String splitStr, final String kvStr) {
		Map<String, List<String>> map = new HashMap<>();
		if (isEmpty(str)) {
			return map;
		}
		String[] arr = str.split(splitStr);
		for (String kv : arr) {
			if (!kv.contains(kvStr)) {
				continue;
			}
			String[] kvs = kv.split(kvStr);
			if (kvs.length == 2) {
				final String key = kvs[0].trim();
				final String value = kvs[1].trim();
				addToMap(map, key, value);
				continue;
			}
			if (kvs.length > 2) {
				final String key = kvs[0];
				int fIndex = kv.indexOf(kvStr);
				final String sub = kv.substring(fIndex+1);
				addToMap(map, key, sub);
			}
		}
		return map;
	}
	
	public static <T>T tryGetIndexOfList(List<T> lst, int index) {
		T obj = null;
		if (lst == null || lst.isEmpty()) {
			return obj;
		}
		if (lst.size() <= index) {
			return obj;
		}
		obj = lst.get(index);
		return obj;
	}
	
	public static <T>T tryGetIndexOfList(List<T> lst) {
		return tryGetIndexOfList(lst, 0);
	}
	
	public static List<String> splitToList(String str, final String splitStr) {
		List<String> lst = new ArrayList<>();
		if (isEmpty(str)) {
			return lst;
		}
		String arr[] = str.split(splitStr);
		for (String s: arr) {
			lst.add(s.trim());
		}
		return lst;
	}
	
	private static void addToMap(Map<String, List<String>> map, final String key, final String value) {
		List<String> lst = null;
		if (!map.containsKey(key)) {
			lst = new ArrayList<>();
		} else {
			lst = map.get(key);
		}
		lst.add(value);
		map.put(key, lst);
	}
	
	public static void main(String[] args) {
		List<Integer> lst = new ArrayList<>();
		lst.add(5);
		lst.add(6);
		lst.add(7);
		//System.out.println(toInQuery(lst));
		
		final String str = "key1=value1=value2, key2 = value2, key1=value3";
		Map<String, List<String>> map = splitMultipleKeyValue(str, ",", "=");
		System.out.println(map);
		//System.out.println(tryGetIndexOfList(lst, 3));
	}
}
