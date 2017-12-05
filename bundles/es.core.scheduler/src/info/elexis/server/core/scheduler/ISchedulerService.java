package info.elexis.server.core.scheduler;

public interface ISchedulerService {

	public void schedule(ISchedulerTask ast);
	
	public void deschedule(ISchedulerTask ast);
}
