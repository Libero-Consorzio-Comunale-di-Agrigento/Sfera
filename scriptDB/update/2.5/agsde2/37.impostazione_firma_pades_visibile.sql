--liquibase formatted sql
--changeset mfrancesconi:2.5_20230421_37_impostazione_firma_pades_visibile runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'FIRMA_PADES_VISIBILE'
		, 'Visualizza nel pdf il box di firma (solo se firma PADES)'
		, 'FIRMA PADES VISIBILE'
		, 'N'
		, 'Y'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
end;
/