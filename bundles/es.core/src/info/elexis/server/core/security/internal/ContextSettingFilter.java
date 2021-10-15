//package info.elexis.server.core.security.internal;
//
//import java.io.IOException;
//
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.FilterConfig;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//
//import ch.elexis.core.services.IContextService;
//
//public class ContextSettingFilter implements Filter {
//	
//	private IContextService contextService;
//	
//	public ContextSettingFilter(IContextService contextService){
//		this.contextService = contextService;
//	}
//	
//	@Override
//	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
//		throws IOException, ServletException{
//		
//		contextService.setActiveCoverage(null);
//		contextService.setActiveMandator(null);
//		contextService.setActiveUser(null);
//		contextService.setActivePatient(null);
//		
//		chain.doFilter(req, res); // call KeycloakOIDC
////		
////		KeycloakSecurityContext keycloakSecurityContext =
////			(KeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
////		if (keycloakSecurityContext != null) {
////			IDToken idToken = keycloakSecurityContext.getIdToken();
////			if (idToken != null) {
////				IUser user = KeycloakUser
////				contextService.setActiveUser(user);
////			}
////		}
//		
////		chain.doFilter(req, res);
//	}
//	
//	@Override
//	public void destroy(){}
//	
//	@Override
//	public void init(FilterConfig arg0) throws ServletException{}
//	
//}
