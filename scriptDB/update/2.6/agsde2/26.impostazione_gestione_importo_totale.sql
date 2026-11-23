--liquibase formatted sql
--changeset mfrancesconi:2.6_20260831_26.impostazione_gestione_importo_totale runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'GESTIONE_IMPORTO_TOTALE'
		, 'Abilita l''inserimento di importo totale per le proposte.'
		, 'GESTIONE IMPORTO TOTALE'
		, 'N'
		, 'Y'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
end;
/