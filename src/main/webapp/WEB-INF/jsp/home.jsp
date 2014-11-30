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
		<p>Search for a random string ID below:</p>
		<input id="item_id" type="text" />
		<button type="button" id="search">Search</button>
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
			<tr id="expl">
				<td colspan="3" style="text-align:center">Enter an ID above and see what happens!</td>
			</tr>
			<tr id="inflight">
				<td colspan="3" style="text-align:center">Searching...</td>
			</tr>
			<tr id="error">
				<td colspan="3" style="text-align:center">There was an error: <span id="message"></span></td>
			</tr>
			<tr id="results">
				<td id="resid"></td>
				<td id="resdesc"></td>
				<td id="resprice"></td>
			</tr>
		</tbody>
	</table>
<ul>
<li><a href="apps">Apps</a></li>
<li><a href="j_spring_security_logout">Logout</a></li>
<li><a href="<c:url value="/"/>">Home</a></li>
</ul>
<script type="text/javascript">
	$(function() {
		$('#expl').show();
		$('#inflight').hide();
		$('#results').hide();
		$('#error').hide();
		
		// Add configuration for one or more providers.
		jso_configure({
			"uaa": {
				client_id: "${clientId}",
				redirect_uri: window.location,
				authorization: "${userAuthorizationUri}",
			}
		});

		$('#search').click(function() {
			$('#expl').hide();
			$('#results').hide();
			$('#inflight').show();
			$('#error').hide();
			
			var id = $('#item_id').val();
			
			$.oajax({
				url: '/service/item/byId/' + id,
				jso_provider: 'uaa',
				jso_allowia: true,
				jso_scopes: ['openid', 'cloud_controller.read'],
				dataType: 'json',
				success: function(data) {
					$('#resid').text(data.id);
					$('#resdesc').text(data.description);
					$('#resprice').text(data.price);
					
					$('#expl').hide();
					$('#error').hide();
					$('#inflight').hide();
					$('#results').show();
				},
				error: function(xhr,text) {
					$('#expl').hide();
					$('#error').show();
					$('#inflight').hide();
					$('#results').hide();
					
					$('#message').text(text);
				}
			});
			
			jso_dump();
			jso_wipe();
		});
	});
</script>

</body>
</html>
