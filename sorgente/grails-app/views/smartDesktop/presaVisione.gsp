<!DOCTYPE html>
<%@page import="org.zkoss.util.resource.Labels"%>
<html>
    <head>
        <title>Presa Visione</title>
        <script>
            window.onunload = refreshParent;
            function refreshParent() {
                window.opener.location.reload();
            }

            function closeMe()
            {
                window.close();
            }
        </script>
    </head>
    <body onload="closeMe()">
        <table>
                <tr>
                    <th width="20%">Atto</th>
                    <th width="20%">Proposta</th>
                    <th width="50%">Oggetto</th>
                    <th></th>
                </tr>
                <g:each in="${documenti}" var="documento">
                    <tr>
                        <g:if test="${documento.doc != null}">
                            <td><g:if test="${documento.doc.numeroAtto}">${documento.doc.numeroAtto}/${documento.doc.annoAtto}</g:if></td>
                            <td><g:if test="${documento.doc.numeroProposta}">${documento.doc.numeroProposta}/${documento.doc.annoProposta}</g:if></td>
                            <td>${documento.doc.oggetto}</td>
                            <td><image src="${documento.result? resource(dir:'/images/agsde2/22x22/', file: 'viewok.png') : resource(dir: '/images/agsde2/22x22/', file:'cancel.png')}"
                                   title="${documento.result? 'Presa visione effettuata' : 'Errore durante presa visione'}"/></td>
                        </g:if>
                    </tr>
                </g:each>
        </table>
        </div>
    </body>
</html>
