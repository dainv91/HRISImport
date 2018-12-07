/**
 * 
 */
package vn.iadd.oim.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Function;

import javax.security.auth.login.LoginException;

import oracle.iam.accesspolicy.api.AccessPolicyService;
import oracle.iam.accesspolicy.vo.AccessPolicy;
import oracle.iam.identity.exception.NoSuchRoleException;
import oracle.iam.identity.exception.RoleModifyException;
import oracle.iam.identity.exception.ValidationFailedException;
import oracle.iam.identity.rolemgmt.api.RoleManager;
import oracle.iam.identity.rolemgmt.api.RoleManagerConstants;
import oracle.iam.identity.rolemgmt.api.RoleManagerConstants.RoleAttributeName;
import oracle.iam.identity.rolemgmt.vo.Role;
import oracle.iam.identity.rolemgmt.vo.RoleManagerResult;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.Platform;
import oracle.iam.platform.authz.exception.AccessDeniedException;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.platform.entitymgr.vo.SearchRule;
import vn.iadd.oim.util.StringUtil;
import vn.iadd.util.Logger;

/**
 * @author DaiNV
 *
 */
public class OimHelper implements AutoCloseable {

	private boolean needLogin;
	
	private String oimAuthwlPath;
	private String oimAppServerType;
	private String oimFactoryInitType;

	private String oimUrl, oimUser, oimPass;
	
	private OIMClient oimClient;
	
	public OimHelper() {
		
	}
	
	public OimHelper(boolean needLogin) {
		this(needLogin, "config/authwl.conf", "wls", "weblogic.jndi.WLInitialContextFactory", "t3://10.4.18.114:14000", "xelsysadm", "******");
	}
	
	/**
	 * Constructor with all parameter
	 * 
	 * @param authwlPath
	 * @param appServerType
	 * @param factoryInitType
	 * @param url
	 * @param user
	 * @param pass
	 * 
	 * @author DaiNV
	 * @since 20180411
	 */
	public OimHelper(boolean needLogin, String authwlPath, String appServerType, String factoryInitType, String url, String user,
			String pass) {
		this.needLogin = needLogin;
		oimAuthwlPath = authwlPath;
		oimAppServerType = appServerType;
		oimFactoryInitType = factoryInitType;
		oimUrl = url;
		oimUser = user;
		oimPass = pass;
		init();
	}

	/**
	 * Init
	 */
	public void init() {
		try {
			initialize0();
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void initialize0() throws LoginException {
		// Set system properties required for OIMClient
		// Logger.log("Begin OIM client Login ");

		System.setProperty("java.security.auth.login.config", oimAuthwlPath);
		System.setProperty("APPSERVER_TYPE", oimAppServerType);

		// Create an instance of OIMClient with OIM environment information
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL, oimFactoryInitType);
		env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, oimUrl);

		// Establish an OIM Client
		oimClient = new OIMClient(env);

		try {
			Logger.log(this, "Logging OIM.");
			oimClient.login(oimUser, oimPass.toCharArray());
			Logger.log("Logging OIM successful.");
		} catch (LoginException e) {
			e.printStackTrace();
		}
	}
	
	public <T> T getService(Class<T> clazz) {
		if (needLogin) {
			return oimClient.getService(clazz);
		}
		return Platform.getService(clazz);
	}
	
