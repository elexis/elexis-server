package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;

public class LockServiceTest  {
	
	@Test
	public void testAcquireLock() {
		LockService service = new LockService();
		LockInfo lockInfo = new LockInfo("objStoreToString::1", "objUser", "testUuid");
		LockResponse lockResponse = service.acquireLock(lockInfo);

		assertNotNull(lockResponse);
		assertTrue(lockResponse.isOk());
		lockResponse = service.releaseLock(lockInfo);
	}
	
	@Test
	public void testReleaseLock() {
		LockService service = new LockService();
		LockInfo lockInfo = new LockInfo("objStoreToString::1", "objUser", "testUuid");
		LockResponse lockResponse = service.acquireLock(lockInfo);

		lockResponse = service.releaseLock(lockInfo);
		assertNotNull(lockResponse);
		assertTrue(lockResponse.isOk());
		assertFalse(service.isLocked("objStoreToString::1"));
	}
	
	@Test
	public void testDoubleAcquire() {
		LockService service = new LockService();
		LockInfo lockInfo = new LockInfo("objStoreToString::1", "objUser", "testUuid");

		LockResponse lockResponse = service.acquireLock(lockInfo);
		assertTrue(lockResponse.isOk());
		assertTrue(service.isLocked("objStoreToString::1"));

		lockResponse = service.acquireLock(lockInfo);
		assertNotNull(lockResponse);
		assertTrue(lockResponse.isOk());
		assertTrue(service.isLocked("objStoreToString::1"));

		lockResponse = service.releaseLock(lockInfo);
	}
	
	@Test
	public void testDoubleRelease() {
		LockService service = new LockService();
		LockInfo lockInfo = new LockInfo("objStoreToString::1", "objUser", "testUuid");
		LockResponse lockResponse = service.acquireLock(lockInfo);

		lockResponse = service.releaseLock(lockInfo);
		assertNotNull(lockResponse);
		assertTrue(lockResponse.isOk());
		assertFalse(service.isLocked("objStoreToString::1"));

		lockResponse = service.releaseLock(lockInfo);
		assertNotNull(lockResponse);
		assertTrue(lockResponse.isOk());
		assertFalse(service.isLocked("objStoreToString::1"));
	}
}
