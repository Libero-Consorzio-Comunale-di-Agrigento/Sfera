import it.finmatica.grails.plugins.confapps.ConfappsHelper

// configuro Grails specificando quali configurazioni aggiuntive caricare
grails.config.locations = ["file:${ConfappsHelper.getConfappsFile("DataSource.groovy").getAbsolutePath()}", "file:${ConfappsHelper.getConfappsFile("Config.groovy").getAbsolutePath()}"]

// Se utilizzo ZK, posso aggiungere la configurazione esterna (ad es. per le label):
org.zkoss.lang.Library.setProperty("org.zkoss.zk.config.path", "file://" + ConfappsHelper.getConfappsFile("zk-production.xml").toURI().toURL().getPath());

// log4j configuration
log4j = {
    appenders {
        rollingFile name: 'file', maxFileSize: 1048576, maxBackupIndex: 10, file: "logs${ConfappsHelper.getContextPath().toLowerCase()}.log"
    }

    root {
        error 'stdout'
    }

    'null' name: 'stacktrace'

    debug additivity: false, file: ['it.finmatica.cim'
                                    , 'it.finmatica.segreteria'
                                    , 'it.finmatica.jdmsutil.data'
                                    , 'it.finmatica.atti'
                                    , 'atti', 'dizionari', 'odg', 'system', 'afc'
                                    , 'grails.app.services.it.finmatica'
                                    , 'grails.app.jobs.it.finmatica'
                                    , 'grails.app.actions']
}

grails.project.groupId = 'it.finmatica.pa.ag' // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
    all:           '*/*',
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
	xml:           ['text/xml', 'application/xml'],
	jar:		   'application/java-archive'
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/robots.txt']

grails.resources.modules = {
    core {
        dependsOn 'jquery'
        defaultBundle 'ui'
        resource url:'/js/bootstrap.min.js'
        resource url:'/css/bootstrap.min.css' 
    }
}

// questa impostazione serve per far risolvere le risorse statiche presenti nei plugin jar (atti-cf e configuratore-iter)
grails.resources.resourceLocatorEnabled = true

grails.spring.bean.packages = ["it.finmatica.atti.config"]

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

// tutti i .save .delete .merge etc comporteranno una failOnError:true e flush:true
grails.gorm.failOnError	= true
grails.gorm.autoFlush   = true

environments {
	development {
		grails.logging.jul.usebridge = true;
		grails.dbconsole.enabled 	 = false;
	}
	production {
		grails.logging.jul.usebridge = false;
		grails.dbconsole.enabled 	 = false;
	}
}

// Aggiunti dal AmministrazioneDatabase Plugin:
grails.plugins.amministrazionedatabase.jndiAd4 		= 'jdbc/ad4' // jndi di connessione all' applicativo
grails.plugins.amministrazionedatabase.jndiTarget 	= 'jdbc/agsde2' // jndi di connessione all' applicativo
grails.plugins.amministrazionedatabase.modulo 		= 'AGSDE2' // codice del MODULO AD4 dell'applicativo
grails.plugins.amministrazionedatabase.istanza 		= 'AGSDE2' // codice dell'ISTANZA AD4 dell'applicativo

// Configurazione di Spring-Security
grails.plugin.springsecurity.useSwitchUserFilter			= true
grails.plugin.springsecurity.rejectIfNoRule 				= true
grails.plugin.springsecurity.fii.rejectPublicInvocations 	= true
grails.plugin.springsecurity.securityConfigType 			= 'InterceptUrlMap'

// web.xml e spring usano due standard diversi: web.xml vuole "un solo asterisco" per indicare "ogni path" mentre spring ne vuole "due".
// per questa ragione, qui e di seguito dichiaro un solo asterisco, ma quando interagisco con spring ne aggiungo uno in più.
grails.plugins.amministrazionedatabase.restrictUrlPatterns = ["/*"]
grails.plugin.springsecurity.interceptUrlMap = [:]

grails.plugins.amministrazionedatabase.publicUrlPatterns = ["/static/*", "/login/*", "/docEr/*", "/aggiornaImpostazioni", "/plugins/*", "/firmaDigitale/*", "/fineFirma/*", "/webScan/*", "/zkau/*"]
grails.plugins.amministrazionedatabase.publicUrlPatterns.each {
	if (it.endsWith("*")) {
		it += "*"
	}
	grails.plugin.springsecurity.interceptUrlMap << [(it):['permitAll']]
}

grails.plugins.amministrazionedatabase.publicUrlPatterns << '/services/*'

grails.plugin.springsecurity.useBasicAuth 			= true
grails.plugin.springsecurity.basic.realmName 		= "Web Service Sfera"

grails.plugin.springsecurity.filterChain.chainMap = [
	'/services/**': 'securityRequestHolderFilter,securityContextPersistenceFilter,logoutFilter,authenticationProcessingFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousAuthenticationFilter,basicExceptionTranslationFilter,customFilterInvocationInterceptor',	// lascio la basic authentication sui webservice ma tolgo i filtri non necessari
	'/**': 			'JOINED_FILTERS,-basicAuthenticationFilter,-basicExceptionTranslationFilter'								// tolgo la basic-authentication da tutte le pagine
]

