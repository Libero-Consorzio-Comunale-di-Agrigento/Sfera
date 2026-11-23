--liquibase formatted sql
--changeset amarinelli:2.5_20240528_65_impostazione_SUFFISSO_FIRMA_PADES runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'SUFFISSO_FIRMA_PADES'
		, 'Suffisso da inserire nel nomefile per la firma Pades se valorizzato ( diverso da -)'
		, 'SUFFISSO FIRMA PADES'
		, '-'
		, 'Y'
		, null
	);
end;
/
