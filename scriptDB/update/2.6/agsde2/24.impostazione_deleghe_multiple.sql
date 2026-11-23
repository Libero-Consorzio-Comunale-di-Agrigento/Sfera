--liquibase formatted sql
--changeset mfrancesconi:2.6_20260825_24.impostazione_excel_deleghe_multiple runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'DELEGHE_MULTIPLE'
		, 'Indica se è possibile aggiungere più deleghe per le proposte di delibera'
		, 'DELEGHE MULTIPLE'
		, 'N'
		, 'Y'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
end;
/