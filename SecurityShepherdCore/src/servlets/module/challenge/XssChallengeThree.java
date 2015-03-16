package servlets.module.challenge;

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

import dbProcs.Getter;
import utils.FindXSS;
import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;
import utils.XssFilter;
/**
 * Cross Site Scripting Challenge Three control class.
 * <br/><br/>
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Security Shepherd project.  If not, see <http://www.gnu.org/licenses/>. 
 * @author Mark Denihan
 *
 */
public class XssChallengeThree extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(XssChallengeThree.class);
	private static String levelName = "Cross Site Scripting Challenge Three";
	private static String levelHash = "ad2628bcc79bf10dd54ee62de148ab44b7bd028009a908ce3f1b4d019886d0e";
	private static String levelResult = ""; //Pulled from DB using levelHash
	/**
	 * Cross Site Request Forgery safe Reflected XSS vulnerability. cannot be remotly deployed, and therfore only is executable against the person initating the funciton.
	 * @param searchTerm To be spat back out at the user after been filtered
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug(levelName + " Servlet Accessed");
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		try
		{
			HttpSession ses = request.getSession(true);
			if(Validate.validateSession(ses))
			{
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
				log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
				Cookie tokenCookie = Validate.getToken(request.getCookies());
				Object tokenParmeter = request.getParameter("csrfToken");
				if(Validate.validateTokens(tokenCookie, tokenParmeter))
				{
					String searchTerm = request.getParameter("searchTerm");
					log.debug("User Submitted - " + searchTerm);
					searchTerm = XssFilter.levelThree(searchTerm);
					log.debug("After Filtering - " + searchTerm);
					String htmlOutput = new String();
					String applicationRoot = getServletContext().getRealPath("");
					if(FindXSS.antiSamySearch(searchTerm, applicationRoot))
					{
						Encoder encoder = ESAPI.encoder();
						htmlOutput = "<h2 class='title'>Well Done</h2>" +
								"<p>You successfully executed the JavaScript alert command!<br />" +
								"The result key for this lesson is <a>" +
								encoder.encodeForHTML(
										Hash.generateUserSolution(
												Getter.getModuleResultFromHash(getServletContext().getRealPath(""), levelHash
												), (String)ses.getAttribute("userName")
										)
								) +
								"</a>";
					}
					log.debug("Adding searchTerm to Html: " + searchTerm);
					htmlOutput += "<h2 class='title'>Search Results</h2>" +
						"<p>Sorry but there were no results found that related to " +
						searchTerm +
						"</p>";
					log.debug("Outputting HTML");
					out.write(htmlOutput);
				}
			}
		}
		catch(Exception e)
		{
			out.write("An Error Occurred! You must be getting funky!");
			log.fatal(levelName + " - " + e.toString());
		}
	}
}
