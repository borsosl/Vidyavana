<%@page import="hu.vidyavana.web.RequestInfo" pageEncoding="UTF-8"%>
<%
RequestInfo ri = (RequestInfo) request.getAttribute("_ri");
ri.check();
%>
<!DOCTYPE html>
<html class="fill">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, height=device-height, user-scalable=no" />
    <title>Pandit</title>
    <link rel="icon" href="/favicon.ico" sizes="16x16 32x32 48x48 64x64 110x110 114x114" type="image/vnd.microsoft.icon" />
    <link rel="stylesheet" type="text/css" href="/css/style.css" />
    <link rel="stylesheet" type="text/css" href="/css/text.css" />
    <script src="/js/lib.js"></script>
    <script src="/js/login.js"></script>
</head>
<body class="fill noScroll login-body">
    &nbsp;
    <div class="login">
        <div class="tabs">
            <div class="tab-ctnr">
                <div id="tab0" class="tab active">
                    Belépés
                </div>
                <div id="tab1" class="tab inactive">
                    Regisztráció
                </div>
            </div>
        </div>
        <div class="form">
            <div>
                <input type="email" id="email" class="top10 textInput100" placeholder="E-mail*">
            </div>
            <div>
                <input type="password" id="password" class="top10 textInput100" placeholder="Jelszó*">
            </div>
            <div class="regitem">
                <input type="password" id="password2" class="top10 textInput100" placeholder="Jelszó újra*">
            </div>
            <div class="regitem">
                <input type="text" id="name" class="top10 textInput100" placeholder="Név">
            </div>
            <div id="loginMsg" class="hidden form-row">
            </div>
            <div class="loginitem form-row button-row center">
                <button id="loginBtn">Belépek</button>
            </div>
            <div class="regitem form-row button-row center">
                <button id="regBtn">Regisztrálok</button>
            </div>
        </div>
    </div>
</body>
</html>
