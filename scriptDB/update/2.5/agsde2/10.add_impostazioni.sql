--liquibase formatted sql
--changeset mfrancesconi:2.5_20220526_10_add_impostazioni runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'VIS_COPYRIGHT'
		, 'Visualizza il copyright nel visualizzatore'
		, 'VISUALIZZA COPYRIGHT'
		, 'Y'
		, 'Y'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
end;
/