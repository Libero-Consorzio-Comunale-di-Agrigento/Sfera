<%@page import="grails.util.Environment" %>
<%@page import="it.finmatica.atti.AggiornamentoInCorsoAuthenticationProvider" %>
<html>
	<head>
		<meta name="layout" content="ads-main">
		<title>${message(code: "titoloApplicazione")}</title>

		<script type="text/javascript">
            window.onload = function () {
                document.getElementById("loginForm").onsubmit = function onSubmit(form) {
                    document.getElementById("loginButton").setAttribute("disabled", "disabled");
                }
            }
		</script>
	</head>
	<body>
	<div id='login'>
		<div class='inner'>
			<div class='fheader'><g:message code="springSecurity.login.header"/></div>
			<g:if test='${flash.message}'>
				<div class='login_message'>${flash.message}</div>
			</g:if>

			<g:if test="${AggiornamentoInCorsoAuthenticationProvider.isAggiornamentoInCorso()}">
				<div class="login_message">AGGIORNAMENTO IN CORSO: l'accesso all'applicativo è accessibile solo per l'utente amministratore.</div>
			</g:if>

			<form action='${postUrl}' method='POST' id='loginForm' class='cssform' autocomplete='off'>
			    <sec:ifNotLoggedIn>
					<g:if test="${request.getParameter('j_username') != null}">
					    <div class='login_message'><g:message code="springSecurity.denied.message" /></div>
					</g:if>
			    </sec:ifNotLoggedIn>
				<p>
					<label for='username'><g:message code="springSecurity.login.username.label"/>:</label>
					<input type='text' class='text_' name='j_username' id='username' autofocus />
				</p>
				<p>
					<label for='password'><g:message code="springSecurity.login.password.label"/>:</label>
					<input type='password' class='text_' name='j_password' id='password'/>
				</p>
				<g:if test="${Environment.getCurrent() == Environment.DEVELOPMENT}">
					<p id="remember_me_holder">
						<input type='checkbox' class='chk' name='${rememberMeParameter}' id='remember_me' <g:if test='${hasCookie}'>checked='checked'</g:if>/>
						<label for='remember_me'><g:message code="springSecurity.login.remember.me.label"/></label>
					</p>
				</g:if>
				<p>
				  <input id="loginButton" type='submit' class="submit" value='${message(code: "springSecurity.login.button")}'>
				</p>
			</form>
		</div>
	</div>
	</body>
</html>
