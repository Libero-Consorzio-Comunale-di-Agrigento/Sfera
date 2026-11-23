--liquibase formatted sql
--changeset mfrancesconi:2.5_20221020_21.add_impostazione_delibera_emendamento runOnChange:true

begin
	impostazioni_pkg.add_impostazione (
		  'DELIBERA_EMENDAMENTO'
		, 'Indica se è abilitata la gestione degli emendamenti a partire da una delibera'
		, 'DELIBERA EMENDAMENTO'
		, 'N'
		, 'Y'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
end;
/