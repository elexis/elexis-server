//package info.elexis.server.core.connector.elexis.services;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.Semaphore;
//
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;
//
//public class ConfigServiceTest {
//
//	public static final String TEST_KEY = "TestKey";
//	public static final String TEST_VALUE = "TestValue";
//	public static final String TEST_KEY_SET = "TestKeySet";
//	public static final String TEST_VALUE_SET = "TestValue,TestValue2,TestValue3";
//
//	@BeforeClass
//	public static void init() {
//		ConfigService.INSTANCE.set(TEST_KEY, TEST_VALUE);
//	}
//
//	@AfterClass
//	public static void deinit() {
//		ConfigService.INSTANCE.remove(TEST_KEY);
//		ConfigService.INSTANCE.remove(TEST_KEY_SET);
//	}
//
//	@Test
//	public void testGet() {
//		String string = ConfigService.INSTANCE.get(TEST_KEY, null);
//		assertEquals(TEST_VALUE, string);
//	}
//
//	@Test
//	public void testSet() {
//		boolean set = ConfigService.INSTANCE.set(TEST_KEY, TEST_VALUE);
//		assertTrue(set);
//	}
//
//	@Test
//	public void testGetAsSet() {
//		ConfigService.INSTANCE.remove(TEST_KEY_SET);
//		ConfigService.INSTANCE.assertPropertyInSet(TEST_KEY_SET, "TestValue");
//		ConfigService.INSTANCE.assertPropertyInSet(TEST_KEY_SET, "TestValue2");
//		ConfigService.INSTANCE.assertPropertyInSet(TEST_KEY_SET, "TestValue3");
//		Set<String> asSet = ConfigService.INSTANCE.getAsSet(TEST_KEY_SET);
//		assertEquals(3, asSet.size());
//		assertTrue(asSet.contains("TestValue"));
//		assertTrue(asSet.contains("TestValue2"));
//		assertTrue(asSet.contains("TestValue3"));
//	}
//
//	@Test
//	public void testFindAllEntries() {
//		List<Config> findAllEntries = ConfigService.findAllEntries();
//		assertEquals(36, findAllEntries.size());
//	}
//
//	@Test
//	@Ignore
//	public void testMultipleParallelSetAndGet() {
//		int LIMIT = 1000000;
//		Semaphore s = new Semaphore(1);
//
//		Thread t1, t2 = null;
//
//		t1 = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				int currentValue = 0;
//				while (currentValue < LIMIT) {
//					try {
//						s.acquireUninterruptibly();
//						System.out.println("[T1] Setting " + currentValue);
//						ConfigService.INSTANCE.set(TEST_VALUE, Integer.toString(currentValue));
//						Thread.sleep(25);
//						String value = ConfigService.INSTANCE.get(TEST_VALUE, "-999999");
//						assertEquals(currentValue, Integer.parseInt(value));
//						currentValue++;
//						s.release();
//					} catch (InterruptedException ie) {
//						ie.printStackTrace();
//					}
//
//				}
//			}
//		});
//
//		t2 = new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				int currentValue = 0;
//				while (currentValue < LIMIT) {
//					s.acquireUninterruptibly();
//					System.out.println("[T2] Reading " + currentValue);
//					String value = ConfigService.INSTANCE.get(TEST_VALUE, "-999999");
//					// assertEquals(currentValue, Integer.parseInt(value));
//					currentValue++;
//					s.release();
//				}
//			}
//		});
//
//		t1.start();
//		// t2.start();
//
//		try {
//			t1.join();
//			// t2.join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//}
