<%@page import="hu.vidyavana.web.RequestInfo"%>
<%
RequestInfo ri = (RequestInfo) request.getAttribute("_ri");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Pandit</title>
    <link rel="icon" href="/favicon.ico" sizes="16x16 32x32 48x48 64x64 110x110 114x114" type="image/vnd.microsoft.icon" />
    <link rel="stylesheet" type="text/css" href="/css/style.css" />
    <link rel="stylesheet" type="text/css" href="/css/text.css" />
    <script src="/js/jquery.min.js"></script>
    <script src="/js/jquery.mobile.min.js"></script>
    <script src="/js/jquery.mousewheel.min.js"></script>
    <!-- 
    <link rel="stylesheet" type="text/css" href="/js/dynatree/skin/ui.dynatree.css" />
    <script src="/js/dynatree/dev.js"></script>
    -->
    <script src="/js/main.js"></script>
</head>
<body class="noMargin noScroll">
	<div id="measure" class="abs fill novis"></div>
	<div id="header">
		<div id="headerCnt">
			<%-- 
			<span id="search" class="inblk clkTxt">
				Keres
			</span>
			--%>
			<span id="sectionLnk" class="inblk clkTxt">
				<u>S</u>zakasz
			</span>
		</div>
	</div>

	<div id="text" class="autoScroll">
	</div>

	<div id="shadowText">
	</div>

	<div id="sectionPop">
		<div style="margin-top: 10px; text-align: center;">
			Ugrás a választott szakaszra:
		</div>
		<div>
			<select id="sect1" class="sectionSelect"></select>
		</div>
		<div>
			<select id="sect2" class="sectionSelect"></select>
		</div>
		<div>
			<select id="sect3" class="sectionSelect"></select>
		</div>
		<div>
			<select id="sect4" class="sectionSelect"></select>
		</div>
		<div>
			<select id="sect5" class="sectionSelect"></select>
		</div>
		<div>
			<select id="sect6" class="sectionSelect"></select>
		</div>
		<div>
			<select id="sect7" class="sectionSelect"></select>
		</div>
		<div>
			<select id="sect8" class="sectionSelect"></select>
		</div>
		<div>
			<select id="sect9" class="sectionSelect"></select>
		</div>
		<div style="margin-top: 10px; text-align: center;">
			<button id="sectionGo">Mehet</button>
		</div>
	</div>

	<div id="message">
	</div>
	
<script>
var pg = <%= ri.ajaxText %>;
</script>
</body>
</html>
