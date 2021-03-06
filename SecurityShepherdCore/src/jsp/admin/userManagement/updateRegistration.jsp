<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: updateRegistration.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(updateRegistration.jsp): tokenCookie Error:" + htmlE.toString());
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
String csrfToken = encoder.encodeForHTMLAttribute(tokenCookie.getValue());
String ApplicationRoot = getServletContext().getRealPath("");
%>
	
	<div id="formDiv" class="post">
		<% if (OpenRegistration.isEnabled()) {%>
		<h1 class="title">Close Registration</h1>
		<% } else {%>
		<h1 class="title">Open Registration</h1>
		<% } %>
		<div class="entry">
			<form id="theForm" action="javascript:;">
			<input type="hidden" id="csrfToken" value="<%= csrfToken %>"/>
				<% if (OpenRegistration.isEnabled()) {%>
					<p>This will close the registration functionality.</p>
					<div id="badData"></div>
					<table align="center">
						<tr><td colspan="2" align="center">
							<input type="submit" id="submitButton" value="Close Registration"/>
						</td></tr>
				<% } else {%>
					<p>This will open the registration functionality.</p>
					<div id="badData"></div>
					<table align="center">
						<tr><td colspan="2" align="center">
							<input type="submit" id="submitButton" value="Open Registration"/>
						</td></tr>
				<% } %>
					</table>
					<script>					
					$("#theForm").submit(function(){
						var theCsrfToken = $('#csrfToken').val();
						//The Ajax Operation
						var ajaxCall = $.ajax({
							type: "POST",
							url: "updateRegistration",
							data: {
								csrfToken: theCsrfToken
							},
							async: false
						});
						if(ajaxCall.status == 200)
						{
							$("#contentDiv").hide("fast", function(){
								$("#contentDiv").html(ajaxCall.responseText);
								$("#contentDiv").show("fast");
							});
						}
						else
						{
							$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
						}
					});
					</script>
					<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
			</form>
		</div>
	</div>
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