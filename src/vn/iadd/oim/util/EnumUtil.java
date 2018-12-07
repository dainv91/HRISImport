/**
 * 
 */
package vn.iadd.oim.util;

/**
 * @author DaiNV
 *
 */
public class EnumUtil {

	/**
	 * Get enum value skip exception 
	 * @param clazz SearchRule.Operator.class
	 * @param name [AND, OR, NOT, GREATER_THAN, GREATER_EQUAL, LESS_THAN, LESS_EQUAL, CONTAINS, DOES_NOT_CONTAIN, BEGINS_WITH, DOES_NOT_BEGIN_WITH, ENDS_WITH, DOES_NOT_END_WITH, EQUAL, NOT_EQUAL, IN, NOT_IN, IN_HIERARCHY, SCOPE, RAW_LDAP_QUERY, TABLE_OF_NUMBER, UNION_ALL]
	 * @return
	 */
	public static <T extends Enum<T>> T enumValueOf(Class<T> clazz, String name) {
		T result = null;
		if (name == null || name.trim().isEmpty()) {
			return result;
		}
		try {
			result = Enum.valueOf(clazz, name.trim());	
		} catch (IllegalArgumentException e) {
			// Return null only
		}
		return result;
	}
}
