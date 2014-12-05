<%--

    Cloud Foundry 2012.02.03 Beta
    Copyright (c) [2009-2012] VMware, Inc. All Rights Reserved.

    This product is licensed to you under the Apache License, Version 2.0 (the "License").
    You may not use this product except in compliance with the License.

    This product includes a number of subcomponents with
    separate copyright notices and license terms. Your use of these
    subcomponents is subject to the terms and conditions of the
    subcomponent's license, as noted in the LICENSE file.

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page session="false"%>
<html>
<head>
<title>Client Authentication Example</title>
	<script type="text/javascript" src="resources/js/libs/json2.js"></script>
	<script type="text/javascript" src="resources/js/libs/localstorage.js"></script>
	<script type="text/javascript" src="resources/js/libs/modernizr-2.5.3.min.js"></script>
	<script type="text/javascript" src="resources/js/jquery.min.js"></script>
	<script type="text/javascript" src="resources/js/libs/jso.js"></script>
</head>
<body>
	<h1>Client Authentication Sample</h1>

	<div id="content">
		<form method="POST">
		<p>Search for a random string ID below:</p>
		<input id="item_id" type="text" />
		<button type="submit" id="search">Search</button>
		</form>
	</div>
	
	<table>
		<thead>
			<tr>
				<th>ID</th>
				<th>Description</th>
				<th>Price</th>
			</tr>
		</thead>
		<tbody>
			<c:choose>
			<c:when test="${not empty item}">
			<tr id="results">
				<td id="resid">${item.id}</td>
				<td id="resdesc">${item.description}</td>
				<td id="resprice">${item.price}</td>
			</tr>
			</c:when>
			<c:otherwise>
			<tr>
				<td colspan="3" style="text-align:center">Please enter an ID above and click "Search"</td>
			</tr>
			</c:otherwise>s
			</c:choose>
		</tbody>
	</table>
<ul>
<li><a href="j_spring_security_logout">Logout</a></li>
</ul>
</body>
</html>
