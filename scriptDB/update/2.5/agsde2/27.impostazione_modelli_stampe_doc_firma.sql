--liquibase formatted sql
--changeset mfrancesconi:2.5_20230126_27_impostazione_modelli_stampe_doc_firma runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'MODELLI_STAMPE_DOC_RIASSUNTIVO_FIRME_DELEGA'
		, 'Frase da scrivere nel modello DELIBERA DOCUMENTO RIASSUNTIVO FIRME per la delega'
		, 'MODELLI STAMPE DOC RIASSUNTIVO FIRME DELEGA'
		, 'come da delega acquisita agli atti dell''Azienda'
		, 'Y'
		, null
	);
end;
/