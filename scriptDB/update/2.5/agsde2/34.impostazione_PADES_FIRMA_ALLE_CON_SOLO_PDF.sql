--liquibase formatted sql
--changeset mfrancesconi:2.5_20221028_34_impostazione_PADES_FIRMA_ALLE_CON_SOLO_PDF runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'PADES_FIRMA_ALLE_CON_SOLO_PDF'
		, 'Se abilitato non è possibile firmare Allegati che contengono file non pdf.'
		, 'PADES_FIRMA_ALLE_CON_SOLO_PDF'
		, 'N'
		, 'N'
		, ''
	);
end;
/