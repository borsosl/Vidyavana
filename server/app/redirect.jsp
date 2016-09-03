<%@page pageEncoding="UTF-8" import="
	hu.vidyavana.web.RequestInfo,
	hu.vidyavana.db.model.User"%>
<%
RequestInfo ri = (RequestInfo) request.getAttribute("_ri");
ri.check();
User user = (User) session.getAttribute("user");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, height=device-height, user-scalable=no" />
    <title>Pandit</title>
    <link rel="icon" href="/favicon.ico" sizes="16x16 32x32 48x48 64x64 110x110 114x114" type="image/vnd.microsoft.icon" />
    <link rel="stylesheet" type="text/css" href="/css/style.css" />
    <script src="/js/lib.js"></script>
    <script src="/js/main.js"></script>
</head>
<body class="noMargin noScroll">
<script>
var pg = <%= ri.ajaxText %>;
</script>
</body>
</html>
