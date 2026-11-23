--liquibase formatted sql
--changeset mfrancesconi:2.6_20260819_23.impostazione_excel_numero_passaggi runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'EXCEL_NUMERO_PASSAGGI'
		, 'Indica se riportare il numero delle volte che il documento è passato nello step corrente nel file di excel della ricerca'
		, 'EXCEL NUMERO PASSAGGI'
		, 'N'
		, 'Y'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
end;
/

begin
	impostazioni_pkg.add_impostazione (
		  'EXCEL_ULTIMO_ATTORE_FLUSSO'
		, 'Indica se riportare il nominativo dell''ultimo utente che ha gestito il documento nel file di excel della ricerca'
		, 'EXCEL ULTIMO ATTORE FLUSSO'
		, 'N'
		, 'Y'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
end;
/