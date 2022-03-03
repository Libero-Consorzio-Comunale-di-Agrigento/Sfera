<!doctype html>
<html>
	<head>
		<meta name="layout" content="ads-main"/>
		<title>Configurazione Installazione Atti</title>
		<style type="text/css" media="screen">
			#status {
				background-color: #eee;
				border: .2em solid #fff;
				margin: 2em 2em 1em;
				padding: 1em;
				width: 12em;
				float: left;
				-moz-box-shadow: 0px 0px 1.25em #ccc;
				-webkit-box-shadow: 0px 0px 1.25em #ccc;
				box-shadow: 0px 0px 1.25em #ccc;
				-moz-border-radius: 0.6em;
				-webkit-border-radius: 0.6em;
				border-radius: 0.6em;
			}

			.ie6 #status {
				display: inline; /* float double margin fix http://www.positioniseverything.net/explorer/doubled-margin.html */
			}

			#status ul {
				font-size: 0.9em;
				list-style-type: none;
				margin-bottom: 0.6em;
				padding: 0;
			}

			#status li {
				line-height: 1.3;
			}

			#status h1 {
				text-transform: uppercase;
				font-size: 1.1em;
				margin: 0 0 0.3em;
			}

			#page-body {
				margin: 2em 1em 1.25em 18em;
			}

			h2 {
				margin-top: 1em;
				margin-bottom: 0.3em;
				font-size: 1em;
			}

			p {
				line-height: 1.5;
				margin: 0.25em 0;
			}

			#controller-list ul {
				list-style-position: inside;
			}

			#controller-list li {
				line-height: 1.3;
				list-style-position: inside;
				margin: 0.25em 0;
			}

			@media screen and (max-width: 480px) {
				#status {
					display: none;
				}

				#page-body {
					margin: 0 1em 1em;
				}

				#page-body h1 {
					margin-top: 0;
				}
			}
		</style>
	</head>
	<body>
		<h1>Completa l'installazione</h1>
		<%--
		-- cose da fare in completamento installazione:
		1) scegliere l'ente di installazione, scrivere le impostazioni.
		2) installare i dizionari, flussi,modelli di default
		3) installare le azioni di default.
		4) configurare i primi parametri: url dei vari webservices, integrazioni principali.
			- protocollo
			- contabilità
			- casa di vetro
			- jworkflow
			- email

		-- cose da testare dopo l'installazione:
			- i vari webservices/integrazioni
			- firma
			- edita-testo
			- trasformazione odt/pdf/docx --%>

		<g:form action="" method="GET" class="cssform">
			<p>
				<label for='amministrazione'>Scegli l'Ottica ISTITUZIONALE per cui installare gli Atti (di solito è quella con codice IST):</label>
				<g:select class='textAmministrazione_'
						  name="ottica"
						  value=""
				          from="${ottiche}"
				          optionValue="${{it.amministrazione.codice+" ("+it.codice+")"}}"
				          optionKey="codice"/>
				<g:hiddenField name="installazione" value="1"/>
			</p>

			<p>
				<input type="submit" id="submit" value="Completa Installazione"/>
			</p>
		</g:form>
	</body>
</html>
