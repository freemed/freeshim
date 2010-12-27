<%-- 
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * FreeMED Electronic Medical Record / Practice Management System
 * Copyright (C) 1999-2011 FreeMED Software Foundation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 --%>
<html>
<head>
<title>FreeSHIM</title>
<meta http-equiv="refresh" content="5;url=status.jsp">
</head>
<body>

<!-- Header -->

<table border="0" cellpadding="5" width="100%">
	<tr>
		<td align="right"><img src="../img/freeshim.png" border="0" alt="" width="200" height="98" /></td>
		<td align="left">FreeSHIM</td>
	</tr>
</table>

<%@ page import="java.util.*"%>
<%@ page import="org.freemedsoftware.shim.Configuration"%>
<%@ page import="org.freemedsoftware.shim.MasterControlServlet"%>
<%
	MasterControlServlet s = Configuration.getServletContext();
	if (request.getParameter("device").equals("DOSING_PUMP")) {
		if (s.getDosingPumpDeviceManager().getActive()) {
			// Stop
			out.println("<b>Stopping DOSING_PUMP device</b><br/>");
			s.getDosingPumpDeviceManager().close();
			out.println("<b>Stopped DOSING_PUMP device</b><br/>");			
		} else {
			// Start
			out.println("<b>Starting DOSING_PUMP device</b><br/>");
			s.getDosingPumpDeviceManager().init();
			out.println("<b>Started DOSING_PUMP device</b><br/>");
		}
	} else if (request.getParameter("device").equals("SIGNATURE")) {
		if (s.getSignatureDeviceManager().getActive()) {
			// Stop
			out.println("<b>Stopping LABEL device</b><br/>");
			s.getSignatureDeviceManager().close();
			out.println("<b>Stopped LABEL device</b><br/>");			
		} else {
			// Start
			out.println("<b>Starting LABEL device</b><br/>");
			s.getSignatureDeviceManager().init();
			out.println("<b>Started LABEL device</b><br/>");
		}
	} else if (request.getParameter("device").equals("LABEL")) {
		if (s.getLabelPrinterDeviceManager().getActive()) {
			// Stop
			out.println("<b>Stopping LABEL device</b><br/>");
			s.getLabelPrinterDeviceManager().close();
			out.println("<b>Stopped LABEL device</b><br/>");			
		} else {
			// Start
			out.println("<b>Starting LABEL device</b><br/>");
			s.getLabelPrinterDeviceManager().init();
			out.println("<b>Started LABEL device</b><br/>");
		}
	} else {
		out.println("Invalid device specified.<br/>");
	}	
	out.println("Please wait to return to the status page.<br/>");
%>

</body>
</html>
