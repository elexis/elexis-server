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
//public class ClearElexisContextFilter implements Filter {
//	
//	private IContextService contextService;
//	
//	public ClearElexisContextFilter(IContextService contextService){
//		this.contextService = contextService;
//	}
//	
//	@Override
//	public void destroy(){}
//	
//	@Override
//	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//		throws IOException, ServletException{
//		
//		contextService.setActiveCoverage(null);
//		contextService.setActiveMandator(null);
//		contextService.setActiveUser(null);
//		contextService.setActivePatient(null);
//		
//		chain.doFilter(request, response);
//		
//		
//	}
//	
//	@Override
//	public void init(FilterConfig arg0) throws ServletException{}
//	
//}
