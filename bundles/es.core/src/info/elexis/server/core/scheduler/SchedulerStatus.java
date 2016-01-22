package info.elexis.server.core.scheduler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutor;

@XmlRootElement
public class SchedulerStatus {

	public static int STATE_LAUNCH = 0;
	public static int STATE_SUCCESS = 1;
	public static int STATE_FAIL = 2;
	
	@XmlElement
	public Map<String, TaskStatus> taskStatusMap;

	public Map<String, TaskStatus> getTaskStatusMap() {
		if(taskStatusMap == null) {
			taskStatusMap = new HashMap<String, SchedulerStatus.TaskStatus>();
		}
		return taskStatusMap;
	}

	public static class TaskStatus {
		public String className;
		public String id;
		public String schedulingPattern;
		public Date lastExecutionUpdate;
		public Date lastStartTime;
		public long lastExecutionDuration;
		public String status;
		public int lastState;
	}

	/**
	 * Update an already registered task with an execution occurence
	 * @param state 
	 * @param task
	 * @param executor
	 */
	public void updateExecution(int state, Task task, TaskExecutor executor) {
		TaskStatus ts = getTaskStatusMap().get(task.getClass().getName());
		if (ts == null) {
			throw new IllegalStateException("Trying to update execution of task which was not yet registered.");
		}
		
		ts.lastExecutionUpdate = new Date();
		ts.lastState = state;
		ts.lastStartTime = new Date(executor.getStartTime());
		ts.status = executor.getStatusMessage();
		if(state==STATE_SUCCESS) {
			ts.lastExecutionDuration = ts.lastExecutionUpdate.getTime()-ts.lastStartTime.getTime();
		}
	}

	/**
	 * Register a task with the Scheduler Status map
	 * @param task
	 * @param schedulingPattern
	 * @param taskId
	 */
	public void register(Task task, SchedulingPattern schedulingPattern, String taskId) {
		TaskStatus ts = getTaskStatusMap().get(task.getClass().getName());
		if (ts == null) {
			ts = new TaskStatus();
		}
		ts.className = task.getClass().getName();
		ts.id = taskId;
		ts.schedulingPattern = schedulingPattern.toString();
	
		getTaskStatusMap().put(task.getClass().getName(), ts);
	}

	public void deregister(Task task) {
		getTaskStatusMap().remove(task.getClass().getName());
	}
}
