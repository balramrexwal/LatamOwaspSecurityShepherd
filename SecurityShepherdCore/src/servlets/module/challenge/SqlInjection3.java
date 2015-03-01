package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import utils.ShepherdLogManager;
import utils.SqlFilter;
import utils.Validate;
import dbProcs.Database;

/**
 * SQL Injection Challenge Three - Does not use user specific key
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
public class SqlInjection3 extends HttpServlet
{
	//Sql Challenge 3
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SqlInjection3.class);
	private static String levelName = "SQL Injection Challenge Three";
	private static String levelHash = "b7327828a90da59df54b27499c0dc2e875344035e38608fcfb7c1ab8924923f6";
	private static String levelResult = ""; // Stored in Vulnerable DB. Not User Specific
	/**
	 * Users have to use SQL injection to get a specific users credit card number. The query they are injecting into by default only outputs usernames.
	 * The input they enter is also been filtered.
	 * @param theUserName User name used in database look up.
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		//Attempting to recover user name of session that made request
		HttpSession ses = request.getSession(true);
		if(Validate.validateSession(ses))
		{
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
		
			PrintWriter out = response.getWriter();  
			out.print(getServletInfo());
			String htmlOutput = new String();
			Encoder encoder = ESAPI.encoder();
			try
			{
				String theUserName = request.getParameter("theUserName");
				log.debug("User Submitted - " + theUserName);
				theUserName = SqlFilter.levelThree(theUserName);
				log.debug("Filtered to " + theUserName);
				String ApplicationRoot = getServletContext().getRealPath("");
				log.debug("Servlet root = " + ApplicationRoot );
				
				log.debug("Getting Connection to Database");
				Connection conn = Database.getChallengeConnection(ApplicationRoot, "SqlChallengeThree");
				Statement stmt = conn.createStatement();
				log.debug("Gathering result set");
				ResultSet resultSet = stmt.executeQuery("SELECT customerName FROM customers WHERE customerName = '" + theUserName + "'");
		
				int i = 0;
				htmlOutput = "<h2 class='title'>Search Results</h2>";
				htmlOutput += "<table><tr><th>Name</th></tr>";
				
				log.debug("Opening Result Set from query");
				while(resultSet.next())
				{
					log.debug("Adding Customer " + resultSet.getString(1));
					htmlOutput += "<tr><td>"
						+ encoder.encodeForHTML(resultSet.getString(1)) + "</td></tr>";
					i++;
				}
				htmlOutput += "</table>";
				if(i == 0)
				{
					htmlOutput = "<p>There were no results found in your search</p>";
				}
			}
			catch (SQLException e)
			{
				log.debug("SQL Error caught - " + e.toString());
				htmlOutput += "<p>An error was detected!</p>" +
					"<p>" + encoder.encodeForHTML(e.toString()) + "</p>";
			}
			catch(Exception e)
			{
				out.write("An Error Occurred! You must be getting funky!");
				log.fatal(levelName + " - " + e.toString());
			}
			log.debug("Outputting HTML");
			out.write(htmlOutput);
		}
		else
		{
			log.error(levelName + " servlet accessed with no session");
		}
	}
}