	public Role getRoleByName(String roleName) {
		Role role = null;
		if (StringUtil.isEmpty(roleName)) {
			return role;
		}
		SearchCriteria criteria = new SearchCriteria(RoleAttributeName.NAME.getId(), roleName, SearchCriteria.Operator.EQUAL);
		RoleManager roleMgr = getService(RoleManager.class);
		List<Role> roles = null;
		try {
			roles = roleMgr.search(criteria, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (roles != null && !roles.isEmpty()) {
			role = roles.get(0);
		}
		return role;
	}
	
	public List<AccessPolicy> getAccessPolicies(List<String> names) {
		List<AccessPolicy> lst = new ArrayList<>();
		if (names == null || names.isEmpty()) {
			return lst;
		}
		
		SearchCriteria criteria = new SearchCriteria(oracle.iam.accesspolicy.vo.AccessPolicy.ATTRIBUTE.NAME.getID(), names, SearchCriteria.Operator.IN);
		AccessPolicyService accMgr = getService(AccessPolicyService.class);
		try {
			lst = accMgr.findAccessPolicies(criteria, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return lst;
	}
	
	public String getAccessPolicyByName(String accessPolicyName) {
		String id = null;
		if (StringUtil.isEmpty(accessPolicyName)) {
			return id;
		}
		List<String> accessPoliciesName = new ArrayList<>();
		accessPoliciesName.add(accessPolicyName);
		List<AccessPolicy> lst = getAccessPolicies(accessPoliciesName);
		if (lst != null && !lst.isEmpty()) {
			id = lst.get(0).getEntityId();
		}
		
		return id;
	}

	public Role createRole(String roleName, String roleDescription, String roleDisplayName) {
		RoleManager roleMgr = getService(RoleManager.class);
		RoleManagerResult roleResult = null;
		HashMap<String, Object> createAttributes = new HashMap<String, Object>();

		Role r = getRoleByName(roleName);
		if (r != null) {
			Logger.log(String.format("Role [%s] is EXISTED with id [%s]", roleName, r.getEntityId()));
			return r;
		}
		
		createAttributes.put(RoleManagerConstants.ROLE_NAME, roleName);
		createAttributes.put(RoleManagerConstants.ROLE_DISPLAY_NAME, roleDisplayName);
		createAttributes.put(RoleManagerConstants.ROLE_DESCRIPTION, roleDescription);
		Role role = new Role(createAttributes);

		try {
			roleResult = roleMgr.create(role);
			Logger.log(String.format("Role [%s] is CREATED with id [%s]", roleName, roleResult.getEntityId()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return getRoleByName(roleName);
	}
	
	public String createRoleMembershipRule(String roleName, String roleDescription, String roleDisplayName, SearchRule rule) {
		Role role = createRole(roleName, roleDescription, roleDisplayName);

		RoleManager roleMgr = getService(RoleManager.class);
		RoleManagerResult roleResult = null;
		try {
			final String roleKey = role.getEntityId();
			final SearchRule userMembershipRule = rule;
			final boolean evaluateMembershipLater = false;
			//roleResult = roleMgr.setUserMembershipRule(roleId, rule);
			roleResult = roleMgr.setUserMembershipRule(roleKey, userMembershipRule, evaluateMembershipLater);
			//Logger.log(roleResult.getEntityId());
        } catch (Exception e) {
            e.printStackTrace();
        }
		return roleResult.getEntityId();
	}
	
	public String createOrModifyRole(String roleName, String roleDescription, String roleDisplayName, SearchRule rule, List<String> accessPolicies) {
		Role role = createRole(roleName, roleDescription, roleDisplayName);
		RoleManager roleMgr = getService(RoleManager.class);
		RoleManagerResult roleResult = null;
		
		Function<List<String>, List<String>> getAccessPolicieIds = (accessPoliciesNames) -> {
			List<String> ids = new ArrayList<>();
			List<AccessPolicy> lst = getAccessPolicies(accessPoliciesNames);
			lst.forEach(accPolicy -> {
				ids.add(accPolicy.getEntityId());
			});
			return ids;
		};
		List<String> ids = getAccessPolicieIds.apply(accessPolicies);
		
		role = new Role((String)role.getEntityId());
		//role.setName(roleName);
		//role.setDisplayName(roleDisplayName);
		
		role.setAttribute(RoleManagerConstants.ROLE_USER_MEMBERSHIP_RULE, rule);
		role.setAttribute(RoleManagerConstants.ACCESS_POLICIES, ids);
		try {
			
			roleResult = roleMgr.modify(role);
			return roleResult.getEntityId();
		} catch (ValidationFailedException | RoleModifyException | NoSuchRoleException | AccessDeniedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void close() throws Exception {
		if (oimClient != null) {
			oimClient.logout();
		}
	}
}
