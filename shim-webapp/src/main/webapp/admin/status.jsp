<%-- 
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * FreeMED Electronic Medical Record / Practice Management System
 * Copyright (C) 1999-2010 FreeMED Software Foundation
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
%>

<table border="1" cellpadding="5">
	<tr>
		<th>Device</th>
		<th>Driver</th>
		<th>Active</th>
		<th>Action</th>
	</tr>
	<tr>
		<th>DOSING_PUMP</th>
		<td><%= s.getDosingPumpDeviceManager().getClassName() %></td>
		<td><%= s.getDosingPumpDeviceManager().getActive() %></td>
		<td></td>
	</tr>
	<tr>
		<th>SIGNATURE</th>
		<td><%= s.getSignatureDeviceManager().getClassName() %></td>
		<td><%= s.getSignatureDeviceManager().getActive() %></td>
		<td></td>
	</tr>
	<tr>
		<th>LABEL</th>
		<td><%= s.getLabelPrinterDeviceManager().getClassName() %></td>
		<td><%= s.getLabelPrinterDeviceManager().getActive() %></td>
		<td></td>
	</tr>
</table>

</body>
</html>
