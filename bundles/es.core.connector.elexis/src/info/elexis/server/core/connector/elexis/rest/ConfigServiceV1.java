package info.elexis.server.core.connector.elexis.rest;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.JsonObject;

import ch.elexis.core.jaxrs.JaxrsResource;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.time.TimeUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/config")
@Component
public class ConfigServiceV1 implements JaxrsResource {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IConfigService configService;

	@GET
	@Path("global")
	@Operation(summary = "Return the global configuration tree")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getGlobalConfigurationAsTree() {

		IQuery<IConfig> query = coreModelService.getQuery(IConfig.class, true, false);
		query.orderBy(ModelPackage.Literals.ICONFIG__KEY, ORDER.ASC);
		Map<String, String> keyValues = query.execute().stream()
				.collect(Collectors.toMap(IConfig::getKey, IConfig::getValue));
		JsonObject root = new JsonObject();
		long highestLastUpdate = coreModelService.getHighestLastUpdate(IConfig.class);
		root.addProperty("last-modification-raw", highestLastUpdate);
		root.addProperty("last-modification", TimeUtil.toLocalDateTime(highestLastUpdate).toString());
		JsonObject global = new JsonObject();
		root.add("entries", global);

		// wrapped into TreeMap to have ascending char ordering
		new TreeMap<String, String>(keyValues).entrySet().forEach(entry -> {
			String[] keys = entry.getKey().split("\\/");
			nestedInsert(global, keys, entry.getValue());

		});

		return Response.ok(root.toString()).build();
	}

	private void nestedInsert(JsonObject root, String[] keys, String value) {
		JsonObject current = root;
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (!current.has(key)) {
				if (i == keys.length - 1) {
					current.addProperty(key, value);
					return;
				} else {
					current.add(key, new JsonObject());
				}
			}
			current = current.get(key).getAsJsonObject();
		}
	}

}