grails.plugin.springsecurity.interceptUrlMap << [
	'/services/*wsdl': 					["permitAll"], 																	// il wsdl dei webservice deve essere pubblico.
	'/services/**':    					["hasAnyRole('AGSDE2_AGDWS',   'AGDWS')"],
	'/logout':							["isAuthenticated()"],
	'/admin':							["hasAnyRole('AGSDE2_AGDAMMI', 'ROLE_PREVIOUS_ADMINISTRATOR')"], 	// ruoli di accesso: il primo è la concatenazione tra MODULO_RUOLO, il secondo è solo il RUOLO
	'/console': 	  					["hasAnyRole('AGSDE2_AGDAMMI', 'ROLE_PREVIOUS_ADMINISTRATOR')"], 	// ruoli di accesso: il primo è la concatenazione tra MODULO_RUOLO, il secondo è solo il RUOLO
	'/assistenza':						["hasAnyRole('AGSDE2_AGD', 'AGSDE2_AGDAMMI', 'ROLE_PREVIOUS_ADMINISTRATOR')"], 	// ruoli di accesso: il primo è la concatenazione tra MODULO_RUOLO, il secondo è solo il RUOLO
	'/switchUser' :   					["hasAnyRole('AGSDE2_AGDAMMI', 'ROLE_PREVIOUS_ADMINISTRATOR')"],
	'/configuratoreiter/**' :   		["hasAnyRole('AGSDE2.CFG_AGDCONF', 'ROLE_PREVIOUS_ADMINISTRATOR')"],
	'/j_spring_security_switch_user': 	["hasAnyRole('AGSDE2_AGDAMMI', 'ROLE_PREVIOUS_ADMINISTRATOR')"],
	"/j_spring_security_exit_user":		["permitAll"],
	"/**":["hasAnyRole('AGSDE2_AGD', 'AGSDE2_AGDAMMI')"]
]

// abilito la console anche per l'ambiente di produzione (così dalla versione 1.5.0 del plugin console)
grails.plugin.console.enabled = true

// Aggiunti dal AnagrafeSoggetti Plugin:
grails.plugins.anagrafesoggetti.jndiAs4 	= 'jdbc/as4' // jndi di connessione ad AS4
grails.plugins.anagrafesoggetti.utenteBatch = 'AGSDE2' 	// utente da usare in update batch
grails.plugins.anagrafesoggetti.competenza 	= 'AGSDE2' 	// competenza da utilizzare per l'aggiornamento dei soggetti

// Aggiunti dal StrutturaOrganizzativa Plugin:
grails.plugins.strutturaorganizzativa.jndiSo4 		= 'jdbc/so4' // jndi di connessione ad SO4
grails.plugins.strutturaorganizzativa.urlSceltaEnte = '/multiEnte' // url a cui redirigere per la scelta dell'ente.

// Aggiunti dal StrutturaOrganizzativa Plugin:
grails.plugins.strutturaorganizzativa.multiEnte.abilitato     = true // indica se abilitare o meno il multi-ente.
grails.plugins.strutturaorganizzativa.multiEnte.nomeFiltro    = 'multiEnteFilter' // nome del filtro hibernate da abilitare.
grails.plugins.strutturaorganizzativa.multiEnte.nomeParametro = 'enteCorrente' // url a cui redirigere per la scelta dell'ente.

// configurazione del plugin dto:
grails.plugins.dto.suffix = "DTO"
grails.plugins.dto.exclude = ["it.finmatica.so4.struttura.So4Componente"
							, "it.finmatica.so4.struttura.So4RuoloComponente"
							, "it.finmatica.so4.struttura.So4AttrComponente"
							, "it.finmatica.atti.commons.TokenIntegrazione"
							, "it.finmatica.atti.cf"
							, "it.finmatica.grails.firmadigitale.FirmaDigitaleBlobDaFirmareDTO"]

grails.plugins.dto.packageMappings = ["it.finmatica.atti" : "it.finmatica.atti.dto"
									, "it.finmatica.ad4"  : "it.finmatica.ad4.dto"
									, "it.finmatica.as4"  : "it.finmatica.as4.dto"
									, "it.finmatica.so4"  : "it.finmatica.so4.dto"
									, "it.finmatica.gestioneiter"  : "it.finmatica.gestioneiter.dto"
									, "it.finmatica.gestionetesti.reporter"  : "it.finmatica.gestionetesti.reporter.dto"
									, "it.finmatica.gestionetesti.lock"  : "it.finmatica.gestionetesti.lock.dto"
									]

grails.plugins.gestionetesti.reporter.asposeFullFonts=true

autoupdate.repositories = ["ftp://<utente>:<pwd>@<ftp-host>/<path>/SFERA"]

// configurazione invio email in caso di errori nel job notturno:
atti.emailProblemi = ['segreteria@ads.it']
// descrizione aggiuntiva inserita nell'oggetto della mail di errore
atti.oggettoEmailProblemi=""

// configurazione per i caratteri word non supportati che vengono sostituiti
atti.mappaCaratteri = [ [search:"′", replace:"'"],
						[search:"‛", replace:"'"],
						[search:"‘", replace:"'"],
						[search:"’", replace:"'"],
						[search:"“", replace:"\""],
						[search:"‟", replace:"\""],
						[search:"‴", replace:"\""],
						[search:"”", replace:"\""],
						[search:"–", replace:"-"]
]

atti.caratteri.allegato='àèéìòù'
