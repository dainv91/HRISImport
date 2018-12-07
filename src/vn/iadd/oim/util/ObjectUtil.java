package vn.iadd.oim.util;


/**
 * ObjectUtil Object util for OIM
 * 
 * @author DaiNV
 * @since 20180531
 *
 */
public class ObjectUtil {

	/**
	 * Try close
	 * 
	 * @param closeable AutoCloseable
	 */
	public static void tryClose(AutoCloseable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Try cast object
	 * 
	 * @param obj   Object
	 * @param clazz Class<T>
	 * @param       T Type parameter
	 * @return T
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T tryGetValue(Object obj, Class<T> clazz) {
		T result = null;
		if (clazz == null || obj == null) {
			return result;
		}
		
		if (obj.getClass().isAssignableFrom(clazz)) {
			result = (T) obj;
		} else {
			if (obj instanceof Number) {
				obj = obj.toString();
			}
			
			if (obj instanceof Boolean) {
				obj = obj.toString();
			}
			result = (T) obj;
		}
		return result;
	}
	
	/**
	 * Try get value with default value
	 * @param obj Object
	 * @param clazz Class<T>
	 * @param defaultValue T
	 * @param T Type parameter
	 * @return T
	 */
	@SuppressWarnings("unchecked")
	public static <T> T tryGetValue(Object obj, Class<T> clazz, T defaultValue) {
		T result = null;
		if (clazz == null || obj == null) {
			return defaultValue;
		}
		if (obj.getClass().isAssignableFrom(clazz)) {
			result = (T) obj;
		}
		if (result == null) {
			return defaultValue;
		}
		return result;
	}
}
