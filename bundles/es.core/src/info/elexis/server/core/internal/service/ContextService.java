package info.elexis.server.core.internal.service;

import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;

@Component
public class ContextService implements IContextService {
	
	private IContext rootContext;
	
	@Activate
	public void activate(){
		rootContext = new Context();
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
	
}
