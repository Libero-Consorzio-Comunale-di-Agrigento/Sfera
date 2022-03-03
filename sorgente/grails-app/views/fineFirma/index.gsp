<!DOCTYPE html>
<%@page import="org.zkoss.util.resource.Labels"%>
<html>
	<head>
		<title>${titolo}</title>
		<r:require modules="core" />
		<g:javascript library="jquery" plugin="jquery" />
		<r:layoutResources/>
	</head>
	<body>
		<g:if test="${!finito}">
			<p style="text-align: center; margin-top:200px;"><g:img style="margin-right:5px" dir="images/agsde2/16x16" file="spinner.gif"/>Attendere il completamento della firma ...</p>
			<div style="margin-left: 50px;margin-right: 50px;"  class="progress">
			  <div class="progress-bar progress-bar-warning progress-bar-striped active" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width: 60%">
			    <span class="sr-only">Firma in corso...</span>
			  </div>
			</div>
			<script type="text/javascript">window.location.href = '<g:createLink controller="fineFirma" action="termina" params="${params}" />';</script>
		</g:if>

		<g:if test="${finito}">
			<p style="text-align: center; margin-top:200px;">Firma completata con successo. È ora possibile chiudere la maschera.</p>
			<div style="margin-left: 50px;margin-right: 50px;" class="progress">
			  <div class="progress-bar progress-bar-success" role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width: 100%">
			    <span class="sr-only">Firma completata con successo</span>
			  </div>
			</div>
		</g:if>
	</body>
	
	<r:layoutResources/>
</html>