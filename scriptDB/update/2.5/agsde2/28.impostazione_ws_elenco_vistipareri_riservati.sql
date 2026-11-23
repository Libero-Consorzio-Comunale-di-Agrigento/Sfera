--liquibase formatted sql
--changeset amarinelli:2.5_20230310_28_impostazione_es_elenco_vistipareri_riservati runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'WS_ELENCO_VISTIPARERI_RISERVATI'
		, 'Elenco dei codici dei vistipareri riservati (separati da #)'
		, 'WS ELENCO VISTIPARERI RISERVATI'
		, '#'
		, '#'
		, null
	);
end;
/