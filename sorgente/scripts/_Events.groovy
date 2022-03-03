import groovy.xml.*

// Riscrivo il "MultiEnteFilter" in modo da evitare di farlo scattare sui webservice.
// altrimenti quello che succede è che dopo la basic-authentication, parte questo filtro che rimanda alla pagina di selezione dell'ente.
// ovviamente da webservice questo non va bene e siccome il plugin StrutturaOrganizzativa non prevede questa opzione,
// in questo modo vado a sovrascriverne il comportamento. Vedasi l'implementazione di it.finmatica.atti.springsecurity.MultiEnteFilter
eventWebXmlEnd = { String filename ->
	String content = webXmlFile.text
	def xml = new XmlParser().parseText(webXmlFile.text)
	def filter = xml.filter.find { it.'filter-class'.text() == 'it.finmatica.so4.filters.MultiEnteFilter' }
	filter.'filter-class'[0].value = 'it.finmatica.atti.springsecurity.MultiEnteFilter'
	
	FileWriter fileWriter = new FileWriter(webXmlFile)
	XmlNodePrinter nodePrinter = new XmlNodePrinter(new PrintWriter(fileWriter))
	nodePrinter.preserveWhitespace = true
	nodePrinter.print(xml)
}


eventWarEnd = {

	println "event war end"

}