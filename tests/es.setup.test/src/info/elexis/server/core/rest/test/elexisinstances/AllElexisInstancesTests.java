package info.elexis.server.core.rest.test.elexisinstances;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import info.elexis.server.core.rest.test.elexisinstances.legacy.InstanceServiceTest;
import info.elexis.server.core.rest.test.elexisinstances.legacy.LockServiceTest;

@RunWith(Suite.class)
@SuiteClasses({ LockServiceTest.class, InstanceServiceTest.class })
public class AllElexisInstancesTests {

}
