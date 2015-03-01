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

import dbProcs.Getter;
import utils.ScoreboardStatus;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * This is the control class for enabling the the user accessible scoreboard
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
public class EnableScoreboard extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(EnableScoreboard.class);

	/** Initiated by assignPlayers.jsp. A number of players can be assigned to a new class. Changing the actual class of the player is handed by Setter.changePlayerClass
	 * @param classId The identifier of the class to add the players to
	 * @param players[] An array of player identifiers to add to the specified class	
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.EnableScoreboard ***");
		Encoder encoder = ESAPI.encoder();
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		if(Validate.validateAdminSession(ses))
		{
			String htmlOutput = new String();
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				log.debug("Scoreboard being enabled by: " + ses.getAttribute("userName"));
				String[] classInfo = new String[2];
				try
				{
					String applicationRoot = getServletContext().getRealPath("");
					
					log.debug("Getting Parameters");
					String classId = (String)request.getParameter("classId");
					log.debug("classId = " + classId);
					
					if(classId.isEmpty()) // Null Submitted - configure scoreboard to list all players regardless of class
					{
						log.debug("Null Class submitted");
						ScoreboardStatus.setScoreboeardOpen();
						htmlOutput = "Scoreboard is now enabled and lists all users regardless of their class.";
						log.debug(htmlOutput);
					}
					else
					{
						//validate class identifier
						classInfo = Getter.getClassInfo(applicationRoot, classId);
						if(classInfo != null && !classInfo[0].isEmpty()) // Class Exists
						{
							log.debug("Valid Class Submitted");
							ScoreboardStatus.setScoreboardClass(classId);
							htmlOutput = "Scoreboard has been enabled and only lists users from " + encoder.encodeForHTML(classInfo[0]);
							log.debug(htmlOutput);
						}
					}
					if(htmlOutput.isEmpty())
					{
						htmlOutput = "<h2 class='title'>Scoreboard Settings are Unchanged</h2>"
								+ "<p>Invalid data was submitted. Please try again.</p>";
					}
					else //Function must have completed if this isn't empty
					{
						String restrictedScoreboard = Validate.validateParameter(request.getParameter("restricted"), 5);
						if(restrictedScoreboard.isEmpty()) //Public Scoreboard
						{
							log.debug("Public Scoreboard Enabled");
							htmlOutput = "<h2 class='title'>Scoreboard Settings Updated</h2>"
								+ "<p>" + htmlOutput +"</p>";
						}
						else
						{
							ScoreboardStatus.setScoreboardAdminOnly();
							log.debug("Admin Only Scoreboard Enabled");
							htmlOutput = "<h2 class='title'>Scoreboard Settings Updated</h2>"
								+ "<p>" + htmlOutput +" The scoreboard is only accessible by administrators</p>";
						}
					}
					out.write(htmlOutput);
				}
				catch (Exception e)
				{
					log.error("SetDefaultClass Error: " + e.toString());
					out.print("<h2 class=\"title\">Scoreboard Configuration Failure</h2><br>" +
							"<p>" +
							"<font color=\"red\">An error Occurred! Please try again.</font>" +
							"<p>");
				}
			}
			else
			{
				log.debug("CSRF Tokens did not match");
				out.print("<h2 class=\"title\">Scoreboard Configuration Failure</h2><br>" +
						"<p>" +
						"<font color=\"red\">An error Occurred! Please try again.</font>" +
						"<p>");
			}
		}
		else
		{
			out.print("<h2 class=\"title\">Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error Occurred! Please try non administrator functions!</font>" +
					"<p>");
		}
		log.debug("*** EnableScoreboard END ***");
	}
}
