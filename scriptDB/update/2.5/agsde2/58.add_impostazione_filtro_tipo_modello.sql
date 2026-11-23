--liquibase formatted sql
--changeset amarinelli:2.5_20240202_58_add_impostazione_filtro_tipo_modello runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'MODELLI_TESTO_UNITA_PROPONENTE'
		, 'Attiva la gestione del filtro modello di testo su UO proponente'
		, 'MODELLI TESTO UNITA PROPONENTE'
		, 'N'
		, 'Y'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
end;
/
