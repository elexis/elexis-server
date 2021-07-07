//package info.elexis.server.core.rest.test.swagger;
//
//import static info.elexis.server.core.rest.test.AllTests.REST_URL;
//import static org.junit.Assert.assertEquals;
//
//import java.io.IOException;
//import java.net.HttpURLConnection;
//
//import org.junit.Test;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//
//import info.elexis.server.core.rest.test.AllTests;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
//public class SwaggerTest {
//
//	private OkHttpClient client = AllTests.getDefaultOkHttpClient();
//	private Response response;
//
//	Gson gson = new Gson();
//	
//	@Test
//	public void testGetSwaggerConfiguration() throws IOException {
//		Request request = new Request.Builder().url(REST_URL + "/swagger.json").get().build();
//		response = client.newCall(request).execute();
//		assertEquals(HttpURLConnection.HTTP_OK, response.code());
//		
//		String swaggerJson = response.body().string();
//		JsonElement jelem = gson.fromJson(swaggerJson, JsonElement.class);
////		JsonObject swagger = jelem.getAsJsonObject();
//		
////		JsonObject securityDefinitions = swagger.getAsJsonObject("securityDefinitions");
////		JsonObject esoAuth = securityDefinitions.get("esoauth").getAsJsonObject();
////		assertEquals("oauth2", esoAuth.get("type").getAsString());
//	}
//
//}
