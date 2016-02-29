package info.elexis.server.core.connector.locking;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import info.elexis.server.core.connector.elexis.locking.ILockService;

@Component
public class LockServiceTest  {

	private static ILockService ils;
	
	@Reference(
            service = ILockService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC
    )
	protected void bind(ILockService isc) {
		ils = isc;
	}

	protected void unbind(ILockService isc) {
		ils = null;
	}
	
	private List<String> objectIds = Arrays.asList(new String[]{"foobar", "blaubar", "whatever"});
	
	@BeforeClass
	public static void beforeClass() {
		assertNotNull(ils);
	}
	
	@After
	public void afterTest() {
//		boolean releaseLocks2 = ils.releaseLocks(objectIds, "userId");
//		assertTrue(releaseLocks2);
	}
	
	@Test
	public void testAcquireLock() {
//		boolean acquireLocks = ils.acquireLocks(objectIds, "userId");
//		assertTrue(acquireLocks);
//		
//		boolean acquireLocks2 = ils.acquireLocks(objectIds, "userId");
//		assertFalse(acquireLocks2);
	}
	
	@Test
	public void testReleaseLock() {
//		boolean acquireLocks = ils.acquireLocks(objectIds, "userId");
//		assertTrue(acquireLocks);
//		
//		boolean releaseLocks = ils.releaseLocks(objectIds, "userId");
//		assertTrue(releaseLocks);
//			
//		boolean acquireLocks2 = ils.acquireLocks(objectIds, "userId");
//		assertTrue(acquireLocks2);
	}
	
	@Test
	public void testFailDoubleAcquire() {		
//		boolean acquireLocks = ils.acquireLocks(objectIds, "userId");
//		assertTrue(acquireLocks);
//		
//		boolean acquireLocks2 = ils.acquireLocks(objectIds, "userId");
//		assertFalse(acquireLocks2);
	}
	
}
