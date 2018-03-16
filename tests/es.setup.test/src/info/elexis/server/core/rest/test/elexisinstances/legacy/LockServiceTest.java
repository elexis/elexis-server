package info.elexis.server.core.rest.test.elexisinstances.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.eclipsesource.jaxrs.consumer.ConsumerFactory;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockRequest.Type;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.server.ILockService;
import info.elexis.server.core.rest.test.AllTests;
import info.elexis.server.core.rest.test.elexisinstances.ElexisServerClientConfig;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LockServiceTest {

	private static ILockService lockService;

	@BeforeClass
	public static void beforeClass() {
		lockService = ConsumerFactory.createConsumer(AllTests.REST_URL, new ElexisServerClientConfig(),
				ILockService.class);
	}

	private LockInfo lockInfo = new LockInfo("objStoreToString::1", "objUser", "testUuid");
	

	@Test
	public void _01_testAcquireLock() {
		LockResponse response = lockService.acquireOrReleaseLocks(new LockRequest(Type.ACQUIRE, lockInfo));
		assertTrue(response.isOk());
	}

	@Test
	public void _02_testGetLockInfo() {
		LockInfo lockInfo = lockService.getLockInfo("objStoreToString::1");
		assertEquals("1", lockInfo.getElementId());
		assertEquals("objUser", lockInfo.getUser());
		assertEquals("testUuid", lockInfo.getSystemUuid());
		assertEquals("objStoreToString::1", lockInfo.getElementStoreToString());
		assertNotNull(lockInfo.getCreationDate());
	}

	@Test
	@Ignore
	public void _03_testIsLocked() {
		// TODO change interface to lockService.isLocked(LockInfo)
//		boolean locked = lockService.isLocked(request);
//		assertTrue(locked);
	}

	@Test
	public void _04_testReleaseLock() {
		LockResponse response = lockService.acquireOrReleaseLocks(new LockRequest(Type.RELEASE, lockInfo));
		assertTrue(response.isOk());
	}

}
