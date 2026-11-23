--liquibase formatted sql
--changeset mfrancesconi:2.5_20231201_53.impostazione_excel_importoLiq_firmatari runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'CONTABILITA_CFA_EXCEL_IMPORTO'
		, 'Indica se riportare l''importo di liquidazione nel file di excel della ricerca'
		, 'CONTABILITA CFA EXCEL IMPORTO'
		, 'N'
		, 'N'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
end;
/

begin
	impostazioni_pkg.add_impostazione (
		  'EXCEL_ELENCO_FIRMATARI'
		, 'Indica se riportare l''elenco dei firmatari nel file di excel della ricerca'
		, 'EXCEL ELENCO FIRMATARI'
		, 'N'
		, 'N'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
end;
/