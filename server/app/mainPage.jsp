<%@page import="hu.vidyavana.web.RequestInfo" pageEncoding="UTF-8"%>
<%
RequestInfo ri = (RequestInfo) request.getAttribute("_ri");
ri.check();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, height=device-height, user-scalable=no" />
    <title>Pandit</title>
    <link rel="icon" href="/favicon.ico" sizes="16x16 32x32 48x48 64x64 110x110 114x114" type="image/vnd.microsoft.icon" />
    <link rel="stylesheet" type="text/css" href="/css/style.css" />
    <link rel="stylesheet" type="text/css" href="/css/text.css" />
    <script src="/js/lib.js"></script>
    <script src="/js/main.js"></script>
</head>
<body class="noMargin noScroll">
	<div id="measure" class="abs fill novis"></div>
	<div id="header">
        <span id="pandit-icon">&nbsp;</span>
        <span id="search-link" class="inblk">
            <u>K</u>eres
        </span>
        <span id="section-link" class="inblk">
            <u>S</u>zakasz
        </span>

        <%-- Right aligned, priority --%>
        <span class="mini-btn next-hit">
            &raquo;
        </span>
        <span class="mini-btn next-sect">
            &rsaquo;
        </span>
        <span class="mini-btn prev-sect">
            &lsaquo;
        </span>
        <span class="mini-btn prev-hit">
            &laquo;
        </span>

        <%-- Right aligned, extra --%>
        <span id="short-ref">
        </span>

        <%-- Left aligned, extra --%>
        <span id="logout-link" class="inblk">
            Kilé<u>p</u>
        </span>
	</div>

	<div id="content" class="autoScroll">
		<div id="text">
		</div>
		<div class="button-rows">
			<div id="text-buttons">
				<span class="btn prev-sect">&lsaquo; Előző szakasz (&#9668;) <span class="bsp-icon"></span></span>
				<span id="sect-down" class="btn">Több betöltése (&#8594;) <span class="rgt-icon"></span></span>
				<span class="btn next-sect">Köv. szakasz (&#8626;) &rsaquo;</span></span>
			</div>
			<div id="hit-buttons">
				<span class="btn prev-hit">&laquo; Előző találat (,)</span>
				<span id="this-sect" class="btn">Teljes szakasz (&#8626;)</span></span>
				<span class="btn next-hit">Köv találat (.) &raquo;</span>
			</div>
		</div>
	</div>

	<div id="searchPop">
		<div style="margin-top: 10px; text-align: center;">
			Keresés:
		</div>
		<div>
			<input type="text" id="searchInput" class="textInput100">
		</div>
		<div id="search-msg">
		</div>
		<div class="form-row button-row center">
			<button id="searchGo">Mehet</button>
		</div>
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
		<div class="form-row button-row center">
			<button id="sectionGo">Mehet</button>
		</div>
	</div>

	<div id="menu">
	    <div id="menu-short-ref" class="mitem">Nincs szakasz</div>
	    <div id="menu-logout-link" class="mitem">Kilé<u>p</u>és</div>
	</div>

	<div id="message">
	</div>
	
<script>
var pg = <%= ri.ajaxText %>;
</script>
</body>
</html>
