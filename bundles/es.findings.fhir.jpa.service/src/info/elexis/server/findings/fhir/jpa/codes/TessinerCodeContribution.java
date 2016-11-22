package info.elexis.server.findings.fhir.jpa.codes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.codes.ICodingContribution;
import ch.elexis.core.findings.util.model.TransientCoding;

@Component
public class TessinerCodeContribution implements ICodingContribution {

	private List<ICoding> codes;
	private Method rootNodesMethod;
	private Method hasChildren;
	private Method getChildren;
	private Method getCode;
	private Method getText;

	private Object[] emptyObjParam = new Object[0];
	private Class<?>[] emptyClsParam = new Class[0];

	@Override
	public String getCodeSystem() {
		return CodingSystem.ELEXIS_DIAGNOSE_TESSINERCODE.getSystem();
	}

	@Override
	public List<ICoding> getCodes() {
		if (codes == null) {
			codes = loadTessinerCode();
		}
		return codes;
	}

	private List<ICoding> loadTessinerCode() {
		List<ICoding> ret = new ArrayList<>();
		// load tessiner code via reflection
		try {
			Class<?> code = Class.forName("ch.elexis.base.ch.ticode.TessinerCode");
			rootNodesMethod = code.getMethod("getRootNodes", emptyClsParam);
			hasChildren = code.getMethod("hasChildren", emptyClsParam);
			getChildren = code.getMethod("getChildren", emptyClsParam);
			getCode = code.getMethod("getCode", emptyClsParam);
			getText = code.getMethod("getText", emptyClsParam);

			Object[] roots = (Object[]) rootNodesMethod.invoke(null, emptyObjParam);
			for (Object object : roots) {
				collectCodes(object, ret);
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			LoggerFactory.getLogger(TessinerCodeContribution.class).error("Could not load Tessiner Code", e);
		}
		return ret;
	}

	private void collectCodes(Object object, List<ICoding> ret)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if ((boolean) hasChildren.invoke(object, emptyObjParam)) {
			Object[] children = (Object[]) getChildren.invoke(object, emptyObjParam);
			for (Object child : children) {
				collectCodes(child, ret);
			}
		} else {
			ret.add(new TransientCoding(getCodeSystem(), (String) getCode.invoke(object, emptyObjParam),
					(String) getText.invoke(object, emptyObjParam)));
		}
	}
}
