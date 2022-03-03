grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
grails.project.fork = false
grails.project.dependency.resolver = "maven"
grails.project.war.file = "target/dist/${appName}.war"

grails.project.repos.releases.type = "maven"
grails.project.repos.releases.url = System.getProperty("releasesUrl")
grails.project.repos.releases.username = System.getProperty("username")
grails.project.repos.releases.password = System.getProperty("password")

grails.project.repos.snapshots.type = "maven"
grails.project.repos.snapshots.url = System.getProperty("snapshotsUrl")
grails.project.repos.snapshots.username = System.getProperty("username")
grails.project.repos.snapshots.password = System.getProperty("password")

grails.war.resources = { stagingDir, args ->
    println("Elimino i jar non necessari dal .war")

    // rimuovo il jar dbutils che arriva da svariati plugins.
    delete {
        fileset(dir: "${stagingDir}/WEB-INF/lib") {
            include(name: "finmatica-accesscontrol-*.jar")
            include(name: "finmatica-affarigenerali.jar")
            include(name: "finmatica-cacheutility.jar")
            include(name: "finmatica-dmserverextension.jar")
            include(name: "finmatica-jpdfwriter.jar")
            include(name: "finmatica-jprotocollo.jar")
            include(name: "finmatica-log4jsuite.jar")
            include(name: "finmatica-semafori.jar")
            include(name: "ojdbc*.jar")
            include(name: "DMServer.jar")

            // jar di tomcat inclusi dal plugin dataSource (non si sa perché)
            include(name: "tomcat*")

            // elimino i jar dbutils e jfcutils che mi aspetto ci siano già nella lib di tomcat.
            include(name: "finmatica-dbutils-*.jar")
            include(name: "finmatica-jfcutils-*.jar")

            // elimino i jar per il parsing di xml perché confido che siano già nella "lib" del tomcat.
            // questo serve perché il parse xml in java registra una variabile globale nella jvm per decidere quale libreria usare, se le lib del contesto
            // differiscono da quelle della lib di tomcat, si hanno problemi.
            include(name: "xerces*")
            include(name: "xalan*")
            include(name: "serializer*")
        }
    }

    println "Scarico il jar del configuratore-iter"
    String versioneConfiguratoreIter = "1.3.0.3"
    new File("${stagingDir}/WEB-INF/lib", "configuratore-iter-${versioneConfiguratoreIter}.jar") << new URL("https://nexus.svi.finmatica.local/repository/old-proxy-finmatica-grails-plugins/it/finmatica/grails/plugins/configuratore-iter/${versioneConfiguratoreIter}/configuratore-iter-${versioneConfiguratoreIter}.jar").openStream()
    //new File("${stagingDir}/WEB-INF/lib", "configuratore-iter-${versioneConfiguratoreIter}.jar") << new URL("http://svi-redmine:8081/artifactory/finmatica-grails-plugins/it/finmatica/grails/plugins/configuratore-iter//configuratore-iter-${versioneConfiguratoreIter}.jar").openStream()
}

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
        excludes 'h2'
        excludes 'grails-docs'
        // questo lo escludo perché si porta dietro i jar bouncycastle che fanno casino con quelli del jsign
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

	repositories {
		inherits true // Whether to inherit repository definitions from plugins
		mavenLocal()
		mavenRepo("https://nexus.finmatica.it/repository/maven-public")
		mavenRepo("https://nexus.finmatica.it/repository/finmatica-snapshots") {
			updatePolicy 'always'
		}
		mavenRepo("https://repo.grails.org/grails/core")
		mavenRepo("https://repo.grails.org/grails/plugins")
//		grailsPlugins()
//		grailsHome()
//		grailsCentral()
		mavenCentral()
	}
	
	dependencies {

        compile 'org.codehaus.groovy:groovy-all:2.4.15'

        // jar ZK Enterprise da artifactory.
        String zkVersion = "6.5.7.1"
        compile "org.zkoss.zkee:zcommon:${zkVersion}"
        compile "org.zkoss.zkee:zel:${zkVersion}"
        compile "org.zkoss.zkee:zhtml:${zkVersion}"
        compile "org.zkoss.zkee:zk:${zkVersion}"
        compile "org.zkoss.zkee:zkbind:${zkVersion}"
        compile "org.zkoss.zkee:zkex:${zkVersion}"
        compile "org.zkoss.zkee:zkmax:${zkVersion}"
        compile "org.zkoss.zkee:zkplus:${zkVersion}"
        compile "org.zkoss.zkee:zml:${zkVersion}"
        compile "org.zkoss.zkee:zul:${zkVersion}"
        compile "org.zkoss.zkee:zweb:${zkVersion}"

        // nuova dipendenza di grails 2.5.3 per il plugin tomcat.
        provided 'commons-dbcp:commons-dbcp:1.4'

        // per l'upload ftp per treviso:
        runtime "commons-net:commons-net:3.3"

        // dipendenze per la build:
        build "org.apache.ant:ant-commons-net:1.8.3"

        // questa dipendenza serve per fare il test dell'installante
        build('org.apache.ant:ant-jsch:1.9.12')

        // dipendenze di ads
        provided "it.finmatica:finmatica-jfcutils:1.20"
        // dipendenze di ads
        provided "it.finmatica:finmatica-dbutils:5.2.1.1"
        provided "it.finmatica:finmatica-accesscontrol:1.21"
        provided "it.finmatica:finmatica-cim:2.0"
        provided "javax.mail:mail:1.4"
        provided "javax.activation:activation:1.1"
        provided "oracle:ojdbc14:10.2.0.5.0"
        provided "it.finmatica:DMServer:3.5.0"

        // dipendenze per integrazione docer
        runtime "it.finmatica.docer:finmatica-docer-jdms-cxf:1.0"
        runtime "it.finmatica.docer:finmatica-docer-cxf:1.0"
        runtime "it.finmatica.docer:finmatica-docer-syncro:1.0"

        // questi servono per evitare che il parsing di xml usi quella merda di xmlparserv2 disseminato nelle
        // shared lib di tutti i tomcat dal cliente.
        runtime "xerces:xercesImpl:2.11.0"
        runtime "xml-apis:xml-apis:1.4.01"

        // questa dipendenza serve per la verifica firma
        runtime "javax.xml:jaxrpc-api:1.1"

        // utility ads per jdms
        compile("it.finmatica:finmatica-japps-util:1.6.1") {
            excludes 'finmatica-dbutils'
        }

        // integrazioni varie con webservice:
        compile "it.finmatica.atti:atti-integrazioni-ws:2.4.2.4"
        compile "it.finmatica:finmatica-jsign:3.8.5"
        compile "org.freemarker:freemarker:2.3.27-incubating"

        // integrazione con cfa
        compile "it.finmatica.grails.plugins:atti-cf-integration:1.18.0.1"

        test "org.grails:grails-datastore-test-support:1.0.2-grails-2.4"
        test "com.h2database:h2:1.4.177"

        // Dipendenze prese dai jar della lib su svn ma con versione aggiornata
        // TODO: verificare versioni
        provided("it.finmatica.ag.protocollo:finmatica-jprotocollo:3.5.1.3") {
            excludes 'xmlparserv2'
        }
    }

    plugins {
        build ":tomcat:7.0.55.3"
        build ":release:3.1.2"
        build "it.finmatica.grails.plugins:grails-ci-plugin:1.5"
        build 'it.finmatica.grails.plugins:ads-installer:1.4'

        runtime ":hibernate:3.6.10.19"
        runtime ":jquery:1.11.1"
        runtime ":resources:1.2.14"
        compile ':cache:1.1.8'

        // console web accessibile da http://localhost:8080/Atti/console per poter fare delle prove senza dover creare perforza un Controller.
        runtime ":console:1.5.1"

        //compile ":excel-import:1.0.0"	 											// per grails 2.2.4
        compile "it.finmatica.grails.plugins:excel-import:2.0.0"

        // plugin zk importato
        compile("it.finmatica.grails.plugins:finmatica-zk:2.5.1.4")
        compile("it.finmatica.grails.plugins:confapps:1.1")

        compile("org.grails.plugins:spring-security-core:2.0.0") {
            excludes "ehcache"
        }
        compile("it.finmatica.grails.plugins:dto:1.3.8")

		compile ("it.finmatica.grails.plugins:amministrazione-database:2.2.3.4.8")
		compile ("it.finmatica.grails.plugins:struttura-organizzativa:2.6.1.4")
		compile ("it.finmatica.grails.plugins:gestione-iter:1.4.3")
		compile ("it.finmatica.grails.plugins:gestione-testi:1.6.29") {
			excludes "finmatica-jsign"
		}
		compile ("it.finmatica.grails.plugins:firma-digitale:3.8.4")
		compile ("it.finmatica.grails.plugins:web-scan:1.1")

        // plugin di monitoraggio risorse (vedere all'url: http://localhost:8080/Atti/monitoring )
        runtime("org.grails.plugins:grails-melody:1.58.0") { excludes 'itext' }

        compile ":hibernate-filter:0.3.2"

        // per usare il code coverage:
        // grails test-app -coverage
        test "org.grails.plugins:code-coverage:2.0.3-3"
        test "org.grails.plugins:plastic-criteria:1.6.5"

        compile ":rest:0.8"

    }
}

