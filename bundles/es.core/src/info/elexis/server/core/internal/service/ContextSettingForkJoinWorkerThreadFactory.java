package info.elexis.server.core.internal.service;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * @since 3.11
 */
public class ContextSettingForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

	private final ContextService contextService;

	public ContextSettingForkJoinWorkerThreadFactory(ContextService contextService) {
		this.contextService = contextService;
	}

	@Override
	public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
		return new ContextSettingForkJoinWorkerThread(pool, contextService, contextService.getInternalRootContext());
	}

}
