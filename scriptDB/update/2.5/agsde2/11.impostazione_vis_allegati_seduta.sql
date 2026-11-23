--liquibase formatted sql
--changeset mfrancesconi:2.5_20220629_11_impostazione_vis_allegati_seduta runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'VIS_SEDUTA_ALLEGATI_PROPOSTA'
		, 'Indica se visualizzare gli allegati della proposta dalla pagina della seduta'
		, 'VISUALIZZA ALLEGATI NELLA PAGINA DELLA SEDUTA'
		, 'N'
		, 'Y'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
end;
/