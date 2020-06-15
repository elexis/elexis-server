package info.elexis.server.core.internal.service;

import java.util.List;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IXid;

/**
 * The elexis-server itself, acting if no other user is set.
 */
public class SystemAgentUser implements IUser {
	
	public static final String ELEXISSERVER_AGENTUSER = "__elexis-server__";
	
	@Override
	public boolean isDeleted(){
		return false;
	}
	
	@Override
	public void setDeleted(boolean value){}
	
	@Override
	public String getId(){
		return ELEXISSERVER_AGENTUSER;
	}
	
	@Override
	public String getLabel(){
		return ELEXISSERVER_AGENTUSER;
	}
	
	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists){
		return false;
	}
	
	@Override
	public IXid getXid(String domain){
		return null;
	}
	
	@Override
	public Long getLastupdate(){
		return System.currentTimeMillis();
	}
	
	@Override
	public String getUsername(){
		return ELEXISSERVER_AGENTUSER;
	}
	
	@Override
	public void setUsername(String value){}
	
	@Override
	public String getHashedPassword(){
		return "";
	}
	
	@Override
	public void setHashedPassword(String value){
		
	}
	
	@Override
	public String getSalt(){
		return "";
	}
	
	@Override
	public void setSalt(String value){}
	
	@Override
	public IContact getAssignedContact(){
		return null;
	}
	
	@Override
	public void setAssignedContact(IContact value){}
	
	@Override
	public List<IRole> getRoles(){
		return null;
	}
	
	@Override
	public boolean isActive(){
		return true;
	}
	
	@Override
	public void setActive(boolean value){}
	
	@Override
	public boolean isAllowExternal(){
		return false;
	}
	
	@Override
	public void setAllowExternal(boolean value){
		
	}
	
	@Override
	public boolean isAdministrator(){
		return true;
	}
	
	@Override
	public void setAdministrator(boolean value){
		
	}
	
	@Override
	public IRole addRole(IRole role){
		return null;
	}
	
	@Override
	public void removeRole(IRole role){
		
	}
	
	@Override
	public IUser login(String username, char[] password){
		return null;
	}
	
	@Override
	public boolean isInternal(){
		return true;
	}
	
}