coverage {
    appendCoverageResults = true

    enabledByDefault = false
    xml = true

    exclusions = ["**/*closure*.class", "**/*DTO.class"]

    // list of directories to search for source to include in coverage reports
    sourceInclusions = ['grails-app/actions', 'grails-app/viewmodels', 'src/dto']
}

finmatica.installante = {
    // directory relativa al proprio progetto dove trovare i sorgenti dell'installante.
    // Il template dell'installante verrà scompattato qui.
    // default: "installante"
    dir = "installante"

    // nome del file installante che verrà prodotto (relativo alla directory "target")
    // default: "[grailsAppName].zip"
    file = "agsde2.zip"

    // path dove copiare il war in fase di creazione dell'installante (relativa alla directory dei sorgenti dell'installante)
    // default: "commons/webapp/${grailsAppName}.war"
    warFile = "commons/webapp/Atti.war"

    // versione dell'installer da utilizzare
    // default: "1.1.+"
    version = "1.1.+"

    // configurazione per il test dell'installante.
    test {
        // configurazione per collegarsi via ssh
        server.host = ""
        server.username = ""
        server.password = ""
        server.javaHome = "/usr/java/jdk1.8.0_221"

        // configurazione per il test di aggiornamento da una versione arbitraria
        update {
            groupId = "it.finmatica.pa.ag"
            artifactId = "Atti"
            version = "2.5.6.0"
        }

        // configurazione che verrà messa nel file config.properties nel lancio dell'installante.
        config {
            global.tomcat.home = ""

            // specificare user e password di system in modo da poter creare la tablespace
            global.db.system.url = ""
            global.db.system.username = ""
            global.db.system.password = ""

            // è possibile modificare il codice di istanza in modo da installare più volte sullo stesso server.
            progetto.istanza.codice = "AGSDE2"

            // è possibile modificare qualsiasi parametro di installazione
            global.db.agsde2.username = "AGSDE2"
            global.db.agsde2.password = ""
            // imposto una dimensione dalla tablespace più piccola rispetto al default
            global.db.agsde2.tablespace.size = 200
        }
    }

    applyDb {
        config {
            // sostituire i parametri con i propri user/password/url:
            global.db.target.url = ""
            global.db.target.username = "AGSDE2"
            global.db.target.password = ""

            // configurazione per AD4:
            global.db.ad4.username = "AD4"
            global.db.ad4.password = ""

            // configurazione per System (necessario per creare tablespace/user-oracle):
            global.db.system.username = ""
            global.db.system.password = ""
        }
    }
}


//grails.plugin.location.'amministrazione-database' = "../AmministrazioneDatabase"
//grails.plugin.location.'struttura-organizzativa' 	= "../StrutturaOrganizzativa"
//grails.plugin.location.'gestione-iter' 				= "../GestioneIter"
//grails.plugin.location.'dto' = "../Dto"
//grails.plugin.location.'gestione-testi' = "../GestioneTesti"
//grails.plugin.location.'firma-digitale' = "../FirmaDigitale"
//grails.plugin.location.'finmatica-zk' = "../FinmaticaZk"

//grails.plugin.location.'atti-cf' = "../AttiCf"

