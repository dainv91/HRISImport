package vn.iadd.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import oracle.iam.platform.entitymgr.vo.SearchRule;
import vn.iadd.excel.IExcelResult;
import vn.iadd.excel.model.IWorksheet;
import vn.iadd.excel.model.MyExcelResult;
import vn.iadd.oim.helper.OimHelper;
import vn.iadd.oim.helper.RuleHelper;
import vn.iadd.oim.util.EnumUtil;
import vn.iadd.oim.util.ObjectUtil;
import vn.iadd.oim.util.StringUtil;
import vn.iadd.util.Logger;

public class MainTest {

	static void testExcel() throws Exception {
		final String file = "output/hrm-import-scheduler/read_20181207.xlsx";
		IExcelResult result = MyExcelResult.newInstance(file);
		IWorksheet sheet = result.getWorkbook().getSheet("rules");
		RuleHelper ruleHelper = new RuleHelper(sheet);
		OimHelper oim = new OimHelper(true);
		
//		String[] arr = "IaddAccessPolicy,Exchange_access_policy".split(",");
//		List<AccessPolicy> lst = oim.getAccessPolicies(Arrays.asList(arr));
//		for (AccessPolicy a: lst) {
//			System.out.println(a);
//		}
//		System.out.println("Done");
//		System.exit(0);
		
		IWorksheet roleSheet = result.getWorkbook().getSheet("roles");
		
		List<Map<String, Object>> roles = roleSheet.getRows();
		for (Map<String, Object> row: roles) {
			final String roleName = ObjectUtil.tryGetValue(row.get("roleName"), String.class);
			final String roleDescription = ObjectUtil.tryGetValue(row.get("roleDescription"), String.class);
			final String roleDisplayName = ObjectUtil.tryGetValue(row.get("roleDisplayName"), String.class);
			final String ruleId = ObjectUtil.tryGetValue(row.get("rule"), String.class);
			final String accessPolicyStr = ObjectUtil.tryGetValue(row.get("access_policy_name"), String.class);
			SearchRule rule = ruleHelper.getRuleById(ruleId);
			//final String roleId = oimHelper.createRoleMembershipRule(roleName, roleDescription, rule);
			
			List<String> accessPolicies = StringUtil.splitToList(accessPolicyStr, ",");
			String roleId = oim.createOrModifyRole(roleName, roleDescription, roleDisplayName, rule, accessPolicies);
			Logger.log(String.format("Role [%s - %s] is modified", roleName, roleId));
		}
		
		oim.close();
	}
	
	static void testSearchRule() {
		OimHelper oim = new OimHelper(true);
		oim.createRoleMembershipRule("testRole", "testRole desc", "testRole display name", null);
		//Role r = oim.getRoleByName("testRole1");
		//System.out.println(r);
		ObjectUtil.tryClose(oim);
	}
	
	public static void main(String[] args) throws Exception {
		//testSearchRule();
		//Object obj = SearchRule.Operator.EQUAL;
		System.out.println(Arrays.toString(SearchRule.Operator.values()));
		System.out.println(EnumUtil.enumValueOf(SearchRule.Operator.class, "AND1"));
		System.out.println(Boolean.parseBoolean(""));
		
		testExcel();
		//oracle.iam.identity.orgmgmt.api.OrganizationManagerConstants.ORGANIZATION_USER
		oracle.iam.identity.orgmgmt.api.OrganizationManagerConstants.AttributeName.ORG_NAME.getId();
	}

}
