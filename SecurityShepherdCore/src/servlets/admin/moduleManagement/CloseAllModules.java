package servlets.admin.moduleManagement;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import dbProcs.Setter;
import utils.ShepherdLogManager;
import utils.Validate;

public class CloseAllModules extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(CloseAllModules.class);
	/**
	 * Control class used to open all modules when called by an administrator
	 * @param csrfToken The csrf protection token for this function
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		String servletName = "servlets.module.CloseAllModules";
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("&&& " + servletName + " &&&");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		String htmlOutput = new String();
		HttpSession ses = request.getSession(true);
		if(Validate.validateAdminSession(ses))
		{
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				String ApplicationRoot = getServletContext().getRealPath("");
				Setter.closeAllModules(ApplicationRoot);
				htmlOutput = "<h2 class='title'>All Modules are Now Closed</h2>"
						+ "<p>All of the Security Shepherd levels are now closed and not available for any user to access or submit solutions for!</p>";
			}
			else
			{
				log.debug("CSRF Tokens did not match");
				htmlOutput = "<h2 class='title'>Error</h2><p>CSRF Tokens Did Not Match. Function Aborted</p>";
			}
		}
		else
		{
			log.error("Invalid Session Detected");
			htmlOutput = "<img src=\"css/images/loggedOutSheep.jpg\" /><br/>";
		}
		out.write(htmlOutput);
		log.debug("&&& END " + servletName + " &&&");
	}

}
