package it.finmatica.atti.documenti

class OrganoControlloNotificaDocumento {
	OrganoControlloNotifica organoControlloNotifica
	Determina 	determina
	Delibera	delibera

	static mapping = {
		table 		'organi_controllo_notifiche_doc'
		id 			column: 'id_organo_controllo_doc'
		organoControlloNotifica column: 'id_organo_controllo_notifica', index: 'orgconnotdoc_orgconnot_fk'
		determina	column: 'id_determina',								index: 'orgconnotdoc_det_fk'
		delibera 	column: 'id_delibera',								index: 'orgconnotdoc_del_fk'
	}

    static constraints = {
		determina 	nullable: true
		delibera 	nullable: true
	}
}
