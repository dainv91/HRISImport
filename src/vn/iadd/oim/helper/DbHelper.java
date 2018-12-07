package vn.iadd.oim.helper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import vn.iadd.oim.util.ConfigUtils;
import vn.iadd.util.Logger;

public class DbHelper implements AutoCloseable {

	//final Logger logger;
	
	private String driver, url, user, pass;

	private Connection conn = null;

	/*public DbHelper() {
		this("jdbc:oracle:thin:@10.4.18.113:1521/ORCL.ORACLE.POC", "dainv", "*******");
	}*/
	
	public DbHelper() {
		this(ConfigUtils.getConfig("DB_DRIVER"), ConfigUtils.getConfig("DB_HOST"), ConfigUtils.getConfig("DB_USER"), ConfigUtils.getConfig("DB_PASS"));
	}
	
	/**
	 * Constructor with parameter
	 * 
	 * @param url
	 *            String
	 * @param user
	 *            String
	 * @param pass
	 *            String
	 */
	public DbHelper(String driver, String url, String user, String pass) {
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.pass = pass;
		//this.logger = LogManager.getLogger(this.getClass());
	}

	void log(String msg) {
		//System.out.println(msg);
		//LogUtil.info(logger, msg);
		Logger.log(this.getClass(), msg);
	}
	
