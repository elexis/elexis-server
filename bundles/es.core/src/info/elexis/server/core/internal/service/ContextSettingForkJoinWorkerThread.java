package info.elexis.server.core.internal.service;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * @since 3.11
 */
public class ContextSettingForkJoinWorkerThread extends ForkJoinWorkerThread {

	private final ContextService contextService;
	private final Context context;

	public ContextSettingForkJoinWorkerThread(ForkJoinPool pool, ContextService contextService, Context context) {
		super(pool);
		this.contextService = contextService;
		this.context = context;
	}

	@Override
	protected void onStart() {
		contextService.setInternalRootContext(context);
	}

	@Override
	protected void onTermination(Throwable exception) {
	}
}
