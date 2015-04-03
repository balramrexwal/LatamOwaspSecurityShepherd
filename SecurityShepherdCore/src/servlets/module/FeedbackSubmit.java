package servlets.module;

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

import utils.Hash;
import utils.ModulePlan;
import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Getter;
import dbProcs.Setter;

/**
 * Marks modules as completed and stores feedback
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
public class FeedbackSubmit extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SolutionSubmit.class);
	/**
	 * Initiated by a dynamic form returned by servlets.module.SolutionSubmit.doPost() 
	 * this method checks the existence of the submitted module identifier before ensuring that the submission is correct.
	 * If the module solution submission is found to be valid then the feedback submitted is stored, marking the module as completed for the user
	 * If the submission is found to be valid then the user is returned with a feedback form.
	 * @param mouleId The identifier of the module that the solution is been submitted for
	 * @param solutionKey The solution key for the proposed module
	 * @param before The knowledge the user had before completing the module
	 * @param after The knowledge the user had after completing the module
	 * @param difficulty The difficulty the user had completing the module
	 * @param additionalInfo Additional Feedback information
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("&&& servlets.module.FeedbackSubmit &&&");
		Encoder encoder = ESAPI.encoder();
		String htmlOutput = new String();
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug("Current User: " + ses.getAttribute("userName").toString());
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				boolean notNull = false;
				String storedResult = null;
				try
				{
					String ApplicationRoot = getServletContext().getRealPath("");
					
					log.debug("Getting Parameters");
					String moduleId = (String)request.getParameter("moduleId");;
					log.debug("moduleId = " + moduleId.toString());
					String solutionKey = encoder.decodeForHTML((String)request.getParameter("solutionKey"));
					log.debug("solutionKey = " + solutionKey.toString());
					int before = Integer.parseInt(Validate.validateParameter(request.getParameter("before"), 1));
					log.debug("before = " + before);
					int after = Integer.parseInt(Validate.validateParameter(request.getParameter("after"), 1));
					log.debug("after = " + after);
					int difficulty = Integer.parseInt(Validate.validateParameter(request.getParameter("difficulty"), 1));
					log.debug("difficulty = " + difficulty);
					String additionalInfo = Validate.validateParameter(request.getParameter("extra"), 5012);
					log.debug("additionalInfo = " + additionalInfo);
					
					log.debug("Getting session parameters");
					String userId = (String)ses.getAttribute("userStamp");
					String userName = (String)ses.getAttribute("userName");
					log.debug("userId = " + userId);
					
					//Validation
					notNull = (moduleId != null && solutionKey != null);
					if(notNull)
					{
						storedResult = Getter.getModuleResult(ApplicationRoot, moduleId);
					}
					if(notNull && storedResult != null)
					{
						boolean validKey = false;
						//Identify if solution is a user Specific key (Does it need to be decrypted?)
						if(Getter.getModuleKeyType(ApplicationRoot, moduleId))
							validKey = storedResult.compareTo(solutionKey) == 0;
						else
						{
							//Encrypted Solution key,  must be decrypted before compare
							String decryptedKey = Hash.decryptUserSpecificSolution(Validate.validateEncryptionKey(userName), solutionKey);
							storedResult += Hash.getCurrentSalt(); //Add server solution salt to base key before compare with decrypted key
							validKey = storedResult.compareTo(decryptedKey) == 0;
							log.debug("Decrypted Submitted Key: " + decryptedKey);
							log.debug("Stored Expected Key    : " + storedResult);
						}
						if(validKey)
						{
							log.debug("Correct key submitted, checking user has not already completed");
							String hasNotCompletedBefore = Getter.checkPlayerResult(ApplicationRoot, moduleId, userId);
							if(hasNotCompletedBefore != null)
							{
								//Marking module complete by user 
								String result = Setter.updatePlayerResult(ApplicationRoot, moduleId, userId, additionalInfo, before, after, difficulty);
								if(result != null)
								{
									log.debug("User Result for module " + result + " succeeded");
									htmlOutput = new String("<h2 class=\"title\">Solution Submission Success</h2><br>" +
											"<p>" +
											encoder.encodeForHTML(result) + " completed! Congratulations.");
									htmlOutput += "</p>";
									if(ModulePlan.isIncrementalFloor())
										htmlOutput += refreshMenuScript(encoder.encodeForHTML((String)tokenParmeter));
								}
								else
								{
									htmlOutput = new String("Could not update user result");
									out.print("<h2 class=\"title\">Solution Submission Failure</h2><br>" +
											"<p><font color=\"red\">" +
											"Sorry but an error Occurred!" +
											"</font></p>");
								}
							}
							else //User Has already completed this level
							{
								log.error("User has completed this module before. Returning Error");
								out.write("<h2 class=\"title\">Haven't You Done This Already?</h2><br>" +
										"<p>" +
										"Our records say you have already completed this module! Go try another one!" +
										"</p>");
							}
						}
						else
						{
							log.error("Incorrect key submitted, returning error");
							out.print("<h2 class=\"title\">Solution Submission Failure</h2><br>" +
									"<p><font color=\"red\">" +
									"Incorrect Solution Key Submitted.<br><br>You have limited amounts of incorrect key submissions before you will loose 10% of your points. Contact the OWASP Security Shepherd if you think you have found the correct key but it is failing you." +
									"</font></p>");
							
							log.error("Invoking Bad Submission procedure...");
							Setter.incrementBadSubmission(ApplicationRoot, userId);
							log.error(userName + " has been warned and potentially has lost points");
						}
					}
					else
					{
						//Validation Error Responses
						String errorMessage = "An Error Occurred: ";
						if(!notNull)
						{
							log.error("Null values detected");
							errorMessage += "Invalid Request. Please try again";
						}
						else if(storedResult == null)
						{
							log.error("Module not found");
							errorMessage += "Module Not Found. Please try again";
						}
						htmlOutput = new String("<h2 class=\"title\">Feedback Submission Failure</h2><br>" +
								"<p><font color=\"red\">" +
								encoder.encodeForHTML(errorMessage) +
								"</font></p>");
					}
				}
				catch (Exception e)
				{
					log.error("Feedback Submission Error: " + e.toString());
					htmlOutput = new String("<h2 class=\"title\">Feedback Submission Failure</h2><br>" +
							"<p>" +
							"<font color=\"red\">An error Occurred! Please try again.</font>" +
							"</p>");
				}
			}
			else
			{
				log.debug("CSRF Tokens did not match");
				htmlOutput = new String("<h2 class=\"title\">Feedback Submission Failure</h2><br>" +
						"<p>" +
						"<font color=\"red\">An error Occurred! Please try again.</font>" +
						"</p>");
			}
		}
		else
		{
			htmlOutput = new String("<h2 class=\"title\">Feedback Submission Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error Occurred! Please try non administrator functions or Log in!</font>" +
					"</p>");
		}
		out.write(htmlOutput);
		log.debug("&&& END SolutionSubmit &&&");
	}
	
	public static String refreshMenuScript(String csrfToken)
	{
		return refreshMenuScript1 + csrfToken + refreshMenuScript2;
	}
	
	private static String refreshMenuScript1 = "<script>" +
			"$(\"#sideMenuWrapper\").slideUp(\"fast\", function(){" +
			"var ajaxCall = $.ajax({" +
				"type: \"POST\"," +
				"url: \"refreshMenu\"," +
				"data: {" +
					"csrfToken: \"";
	private static String refreshMenuScript2 = "\"" +
				"}," +
				"async: false" +
			"});" +
			"if(ajaxCall.status == 200)" +
			"{" +
				"$(\"#sideMenuWrapper\").html(ajaxCall.responseText);" +
			"}" +
			"else" +
			"{" +
				"$(\"#sideMenuWrapper\").append(\"<br/><font color='red'>Refresh Failed: \" + ajaxCall.status + \"</font>\");" +
			"}" +
		"});" +
		"$('#sideMenuWrapper').slideDown('slow');" +
		"</script>";
}
