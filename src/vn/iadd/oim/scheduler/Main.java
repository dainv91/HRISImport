/**
 * 
 */
package vn.iadd.oim.scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.iam.platform.entitymgr.vo.SearchRule;
import oracle.iam.scheduler.vo.TaskSupport;
import vn.iadd.excel.IExcelResult;
import vn.iadd.excel.model.IWorksheet;
import vn.iadd.excel.model.MyExcelResult;
import vn.iadd.oim.helper.DbHelper;
import vn.iadd.oim.helper.OimHelper;
import vn.iadd.oim.helper.RuleHelper;
import vn.iadd.oim.util.JsonUtil;
import vn.iadd.oim.util.ObjectUtil;
import vn.iadd.oim.util.StringUtil;

/**
 * @author DaiNV
 *
 */
public class Main extends TaskSupport {

	static Logger logger;

	private DbHelper dbHelper;
	private OimHelper oimHelper;
	
	static {
		initLog();
		if (logger != null) {
			checkLevel();
		}
	}

	private static void out(Level level) {
		System.out.println(level + "--" + logger.isLoggable(level));
	}

	private static void checkLevel() {
		out(Level.ALL);
		out(Level.CONFIG);
		out(Level.FINE);
		out(Level.FINER);
		out(Level.FINEST);
		out(Level.INFO);
		out(Level.OFF);
		out(Level.SEVERE);
		out(Level.WARNING);
	}

	private static void initLog() {
		if (logger != null) {
			// checkLevel();
			return;
		}
		// Init logger
		try {
			// Class.forName("oracle.core.ojdl.logging.ODLLogger");
			// log("oracle.core.ojdl.logging.ODLLogger existed...");
			log("Init logger: " + Main.class.getName());
			logger = Logger.getLogger(Main.class.getName());
			System.out.println(logger);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	static void log(String msg) {
		// System.out.println(msg);
		// logger.error(msg);
		if (logger != null) {
			logger.warning(msg);
		} else {
			System.out.println(msg);
		}
	}

	public Main() {
		super();
		initLog();
		log("Init class using log: " + this.getClass().getName());
		//initDb();
		
		oimHelper = new OimHelper();
	}

	private void initDb(@SuppressWarnings("rawtypes") Map parameters) {
		final String otherParameters = ObjectUtil.tryGetValue(parameters.get("Other parameters"), String.class);
		Map<String, List<String>> others = StringUtil.splitMultipleKeyValue(otherParameters, ",", "=");
		
		final String DB_DRIVER = StringUtil.tryGetIndexOfList(others.get("DB_DRIVER"));
		final String DB_HOST = StringUtil.tryGetIndexOfList(others.get("DB_HOST"));
		final String DB_USER = StringUtil.tryGetIndexOfList(others.get("DB_USER"));
		final String DB_PASS = StringUtil.tryGetIndexOfList(others.get("DB_PASS"));
		dbHelper = new DbHelper(DB_DRIVER, DB_HOST, DB_USER, DB_PASS);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void execute(HashMap parameters) throws Exception {
		log("start");
		initDb(parameters);
		// log("Parameters: " + parameters);
		final String excelFile = ObjectUtil.tryGetValue(parameters.get("Input File"), String.class);
		//final String others = ObjectUtil.tryGetValue(parameters.get("Other parameters"), String.class);
		log("Parameters: " + JsonUtil.toJson(parameters));
		IExcelResult result = MyExcelResult.newInstance(excelFile);
		
		IWorksheet ruleSheet = result.getWorkbook().getSheet("rules");
		RuleHelper ruleHelper = new RuleHelper(ruleSheet);
		
		IWorksheet roleSheet = result.getWorkbook().getSheet("roles");
		List<Map<String, Object>> roles = roleSheet.getRows();
		for (Map<String, Object> row: roles) {
			final String roleName = ObjectUtil.tryGetValue(row.get("roleName"), String.class);
			final String roleDescription = ObjectUtil.tryGetValue(row.get("roleDescription"), String.class);
			final String roleDisplayName = ObjectUtil.tryGetValue(row.get("roleDisplayName"), String.class);
			final String ruleId = ObjectUtil.tryGetValue(row.get("rule"), String.class);
			final String accessPolicyStr = ObjectUtil.tryGetValue(row.get("access_policy_name"), String.class);

			final List<String> accessPolicies = StringUtil.splitToList(accessPolicyStr, ",");
			
			SearchRule rule = ruleHelper.getRuleById(ruleId);
			
			String roleId = oimHelper.createOrModifyRole(roleName, roleDescription, roleDisplayName, rule, accessPolicies);
			log(String.format("Role [%s] is created", roleId));
		}
		
		//System.out.println(dbHelper.execQueryReturnMap("select * from HRIS_USER"));
		ObjectUtil.tryClose(dbHelper);
		ObjectUtil.tryClose(oimHelper);
		log("End execute vn.iadd.oim.scheduler.Main");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public HashMap getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttributes() {
		// TODO Auto-generated method stub
	}

}
