--liquibase formatted sql
--changeset mfrancesconi:2.5_20230504_39_impostazione_vis_download_zip runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'VIS_DOWNLOAD_ZIP'
		, 'Visualizza il download dello zip'
		, 'VISUALIZZA DOWNLOAD ZIP'
		, 'N'
		, 'Y'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
end;
/