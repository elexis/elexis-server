package es.core.connector.elexis.jpa.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.converter.AllConverterTests;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.util.ComparatorsTest;

@RunWith(Suite.class)
@SuiteClasses({AllConverterTests.class, ComparatorsTest.class})
public class AllTest {

}
