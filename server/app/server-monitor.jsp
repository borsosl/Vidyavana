<%@page import="hu.vidyavana.web.RequestInfo" pageEncoding="UTF-8"%>
<%
RequestInfo ri = (RequestInfo) request.getAttribute("_ri");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <title>Pandit</title>
    <script src="/js/lib.js"></script>
    <script src="/js/server-monitor.js"></script>
</head>
<body>
	<pre id="content">
	</pre>
<script>
var pg = <%= ri.ajaxText %>;
</script>
</body>
</html>
