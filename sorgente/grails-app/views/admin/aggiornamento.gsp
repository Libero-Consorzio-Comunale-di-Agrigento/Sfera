<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Insert title here</title>
		<style type="text/css">
			select {
				width: 100%;
			}
			label > select {
				height: 200px;
			}
			input {
				padding: 5px;
				margin: 4px;
			}
		</style>
	</head>
	<body>

		<g:if test="${flash.message}">
		  <div class="message" style="display: block">${flash.message}</div>
		</g:if>

		<g:form controller="admin" action="submit">
			<label>Funzionalità:
			<g:actionSubmit value="Aggiorna Tutto" action="aggiornaAtti" />

			<g:actionSubmit value="Aggiorna Azioni" action="aggiornaAzioni" />
			<g:actionSubmit value="Elimina Azioni vecchie" action="eliminaAzioni" />
			<g:actionSubmit value="Aggiorna Impostazioni" action="aggiornaImpostazioni" />
			<g:actionSubmit value="Aggiorna Regole di Calcolo" action="aggiornaRegoleCalcolo" />
			<g:actionSubmit value="Aggiorna Tipi Modello Testo" action="aggiornaTipiModelloTesto" />
			</label>
			<br/>
			<label style="padding-right: 57px">Job:</label>
			<g:actionSubmit value="Attiva JOB Notturno" action="attivaJob" />
			<g:actionSubmit value="Attiva JOB Conservazione Automatica" action="attivaJobConservazioneAutomatica" />
			<g:actionSubmit value="Attiva JOB Aggiorna Stati Conservazione" action="attivaJobAggiornaConservazione" />
            <g:actionSubmit value="Attiva JOB Notifiche" action="attivaJobNotifiche" />
		</g:form>
		<g:uploadForm controller="admin" action="runUpdate">
			<label>Aggiornamenti:
				<g:actionSubmit value="Controlla Aggiornamenti" action="checkForUpdates" />
				<g:if test="${aggiornamentiDisponibili}"><g:actionSubmit value="Installa Aggiornamenti" action="runUpdates" /></g:if>
				<input type="file" 	name="patch" />
				<input type="submit" value="Carica Aggiornamento" />
			</label>
	    </g:uploadForm>

		<!-- Correggi azioni -->
		<g:form controller="admin">
			<label>Azioni Vecchie in uso:
				<g:select name="azioneVecchia" from="${azioniVecchie}" multiple="true" optionKey="id" optionValue="nome" />
			</label>
			<g:textField name="filtroAzioniNuove" value="${filtroAzioniNuove}"/>
			<g:actionSubmit value="Cerca" action="cercaAzioniNuove"/>
			<g:select name="azioneNuova" from="${azioniNuove}" optionKey="id" optionValue="nome"/>
			<g:actionSubmit value="Sostituisci Vecchie Azioni con Nuova" action="sostituisciVecchioConNuovo"/>
		</g:form>

		<h3>Test:</h3>
		<g:form controller="admin" action="submit">
		<!--g:actionSubmit value="Test Integrazione JWorklist" action="testIntegrazioneJWorklist" /-->
		<!--g:actionSubmit value="Test Invio Email Certificata" action="testInvioEmailCert" /-->
		<!--g:actionSubmit value="Test Casa di Vetro" action="testCasaDiVetro" /-->
		<label>invia email a: <g:textField name="email" /></label>
		<g:actionSubmit value="Test Invio Email" action="testInvioEmail" />
		</g:form>


		<!--g:form controller="admin" action="submit"-->
			<!--g:actionSubmit value="Aggiorna SFERA" action="aggiornaSfera" /-->
		<!--/g:form-->

	</body>
</html>