import groovy.xml.XmlUtil

includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsClasspath")
includeTargets << grailsScript("_GrailsWar")
includeTargets << grailsScript("_GrailsPackage")
includeTargets << grailsScript("_GrailsArgParsing")

includeTool << gant.tools.Ivy

target (main: "Crea una nuova build") {
	if (argsMap.params.contains("release")) {
		depends (release)
	} else if (argsMap.params.contains("ftp")) {
		depends (ftp)
	} else if (argsMap.params.contains("creaInstallante")) {
		depends (creaInstallante)
	} else if (argsMap.params.contains("patch")) {
		depends (creaPatch)
	} else if (argsMap.params.contains("publishPatch")) {
		depends (publishPatch)
	} else if (argsMap.params.contains("creaInstallanteCompleto")) {
		depends(creaInstallanteCompleto)
	} else if (argsMap.params.contains("creaInstallanteNonRilasciato")) {
		depends(creaInstallanteNonRilasciato)
	} else {
		depends (classpath, buildInfo, war)
	}
}

target (creaPatch: "Crea la Patch di versione") {
	// verifico che ci sia il war:
	if (!new File("target/dist/Atti.war").exists()) {
		ant.fail ("Non ho trovato il war! Prima bisogna lanciare grails prod build")
		return
	}

	String version = metadata.'app.version'
	println "Creo la patch per la versione: ${version}"

	// elimino il file di patch se già presente
	ant.delete (file:"target/dist/${version}.zip")

	println "Leggo i file da includere nella patch dal changelog.txt"
	def patternsToInclude = ["**/application.properties"]
	boolean stop = false
	boolean readLine = false
	def changelog = new File("changelog.txt")
	changelog.eachLine { line ->
		if (line.startsWith("----")) {
			stop = true
			readLine = false
			return false
		}

		if (readLine) {
			if (line.trim().length() > 0) {
				patternsToInclude << line
			}
			return true
		}

		if (stop) {
			return false
		}

		if (line.startsWith("File modificati:")) {
			readLine = true
			return true
		}
	}

	ant.mkdir (dir:"target/work/temp/patch")
	ant.unzip (src:"target/dist/Atti.war", dest:"target/work/temp/patch") {
		patternset {
			patternsToInclude.each { pattern ->
				include (name:pattern)
			}
		}
	}

	ant.zip (destfile:"target/dist/${version}.zip", basedir:"target/work/temp/patch")
	ant.delete(dir:"target/work/temp/patch")
	println "Patch target/dist/${version}.zip creata."
}

target (buildInfo: "Crea le informazioni della build") {
	String version 		= metadata.'app.version'
	String buildVersion = metadata.'app.buildVersion'
	def buildNumber 	= metadata.'app.buildNumber'

	if (!buildNumber) {
		buildNumber = 0
	} else {
		buildNumber = Integer.parseInt(buildNumber)
	}

	buildNumber += 1;

	metadata.'app.buildVersion' = version;
	metadata.'app.buildNumber'  = buildNumber.toString();
	metadata.'app.buildTime'    = new Date().format("dd/MM/yyyy HH:mm:ss");

	metadata.persist();

	println ("Inizio la build #$buildNumber per la versione $version")
}

target (creaInstallante: "Crea l'installante") {
	println "Creo l'installante"
	String appVersion		 = metadata.'app.version'
	String dirTplInstallante = "install/installante"
	String dirTargetTmp		 = "target/work/temp/installante"
	String dirTargetWar		 = "${dirTargetTmp}/commons/AGSDE2"
	String warFile			 = "target/dist/Atti.war"
	String zipTarget		 = "target/dist/agsde2.zip"

	// elimino la directory target
	ant.delete(dir:dirTargetTmp)
	ant.delete(file:zipTarget)
	ant.mkdir(dir:dirTargetTmp)

	// copio il template dell'installante nella directory target
	ant.copy (todir:dirTargetTmp) {
		fileset (dir:dirTplInstallante)
	}

	// copio il war degli atti
	ant.copy (file:warFile, todir:dirTargetWar)

	// cambio il numero di versione
	Properties props = new Properties()
	File propsFile = new File("${dirTargetTmp}/config.properties");
	props.load(propsFile.newDataInputStream())
	props.setProperty('versione', appVersion.split('\\.').take(3).join('.')+".0")
	props.store(propsFile.newWriter(), null)

	// zippo il tutto
	ant.zip(destfile:zipTarget, basedir:dirTargetTmp)

	println "Installante creato in: ${zipTarget}"
}