	boolean isEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}
	
	/**
	 * Get connection
	 * 
	 * @return Connection
	 * @author DaiNV
	 * @since 20180413
	 */
	private Connection getConnection() {
		boolean connectionCached = false;
		try {
			connectionCached = conn != null && !conn.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (connectionCached) {
			return conn;
		}
		try {
			Class.forName(driver);
			Connection c = DriverManager.getConnection(url, user, pass);
			conn = c;
			return conn;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Close resource
	 * 
	 * @param ac
	 *            AutoCloseable
	 * @author DaiNV
	 * @since 20180413
	 */
	private void closeIgnoreEx(AutoCloseable ac) {
		if (ac == null) {
			return;
		}
		try {
			ac.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {
		closeIgnoreEx(conn);
		conn = null;
	}

	/**
	 * Exec query and return value
	 * 
	 * @param query
	 *            String
	 * @return Map<String, List<Object>>
	 * @author DaiNV
	 * @since 20180413
	 */
	public Map<String, List<Object>> execQuery(String query) {
		log("QUERY_TO_EXEC: " + query);
		Map<String, List<Object>> map = new LinkedHashMap<>();
		Statement stmt = null;
		try {
			Connection conn = getConnection();
			if (conn == null) {
				return map;
			}
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			int colCount = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				List<Object> rowData = getDataRow(rs, colCount);
				if (rowData.isEmpty()) {
					break;
				}
				Object obj = rowData.get(0);
				String key = null;
				if (obj.getClass().isAssignableFrom(String.class)) {
					key = (String) obj;
				} else {
					key = obj.toString();
				}
				map.put(key, rowData);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeIgnoreEx(stmt);
		}
		log("QUERY_RESULT: " + map.size());
		return map;
	}

	/**
	 * Exec query and return map of values
	 * 
	 * @param query String
	 * @return Map<String, Map<String, Object>>
	 * @author DaiNV
	 * @since 20180619
	 */
	public Map<String, Map<String, Object>> execQueryReturnMap(String query) {
		log("QUERY_TO_EXEC: " + query);
		Map<String, Map<String, Object>> map = new LinkedHashMap<>();
		Statement stmt = null;
		try {
			Connection conn = getConnection();
			if (conn == null) {
				return map;
			}
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();
			while (rs.next()) {
				Map<String, Object> rowData = getDataRowWithName(rs, meta, colCount);
				if (rowData.isEmpty()) {
					break;
				}
				Object obj = rowData.get(rowData.keySet().iterator().next());
				String key = null;
				if (obj.getClass().isAssignableFrom(String.class)) {
					key = (String) obj;
				} else {
					key = obj.toString();
				}
				map.put(key, rowData);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeIgnoreEx(stmt);
		}
		log("QUERY_RESULT: " + map.size());
		return map;
	}
	
	/**
	 * Execute update query
	 * 
	 * @param query
	 * @author DaiNV
	 * @since 20180413
	 */
	public boolean execUpdate(String query) {
		log("QUERY_TO_EXEC: " + query);
		boolean success = false;
		Statement stmt = null;
		try {
			Connection conn = getConnection();
			if (conn == null) {
				return success;
			}
			stmt = conn.createStatement();
			int ret = stmt.executeUpdate(query);
			log("QUERY_RESULT: " + ret);
			if (ret >= 0) {
				success = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeIgnoreEx(stmt);
		}
		return success;
	}

	/**
	 * Execute update query return rows count
	 * 
	 * @param query
	 * @author DaiNV
	 * @since 20180413
	 */
	public int execUpdateReturnRowsCount(String query) {
		log("QUERY_TO_EXEC: " + query);
		int ret = 0;
		Statement stmt = null;
		try {
			Connection conn = getConnection();
			if (conn == null) {
				return ret;
			}
			stmt = conn.createStatement();
			ret = stmt.executeUpdate(query);
			log("QUERY_RESULT: " + ret);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeIgnoreEx(stmt);
		}
		return ret;
	}

	/**
	 * Exec batch query update
	 * 
	 * @param queries
	 *            List<String>
	 * @return boolean
	 * @author DaiNV
	 * @since 20180413
	 */
	public boolean execUpdateBatch(List<String> queries) {
		boolean success = false;
		if (queries == null || queries.isEmpty()) {
			return success;
		}
		log("LIST_QUERY_TO_EXEC_BATCH: " + Arrays.toString(queries.toArray()));
		Statement stmt = null;
		Connection conn = null;
		try {
			conn = getConnection();
			if (conn == null) {
				return success;
			}
			stmt = conn.createStatement();
			for (String query : queries) {
				if (isEmpty(query)) {
					continue;
				}
				stmt.addBatch(query);
			}
			int[] ret = stmt.executeBatch();
			log("QUERY_RESULT: " + Arrays.toString(ret));
			success = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeIgnoreEx(stmt);
		}
		return success;
	}

	/**
	 * Get data in row
	 * 
	 * @param rs
	 * @param colCount
	 * @return
	 * @author DaiNV
	 * @since 20180412
	 */
	private List<Object> getDataRow(ResultSet rs, int colCount) {
		List<Object> row = new LinkedList<>();
		for (int i = 1; i <= colCount; i++) {
			try {
				Object colData = rs.getObject(i);
				row.add(colData);
			} catch (Exception ex) {
				// Skip'
			}
		}
		return row;
	}
	
	/**
	 * Get data in row
	 * @param rs ResultSet
	 * @param meta ResultSetMetaData
	 * @param colCount int
	 * @return Map<String, Object>
	 * @author DaiNV
	 * @since 20180619
	 */
	private Map<String, Object> getDataRowWithName(ResultSet rs, ResultSetMetaData meta, int colCount) {
		Map<String, Object> map = new LinkedHashMap<>();
		for (int i = 1; i <= colCount; i++) {
			try {
				String name = meta.getColumnLabel(i);
				Object colData = rs.getObject(i);
				map.put(name, colData);
			} catch (Exception ex) {
				// Skip'
			}
		}
		return map;
	}
	
	public static void main(String[] args) throws Exception {
		//testQuery();
		testRoleQuery();
	}
	static void testQuery() throws Exception {
		DbHelper helper = new DbHelper();
		final String query = "select * from abcd_user";
		Map<String, Map<String, Object>> result = helper.execQueryReturnMap(query);
		helper.close();
		if (result.isEmpty()) {
			return;
		}
		Iterator<Map<String, Object>> it = result.values().iterator();
		while (it.hasNext()) {
			Map<String, Object> map = it.next();
			System.out.println(map);
//			User u = User.fromMapDb(map);
//			System.out.println(u);
		}
	}
	
	static void testRoleQuery() throws Exception {
		DbHelper helper = new DbHelper();
		final String query = "select * from abcd_role";
		Map<String, Map<String, Object>> result = helper.execQueryReturnMap(query);
		helper.close();
		if (result.isEmpty()) {
			return;
		}
		Iterator<Map<String, Object>> it = result.values().iterator();
		while (it.hasNext()) {
			Map<String, Object> map = it.next();
			System.out.println(map);
//			Role role = Role.fromMapDb(map);
//			System.out.println(role);
		}
	}
}
