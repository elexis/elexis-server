package info.elexis.server.core.internal.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

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

	protected Context getInternalRootContext() {
		return rootContext.get();
	}

	protected void setInternalRootContext(Context context) {
		rootContext.set(context);
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

	@Override
	public <T> T submitContextInheriting(Callable<T> task) {
		final ForkJoinPool customThreadPool = new ForkJoinPool(4, new ContextSettingForkJoinWorkerThreadFactory(this),
				(t, e) -> LoggerFactory.getLogger(getClass()).error("", e), false);
		try {
			final ForkJoinTask<T> submit = customThreadPool.submit(task);
			return submit.get();
		} catch (InterruptedException | ExecutionException e) {
			LoggerFactory.getLogger(getClass()).error("", e);
			e.printStackTrace();
			return null;
		} finally {
			customThreadPool.shutdown();
		}
	}

}
