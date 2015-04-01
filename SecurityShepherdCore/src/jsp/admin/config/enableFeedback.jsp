<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: enableFeedback.jsp *************************");

/**
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
 * 
 * @author Mark Denihan
 */
 
if (request.getSession() != null)
{
HttpSession ses = request.getSession();
Getter get = new Getter();
//Getting CSRF Token from client
Cookie tokenCookie = null;
try
{
	tokenCookie = Validate.getToken(request.getCookies());
}
catch(Exception htmlE)
{
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(enableFeedback.jsp): tokenCookie Error:" + htmlE.toString());
}
// validateAdminSession ensures a valid session, and valid administrator credentials
// Also, if tokenCookie != null, then the page is good to continue loading
// Token is now validated when accessing admin pages to stop attackers causing other users to tigger logs of access attempts
Object tokenParmeter = request.getParameter("csrfToken");
if(Validate.validateAdminSession(ses, tokenCookie, tokenParmeter))
{
	//Logging Username
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Accessed by: " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));
// Getting Session Variables
//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
Encoder encoder = ESAPI.encoder();
String csrfToken = encoder.encodeForHTML(tokenCookie.getValue());
String ApplicationRoot = getServletContext().getRealPath("");
%>
	<h1 class="title">Enable Feedback</h1>
	Enable feedback to force users to submit feedback on each module before they can complete them
	
	<br/>
	<br/>
	<div id="badData" style="display: none;"></div>
	<div id="theStep">
	<form action="javascript:;" id="leForm">
		<table align="center">
			<tr><td colspan="2" align="center">
				<input type="submit" id="submitButton" value="Enable Module Feedback"/>
				<div id="loadingSign" style="display: none;"><p>Loading...</p></div>
			</td></tr>
		</table>
	</form>
	</div>
	<script>
	$("#leForm").submit(function(){
		$("#badData").hide("fast");
		$("#submitButton").hide("fast");
		$("#loadingSign").show("slow", function(){
			var ajaxCall = $.ajax({
				dataType: "text",
				type: "POST",
				url: "enableFeedback",
				data: {
					csrfToken: "<%= csrfToken %>"
				},
				async: false
			});
			$("#theStep").hide("fast", function(){
				if(ajaxCall.status == 200)
				{
					$("#theStep").html(ajaxCall.responseText);
				}
				else
				{
					$("#badData").html("<p> An Error Occured: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
					$("#badData").show("slow");
				}
			});
			$("#loadingSign").hide("fast", function(){
				$("#theStep").show("slow");
			});
		});
	});
	</script>
	<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
	<%
}
else
{
response.sendRedirect("../../loggedOutSheep.html");
}
}
else
{
response.sendRedirect("../../loggedOutSheep.html");
}
%>