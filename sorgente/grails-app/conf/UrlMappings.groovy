
class UrlMappings {

	// questo serve per escludere il percorso dei webservice dall'essere processati come Controller di Grails.
	static excludes = ["/services/*"]

	static mappings = {
		"/$controller/$action?/$id?"(controllerExclude:'/services/*');
		"500"(view:'/error')
		"/aggiornaImpostazioni"(controller:"admin", action:"aggiornaImpostazioni")
		"/multiEnte" (controller:"attiMultiEnte")
		"/assistenza"(view:"/assistenza/index.zul")
		"/"(view:"/index.zul")
	}
}