target (publishPatch: "Pubblica la Patch su FTP") {
	String appVersion	= metadata.'app.version';
	String majorVersion = appVersion.substring(0, appVersion.lastIndexOf(".")+1)

	File updatesXml = new File("target/work/temp/updates.xml")
	updatesXml.delete()

	// controllo che ci sia il file di patch (se non c'è, esco con errore)
	File patchFile = new File("target/dist/${appVersion}.zip")
	if (!patchFile.exists()) {
		ant.fail(message:"Non ho trovato il file di patch: ${patchFile.absolutePath}")
		return
	}

	// tento subito di creare la directory se non esiste già:
	ant.ftp (server:"ftp.ads.it", userid:"genftp", password:"", remotedir:"passa/sasdo/SFERA", binary:"false", action:"mkdir", ignoreNoncriticalErrors:"true")

	// scarico il file updates.xml da ftp://ftp.ads.it/passa/sasdo/SFERA/updates.xml
	ant.ftp (server:"ftp.ads.it", userid:"genftp", password:"", remotedir:"passa/sasdo/SFERA", action:"get", verbose:"yes", skipFailedTransfers:"true") {
		fileset (dir:"target/work/temp") {
			include (name:"updates.xml")
		}
	}

	// se non presente, ne creo uno nuovo con il n. di versione
	if (!updatesXml.exists()) {
		updatesXml << '<?xml version="1.0" encoding="UTF-8"?><updates></updates>'
	}

	// lo modifico con il nuovo n. di versione
	def xml = new XmlParser().parse(updatesXml)

	// cerco il n. di versione
	def node = xml.update.find {  it.@version.startsWith(majorVersion) }

	if (node == null) {
		xml.appendNode("update", [version:appVersion])
	} else {
		node.@version = appVersion
	}
	// svuoto il file
	updatesXml.delete()
	updatesXml << XmlUtil.serialize(xml)

	// carico la patch su ftp
	ant.ftp (server:"ftp.ads.it", userid:"genftp", password:"", remotedir:"passa/sasdo/SFERA", binary:"false", action:"put") {
		fileset (dir:"target/dist") {
			include (name:"${appVersion}.zip")
		}
	}

	// carico il file updates.xml
	ant.ftp (server:"ftp.ads.it", userid:"genftp", password:"", remotedir:"passa/sasdo/SFERA", binary:"false", action:"put") {
		fileset (dir:"target/work/temp") {
			include (name:"updates.xml")
		}
	}
}

target (creaInstallanteCompleto: "Crea lo zip con anche agsvis") {
	String version = (metadata.'app.version').split("\\.").take(3).join(".")

	// ottengo l'installer.
	println "Scarico l'installer:"
	ivy.settings(file: "scripts/ivy-settings.xml")
	new File("target/work/temp/adsinstaller/downloads").mkdirs()
	ivy.retrieve(organisation:"it.finmatica.pa.ag", module:"finmatica-adsinstaller", revision:"1.1.40", type:"jar", inline:true, pattern:"target/work/temp/adsinstaller/AdsInstaller.jar")
	ivy.retrieve(organisation:"it.finmatica.pa.ag", module:"atti-visualizzatore",    revision:"${version}.+", type:"zip", inline:true, pattern:"target/work/temp/adsinstaller/downloads/agsde2-vis.zip")
	ivy.retrieve(organisation:"it.finmatica.pa.ag", module:"atti",       			 revision:"${version}.+", type:"zip", inline:true, pattern:"target/work/temp/adsinstaller/downloads/agsde2.zip")

	ant.copy (file:"target/dist/agsde2.zip", todir:"target/work/temp/adsinstaller/downloads")

	ant.copy (file:"scripts/config.properties", todir:"target/work/temp/adsinstaller")
	ant.zip  (destfile:"target/dist/installante.zip", basedir:"target/work/temp/adsinstaller")
	ant.delete (dir:"target/work/temp")
	println "Installante creato: target/dist/installante.zip"
}

target (creaInstallanteNonRilasciato: "Crea lo zip di installazione con l'installer appena creato") {
	String version = (metadata.'app.version').split("\\.").take(3).join(".")

	// ottengo l'installer.
	println "Scarico l'installer:"
	ivy.settings(file: "scripts/ivy-settings.xml")
	new File("target/work/temp/adsinstaller/downloads").mkdirs()
	ivy.retrieve(organisation:"it.finmatica.pa.ag", module:"finmatica-adsinstaller", revision:"1.1.40", type:"jar", inline:true, pattern:"target/work/temp/adsinstaller/AdsInstaller.jar")
	// siccome ivy non supporta (almeno in maniera banale) i classifier di maven, per ora faccio così, per scaricare la versione giusta del visualizzatore:

    /*
    String versioneVisualizzatore = "2.5.5.0-SNAPSHOT"
	File visualizzatore = new File("target/work/temp/adsinstaller/downloads", "agsde2-vis.zip")
	if (visualizzatore.exists()) {
		visualizzatore.delete()
	}
	if (versioneVisualizzatore.endsWith("SNAPSHOT")){
		def root = new XmlParser().parse(new URL("https://nexus.finmatica.it/repository/maven-public/it/finmatica/pa/ag/atti/atti-visualizzatore/${versioneVisualizzatore}/maven-metadata.xml").openStream())
		String snapshot = root.versioning.snapshotVersions.snapshotVersion[0].value.text()
		visualizzatore << new URL("https://nexus.finmatica.it/repository/maven-public/it/finmatica/pa/ag/atti/atti-visualizzatore/${versioneVisualizzatore}/atti-visualizzatore-${snapshot}-install.zip").openStream()
	}
	else {
		visualizzatore << new URL("https://nexus.finmatica.it/repository/maven-public/it/finmatica/pa/ag/atti/atti-visualizzatore/${versioneVisualizzatore}/atti-visualizzatore-${versioneVisualizzatore}-install.zip").openStream()
	}
	*/
	ant.copy (file:"target/dist/agsde2.zip", todir:"target/work/temp/adsinstaller/downloads")

	ant.copy (file:"scripts/config.properties", todir:"target/work/temp/adsinstaller")
	ant.zip  (destfile:"target/dist/installante.zip", basedir:"target/work/temp/adsinstaller")
	println "Installante creato: target/dist/installante.zip"
}

setDefaultTarget (main)