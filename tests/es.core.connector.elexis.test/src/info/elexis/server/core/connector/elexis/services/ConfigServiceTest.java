package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigServiceTest {

	public static final String TEST_KEY = "TestKey";
	public static final String TEST_VALUE = "TestValue";
	public static final String TEST_KEY_SET = "TestKeySet";
	public static final String TEST_VALUE_SET = "TestValue,TestValue2,TestValue3";
	
	@BeforeClass
	public static void init() {
		ConfigService.INSTANCE.set(TEST_KEY, TEST_VALUE);
	}
	
	@AfterClass
	public static void deinit() {
		ConfigService.INSTANCE.remove(TEST_KEY);
		ConfigService.INSTANCE.remove(TEST_KEY_SET);
	}
	
	@Test
	public void testGet() {
		String string = ConfigService.INSTANCE.get(TEST_KEY, null);
		assertEquals(TEST_VALUE, string);
	}

	@Test
	public void testSet() {
		boolean set = ConfigService.INSTANCE.set(TEST_KEY, TEST_VALUE);
		assertTrue(set);
	}

	@Test
	public void testGetAsSet() {
		ConfigService.INSTANCE.remove(TEST_KEY_SET);
		ConfigService.INSTANCE.assertPropertyInSet(TEST_KEY_SET, "TestValue");
		ConfigService.INSTANCE.assertPropertyInSet(TEST_KEY_SET, "TestValue2");
		ConfigService.INSTANCE.assertPropertyInSet(TEST_KEY_SET, "TestValue3");
		Set<String> asSet = ConfigService.INSTANCE.getAsSet(TEST_KEY_SET);
		assertEquals(3, asSet.size());
		assertTrue(asSet.contains("TestValue"));
		assertTrue(asSet.contains("TestValue2"));
		assertTrue(asSet.contains("TestValue3"));
	}

}
