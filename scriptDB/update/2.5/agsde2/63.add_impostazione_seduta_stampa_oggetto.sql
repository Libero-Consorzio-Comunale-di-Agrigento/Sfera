--liquibase formatted sql
--changeset mfrancesconi:2.5_20240521_63_add_impostazione_seduta_stampa_oggetto runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'SEDUTA_STAMPA_OGGETTO'
		, 'Frase calcolata in fase di invio pec per le stampe delle sedute (Usare $ come separatore delle variabili)'
		, 'Seduta Stampa Oggetto'
		, '$STAMPA_TITOLO$ $COMMISSIONE_TITOLO$ $SEDUTA_NUMERO$ / $SEDUTA_ANNO$ del $SEDUTA_DATA_ORA_MINUTI$'
		, 'Y'
		, null
	);
end;
/
