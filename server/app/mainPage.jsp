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
    <link rel="stylesheet" type="text/css" href="/css/text.css" />
    <script src="/js/lib.js"></script>
<% if(ri.admin) { %>
    <link rel="stylesheet" type="text/css" href="/css/admin.css" />
    <script src="/js/admin.js"></script>
<% } else { %>
    <script src="/js/main.js"></script>
<% } %>
</head>
<body class="noMargin noScroll">
	<div id="measure" class="abs fill novis"></div>
	<div id="header">
        <span id="pandit-icon">&nbsp;</span>
        <span id="info-icon">&nbsp;</span>

        <%-- Right aligned, priority --%>
        <span class="mini-btn switch-view" title="Nézet váltás (Enter)">
            &harr;
        </span>
        <span class="mini-btn next-page" title="Következő oldal ( . | N+)">
            &raquo;
        </span>
        <span class="mini-btn prev-page" title="Előző oldal ( , | N-)">
            &laquo;
        </span>

        <%-- Left aligned, extra --%>
        <span id="search-link" class="inblk">
            <u>K</u>eres
        </span>
        <span id="section-link" class="inblk">
            <u>S</u>zakasz
        </span>
        <span id="bookmark-link" class="inblk">
            Könyvj<u>e</u>lző
        </span>

        <%-- Right aligned, extra --%>
        <span id="short-ref"></span>

        <%-- Left aligned, extra --%>
        <span id="help-link" class="inblk">
            Súgó
        </span>
        <span id="logout-link" class="inblk">
            Kilé<u>p</u>
        </span>
        <span id="view-link" class="inblk">
            Nézet
        </span>
        <span id="profile-link" class="inblk">
            Profil
        </span>

        <%-- Right aligned, extra --%>
        <span id="book-title"></span>
	</div>

	<div id="content" class="autoScroll" tabindex="0">
		<div id="text">
		</div>
		<div id="hits">
		</div>
		<div class="button-rows">
			<div id="text-buttons">
				<span class="btn prev-page">&laquo; Előző (,)</span>
				<span id="sect-down" class="btn">Több betöltése (&#8594;)</span>
				<span class="btn next-page">Következő (.) &raquo;</span>
			</div>
			<div id="hit-buttons">
				<span class="btn prev-page">&laquo; Előző (,)</span>
				<span id="this-sect" class="btn">Teljes szakasz (&#8626;)</span>
				<span class="btn next-page">Következő (.) &raquo;</span>
			</div>
		</div>
	</div>

	<div id="form-content" class="autoScroll">
	</div>

	<div id="fixed-size">
        <div id="searchPop" class="has-button">
            <div style="margin-top: 10px; text-align: center;">
                Keresés:
            </div>
            <div>
                <input type="text" id="searchInput" class="top10 textInput100">
            </div>
            <div style="position: relative;">
                <div id="mru"></div>
            </div>
            <div class="top10 font85">
                Sorrend:&nbsp;
                <input type="radio" name="sort" value="Score" id="score-order" checked>&nbsp;Fontosság
                &nbsp;
                <input type="radio" name="sort" value="Index">&nbsp;Szakasz
            </div>
            <div class="top10 font85" id="search-paging">
                Oldalanként:&nbsp;
                <input type="radio" name="spage" value="1" checked>&nbsp;1
                &nbsp;
                <input type="radio" name="spage" value="10">&nbsp;10
                &nbsp;
                <input type="radio" name="spage" value="20">&nbsp;20
                &nbsp;
                <input type="radio" name="spage" value="50">&nbsp;50
            </div>
            <div class="top10">
                Könyvek:&nbsp;<a href="#" id="search-sect-link">Minden könyv</a>
            </div>
            <div class="top10">
                Bekezdések:&nbsp;<a href="#" id="para-types-link">Minden típus</a>
            </div>
            <div id="search-msg">
            </div>
            <div class="form-row button-row center">
                <button id="searchGo" class="ok-button">Mehet</button>
            </div>
        </div>

        <div id="typesPop" class="has-button">
            <div class="form-row center">
                Keresett bekezdés-típusok
            </div>
            <div class="form-row"><input type="checkbox" name="types-0"> Cím</div>
            <div class="form-row"><input type="checkbox" name="types-1"> Alcím, versszám</div>
            <div class="form-row"><input type="checkbox" name="types-2"> Vers</div>
            <div class="form-row"><input type="checkbox" name="types-3"> Szavankénti fordítás</div>
            <div class="form-row"><input type="checkbox" name="types-4"> Versfordítás</div>
            <div class="form-row"><input type="checkbox" name="types-5"> Magyarázat, törzsszöveg</div>
            <div class="form-row"><input type="checkbox" name="types-6"> Vers a magyarázatban</div>
            <div class="form-row"><input type="checkbox" name="types-7"> Tárgymutató</div>
            <div  class="form-row button-row center">
                <button id="types-all">Mind</button>&nbsp;&nbsp;
                <button id="types-none">Egyik sem</button>
            </div>
            <div class="form-row button-row center">
                <button id="types-go" class="ok-button">Mehet</button>&nbsp;&nbsp;
                <button id="types-cancel" class="cancel-button">Mégsem</button>
            </div>
        </div>

        <div id="sectionPop" class="has-button">
            <div class="goto-sect form-row center">
                Ugrás a választott szakaszra:
            </div>
            <div class="search-sect">
                <div class="form-row font80">
                    Szűrés a könyvek között: vagy az összes könyvből indulsz ki, és kizárod
                    a nem szükségeseket, vagy fordítva: csak a hozzáadott szakaszokban fog keresni.
                </div>
                <div class="form-row">
                    Kiindulás:&nbsp;
                    <input type="radio" name="search-sect-base" value="all">&nbsp;Összes
                    &nbsp;
                    <input type="radio" name="search-sect-base" value="none" checked>&nbsp;Semmi
                </div>
                <div id="search-sect-list" class="form-row hidden">
                    <div id="search-sect-list-btn" class="font85"></div>
                    <div class="font80">Kattints a törléshez. Kiindulás váltása üríti.</div>
                </div>
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
            <div class="search-sect form-row center" style="margin-bottom: 20px;">
                <button id="search-sect-add">Hozzáadás</button>
            </div>
            <div class="form-row center">
                <input type="checkbox" id="section-abbrev">&nbsp;Rövidített címek
            </div>
            <div  class="form-row center">
                <button id="sectionGo" class="ok-button">Mehet</button>
                <span class="search-sect">
                    &nbsp;&nbsp;
                    <button id="search-sect-cancel" class="cancel-button">Mégsem</button>
                </span>
            </div>
        </div>

        <div id="viewPop" class="has-button">
            <div class="top10">
                Betűméret:
            </div>
            <div>
                <input type="number" min="6" max="30" id="viewFontInput" class="top10 textInput100">
            </div>
            <div id="view-msg" class="top10">
            </div>
            <div class="form-row button-row center">
                <button id="viewGo" class="ok-button">Beállítás</button>
            </div>
        </div>

        <div id="menu">
            <div id="menu-short-ref" class="mitem">(Nincs pozíció)</div>
            <div id="menu-search-link" class="mitem"><u>K</u>eres</div>
            <div id="menu-section-link" class="mitem"><u>S</u>zakasz</div>
            <div id="menu-bookmark-link" class="mitem">Könyvj<u>e</u>lző</div>
            <div id="menu-help-link" class="mitem">Súgó</div>
            <div id="menu-logout-link" class="mitem">Kilé<u>p</u>és</div>
            <div id="menu-view-link" class="mitem">Nézet</div>
            <div id="menu-profile-link" class="mitem">Profil</div>
<% if(ri.admin) { %>
    	    <div id="users-link" class="mitem">Felhasználók</div>
<% } %>
    	</div>
    </div>

	<div id="message">
	</div>

	<div id="loading">
	    <div></div>
	</div>

<script>
var pg = <%= ri.ajaxText %>;
</script>
</body>
</html>
