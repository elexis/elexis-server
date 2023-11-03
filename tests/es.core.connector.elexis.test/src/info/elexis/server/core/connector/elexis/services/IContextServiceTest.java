package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.Test;

import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IContextServiceTest {

	private IContextService contextService = OsgiServiceUtil.getService(IContextService.class).get();

	@Test
	public void submitContextInheriting() {

		long firstNum = 1;
		long lastNum = 1_000_000;

		contextService.getRootContext().setNamed("foo", "bar");

		List<Long> aList = LongStream.rangeClosed(firstNum, lastNum).boxed().collect(Collectors.toList());

		Set<?> submitContextInheriting = contextService.submitContextInheriting(() -> aList.parallelStream()
				.map(e -> ContextServiceHolder.get().getNamed("foo")).map(Optional::get).collect(Collectors.toSet()));
		assertEquals(1, submitContextInheriting.size());
		assertEquals("bar", submitContextInheriting.iterator().next());
	}

}
