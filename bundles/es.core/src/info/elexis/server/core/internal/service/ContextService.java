package info.elexis.server.core.internal.service;

import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;

@Component
public class ContextService implements IContextService {
	
	private ThreadLocal<IContext> rootContext;
	
	@Activate
	public void activate(){
		rootContext = new ThreadLocal<IContext>() {
			// global values for all threads
			@Override
			protected IContext initialValue(){
				Context context = new Context();
				context.setNamed("IS_ELEXIS_SERVER", Boolean.TRUE.toString());
				return context;
			}
		};
	}
	
	@Override
	public IContext getRootContext(){
		return rootContext.get();
	}
	
	@Override
	public Optional<IContext> getNamedContext(String name){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IContext createNamedContext(String name){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void releaseContext(String name){
		throw new UnsupportedOperationException();	
	}
	
	@Override
	public void postEvent(String eventTopic, Object object){
		throw new UnsupportedOperationException();	
	}
	
}
