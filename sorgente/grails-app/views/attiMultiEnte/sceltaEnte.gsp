<html>
<head>
	<meta name='layout' content='ads-main'/>
	<title><g:message code="springSecurity.login.title"/></title>
</head>
<body>
	<div id='login'>
		<div class='inner'>
			<div class='fheader'>Seleziona l'ente</div>

			<g:if test='${flash.message}'>
				<div class='login_message'>${flash.message}</div>
			</g:if>

			<g:form controller="attiMultiEnte" method="GET" class="cssform">
				<p>
					<label for='amministrazione'>Amministrazione:</label>
					<g:select class='textAmministrazione_'
							  name="amministrazione"
							  value="${amministrazione}"
					          from="${amministrazioni}"
					          optionValue="descrizione"
					          optionKey="codice"/>
				</p>

				<p>
					<g:hiddenField name="ottica" value="${ottica}"/>
				</p>

				<p>
					<input type='submit' id="submit" value="${labelBottone}"/>
				</p>
			</g:form>
		</div>
	</div>
</body>
</html>
