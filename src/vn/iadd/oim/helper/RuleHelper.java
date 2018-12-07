/**
 * 
 */
package vn.iadd.oim.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.iam.platform.entitymgr.vo.SearchRule;
import vn.iadd.excel.model.IWorksheet;
import vn.iadd.oim.util.EnumUtil;
import vn.iadd.oim.util.ObjectUtil;
import vn.iadd.oim.util.StringUtil;

/**
 * @author DaiNV
 *
 */
public class RuleHelper {
	
	private Map<String, RuleInfo> mapRows = new HashMap<>();
	
	private String finalKey;
	
	public RuleHelper() {
		initData();
	}
	
	public RuleHelper(IWorksheet sheet) {
		initData();
		parseData(sheet);
	}
	
	private void initData() {
		mapRows.clear();
		finalKey = null;
	}
	
	private void parseData(IWorksheet sheet) {
		if (sheet == null) {
			return;
		}
		List<Map<String, Object>> rows = sheet.getRows();
		for (Map<String, Object> row: rows) {
			Double idObj = ObjectUtil.tryGetValue(row.get("id"), Double.class);
			String id = String.valueOf(idObj);
			final String key = ObjectUtil.tryGetValue(row.get("key"), String.class);
			final String value = ObjectUtil.tryGetValue(row.get("value"), String.class);
			final String operator = ObjectUtil.tryGetValue(row.get("operator"), String.class);
			final String is_final = ObjectUtil.tryGetValue(row.get("is_final"), String.class);
			final String is_combine = ObjectUtil.tryGetValue(row.get("is_combine"), String.class);
			
			if (StringUtil.isEmpty(id) || StringUtil.isEmpty(key) || StringUtil.isEmpty(value) || StringUtil.isEmpty(operator)) {
				continue;
			}
			id = id.trim();
			boolean isFinal = Boolean.parseBoolean(StringUtil.trim(is_final));
			boolean isCombine = Boolean.parseBoolean(StringUtil.trim(is_combine));
			if (isFinal) {
				finalKey = id;
			}
			RuleInfo info = new RuleInfo(id, key, value, operator, isFinal, isCombine);
			mapRows.put(info.getId(), info);
		}
	}
	
	private boolean isValidRuleInfo(RuleInfo info) {
		boolean ret = false;
		if (info == null) {
			return ret;
		}
		final String key = info.getKey();
		final String value = info.getValue();
		final String op = info.getOperator();
		if (StringUtil.isEmpty(key) || StringUtil.isEmpty(value)) {
			return ret;
		}
		SearchRule.Operator operator = EnumUtil.enumValueOf(SearchRule.Operator.class, op);
		if (operator == null) {
			return ret;
		}
		
		if (!info.isCombine()) {
			ret = true;
			return ret;
		}
		
		if (!mapRows.containsKey(key.trim()) || !mapRows.containsKey(value)) {
			return ret;
		}
		ret = true;
		return ret;
	}
	
	public SearchRule getRuleById(String id) {
		if (!mapRows.containsKey(id)) {
			return null;
		}
		return toSearchRule(mapRows.get(id));
	}
	
	public SearchRule toSearchRule() throws IllegalAccessException {
		if (StringUtil.isEmpty(finalKey) || !mapRows.containsKey(finalKey)) {
			throw new IllegalAccessException("Please mark one row as is_final TRUE");
		}
		RuleInfo last = mapRows.get(finalKey);
		return toSearchRule(last);
	}
	
	public SearchRule toSearchRule(RuleInfo info) {
		SearchRule result = null;
		if (!isValidRuleInfo(info)) {
			return result;
		}
		SearchRule.Operator operator = EnumUtil.enumValueOf(SearchRule.Operator.class, info.getOperator());
		if (info.isCombine()) {
			RuleInfo leftInfo = mapRows.get(info.getKey());
			RuleInfo rightInfo = mapRows.get(info.getValue());
			
			SearchRule left = toSearchRule(leftInfo);
			SearchRule right = toSearchRule(rightInfo);
			result = new SearchRule(left, right, operator);
		} else {
			result = new SearchRule(info.getKey(), info.getValue(), operator);
		}
		return result;
		
	}
	
	
	
	private static class RuleInfo {
		private String id, key, value, operator;
		@SuppressWarnings("unused")
		private boolean isFinal, isCombine;

		public RuleInfo(String id, String key, String value, String operator, boolean isFinal, boolean isCombine) {
			super();
			this.id = id;
			this.key = key;
			this.value = value;
			this.operator = operator;
			this.isFinal = isFinal;
			this.isCombine = isCombine;
		}

		public String getId() {
			return id;
		}
		
		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		public String getOperator() {
			return operator;
		}

		public boolean isCombine() {
			return isCombine;
		}
	}
}
