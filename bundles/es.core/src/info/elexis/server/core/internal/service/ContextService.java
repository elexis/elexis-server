package info.elexis.server.core.internal.service;

import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;
import info.elexis.server.core.SystemPropertyConstants;

@Component
public class ContextService implements IContextService {
	
	private IContext rootContext;
	
	@Activate
	public void activate(){
		rootContext = new Context(SystemPropertyConstants.getStationId());
	}
	
	@Override
	public IContext getRootContext(){
		return rootContext;
	}
	
	@Override
	public Optional<IContext> getNamedContext(String name){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public IContext createNamedContext(String name){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void releaseContext(String name){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void postEvent(String eventTopic, Object object){
		// TODO Auto-generated method stub
		
	}
	
	private class Context implements IContext {
		
		private final String stationId;
		
		public Context(String stationId){
			this.stationId = stationId;
		}
		
		@Override
		public Optional<IUser> getActiveUser(){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void setActiveUser(IUser user){
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public Optional<IContact> getActiveUserContact(){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void setActiveUserContact(IContact user){
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public Optional<IPatient> getActivePatient(){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void setActivePatient(IPatient patient){
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public Optional<IMandator> getActiveMandator(){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void setActiveMandator(IMandator mandator){
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public <T> Optional<T> getTyped(Class<T> clazz){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void setTyped(Object object){
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void removeTyped(Class<?> clazz){
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public Optional<?> getNamed(String name){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void setNamed(String name, Object object){
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public String getStationIdentifier(){
			return stationId;
		}
		
	}
	
}
