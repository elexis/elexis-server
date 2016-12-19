package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.lock.types.LockResponse.Status;

public class LockServiceTest {

	private final LockService service = new LockService();

	private final String LOCK_USER_A = "lockUserA";
	private final String LOCK_USER_B = "lockUserB";
	private final String LOCK_USER_C = "lockUserC";
	private final String LOCK_USER_D = "lockUserD";

	private LockResponse lrA;
	private LockResponse lrB;
	private LockResponse lrC;
	private LockResponse lrD;

	private long lockResponseTimeThreadB;
	private long lockResponseTimeThreadD;

	@Test
	public void testLockResponseInvariants() {
		LockResponse permDeny = new LockResponse(Status.DENIED_PERMANENT, new LockInfo());
		assertFalse(permDeny.isOk());
		LockResponse denied = LockResponse.DENIED(new LockInfo());
		assertFalse(denied.isOk());
		LockResponse error = LockResponse.ERROR;
		assertFalse(error.isOk());
		LockResponse ok = LockResponse.OK();
		assertTrue(ok.isOk());
	}

	@Test
	public void testAcquireLock() {
		LockInfo lockInfo = new LockInfo("objStoreToString::1", "objUser", "testUuid");
		LockResponse lockResponse = service.acquireLock(lockInfo);

		assertNotNull(lockResponse);
		assertTrue(lockResponse.isOk());
		lockResponse = service.releaseLock(lockInfo);
	}

	@Test
	public void testReleaseLock() {
		LockInfo lockInfo = new LockInfo("objStoreToString::1", "objUser", "testUuid");
		LockResponse lockResponse = service.acquireLock(lockInfo);

		lockResponse = service.releaseLock(lockInfo);
		assertNotNull(lockResponse);
		assertTrue(lockResponse.isOk());
		assertFalse(service.isLocked(lockInfo));
	}

	@Test
	public void testDoubleAcquire() {
		LockInfo lockInfo = new LockInfo("objStoreToString::1", "objUser", "testUuid");

		LockResponse lockResponse = service.acquireLock(lockInfo);
		assertTrue(lockResponse.isOk());
		assertTrue(service.isLocked(lockInfo));

		lockResponse = service.acquireLock(lockInfo);
		assertNotNull(lockResponse);
		assertTrue(lockResponse.isOk());
		assertTrue(service.isLocked(lockInfo));

		lockResponse = service.releaseLock(lockInfo);
	}

	@Test
	public void testAcquireLocked() {
		LockInfo lockInfo1 = new LockInfo("objStoreToString::1", "objUser", "testUuid1");
		LockInfo lockInfo2 = new LockInfo("objStoreToString::1", "objUser", "testUuid2");

		LockResponse lockResponse = service.acquireLock(lockInfo1);
		assertTrue(lockResponse.isOk());
		assertTrue(service.isLocked(lockInfo1));

		lockResponse = service.acquireLock(lockInfo2);
		assertNotNull(lockResponse);
		assertFalse(lockResponse.isOk());
		assertFalse(service.isLocked(lockInfo2));

		lockResponse = service.releaseLock(lockInfo1);
	}

	@Test
	public void testDoubleRelease() {
		LockInfo lockInfo = new LockInfo("objStoreToString::1", "objUser", "testUuid");
		LockResponse lockResponse = service.acquireLock(lockInfo);

		lockResponse = service.releaseLock(lockInfo);
		assertNotNull(lockResponse);
		assertTrue(lockResponse.isOk());
		assertFalse(service.isLocked(lockInfo));

		lockResponse = service.releaseLock(lockInfo);
		assertNotNull(lockResponse);
		assertFalse(lockResponse.isOk());
		assertFalse(service.isLocked(lockInfo));
	}

	@Test
	public void testAcquireLockBlocking() throws InterruptedException {
		LockInfo lockInfo1 = new LockInfo("objStoreToString::11", LOCK_USER_A, "testUuid1");
		LockInfo lockInfo2 = new LockInfo("objStoreToString::11", LOCK_USER_B, "testUuid2");

		LockResponse lockResponse = service.acquireLock(lockInfo1);
		assertTrue(lockResponse.isOk());
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(500);
					service.releaseLock(lockInfo1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();

		LockResponse al1 = service.acquireLock(lockInfo2);
		assertFalse(al1.isOk());

		long start = System.currentTimeMillis();
		LockResponse lockResponse2 = service.acquireLockBlocking(lockInfo2, 5);
		long end = System.currentTimeMillis();
		thread.join();
		assertTrue(end - start > 350);
		assertTrue(lockResponse2.isOk());
		assertFalse(thread.isAlive());
	}

	/**
	 * #5431 still in architectural discussion, this test would fail but is
	 * correct
	 * 
	 * @throws InterruptedException
	 */
	@Ignore
	public void testAcquireLockInOrderBlocking() throws InterruptedException {
		LockInfo lockInfoA = new LockInfo("objStoreToString::12", LOCK_USER_A, "testUuid1");
		LockInfo lockInfoB = new LockInfo("objStoreToString::12", LOCK_USER_B, "testUuid2");
		LockInfo lockInfoC = new LockInfo("objStoreToString::12", LOCK_USER_C, "testUuid3");
		LockInfo lockInfoD = new LockInfo("objStoreToString::12", LOCK_USER_D, "testUuid4");

		long refTime = System.currentTimeMillis();
		Thread threadA = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					System.out.println("[RQA] " + (System.currentTimeMillis() - refTime));
					lrA = service.acquireLock(lockInfoA);
					Thread.sleep(2000);
					System.out.println("[RLA] " + lrA.getStatus() + " " + (System.currentTimeMillis() - refTime));
					service.releaseLock(lockInfoA);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		threadA.start();

		Thread threadB = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(250);
					System.out.println("[RQB] " + (System.currentTimeMillis() - refTime));
					lrB = service.acquireLockBlocking(lockInfoB, 3);
					lockResponseTimeThreadB = System.currentTimeMillis();
					System.out.println("[RB] " + lrB.getStatus() + " " + (lockResponseTimeThreadB - refTime));
					Thread.sleep(1000);
					System.out.println("[RLB] " + (System.currentTimeMillis() - refTime));
					service.releaseLock(lockInfoB);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		threadB.start();

		Thread threadC = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(750);
					System.out.println("[RQC] " + (System.currentTimeMillis() - refTime));
					lrC = service.acquireLockBlocking(lockInfoC, 2);
					System.out.println("[RC] " + lrC.getStatus() + " " + (System.currentTimeMillis() - refTime));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		threadC.start();

		Thread threadD = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(2000);
					System.out.println("[RQD] " + (System.currentTimeMillis() - refTime));
					lrD = service.acquireLockBlocking(lockInfoD, 2);
					lockResponseTimeThreadD = System.currentTimeMillis();
					System.out.println("[RD] " + lrD.getStatus() + " " + (lockResponseTimeThreadD - refTime));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		threadD.start();

		threadA.join();
		threadB.join();
		threadC.join();
		threadD.join();

		assertEquals(Status.OK, lrA.getStatus());
		assertEquals(Status.OK, lrD.getStatus());
		assertEquals(Status.OK, lrB.getStatus());

		assertEquals(Status.DENIED, lrC.getStatus());
		assertTrue(lockResponseTimeThreadD > lockResponseTimeThreadB);
	}

}
