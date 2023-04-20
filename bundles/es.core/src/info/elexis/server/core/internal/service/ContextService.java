package info.elexis.server.core.internal.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;

@Component
public class ContextService implements IContextService {

	private ThreadLocal<Context> rootContext;
	private ThreadLocal<ConcurrentHashMap<String, Context>> contexts;

	@Activate
	public void activate() {
		rootContext = new ThreadLocal<>() {
			@Override
			protected Context initialValue() {
				Context context = new Context();
				context.setNamed("IS_ELEXIS_SERVER", Boolean.TRUE.toString());
				return context;
			}
		};
		contexts = new ThreadLocal<>() {
			@Override
			protected ConcurrentHashMap<String, Context> initialValue() {
				return new ConcurrentHashMap<String, Context>();
			};
		};
	}

	@Override
	public IContext getRootContext() {
		return rootContext.get();
	}

	@Override
	public Optional<IContext> getNamedContext(String name) {
		return Optional.ofNullable(contexts.get().get(name));
	}

	@Override
	public IContext createNamedContext(String name) {
		Context context = new Context(rootContext.get());
		contexts.get().put(name, context);
		return context;
	}

	@Override
	public void releaseContext(String name) {
		Context context = contexts.get().get(name);
		if (context != null) {
			context.setParent(null);
			contexts.get().remove(name);
		}
	}

	@Override
	public void postEvent(String eventTopic, Object object, Map<String, Object> additionalProperties) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendEvent(String eventTopic, Object object, Map<String, Object> additionalProperties) {
		throw new UnsupportedOperationException();
	}
}
