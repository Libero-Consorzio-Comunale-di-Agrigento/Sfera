--liquibase formatted sql
--changeset mfrancesconi:2.5_20231121_51.alter_table_odg_sedute_partecipanti runOnChange:true stripComments:false failOnError:false

alter table odg_sedute_partecipanti add ( data_entrata date, ora_entrata varchar2(5), data_uscita date, ora_uscita varchar2(5))
/

begin
	impostazioni_pkg.add_impostazione (
		  'ODG_DATA_ORA_PRESENZA_COMPONENTI'
		, 'Indica se gestire l''orario di entrate e uscita dei convocati in seduta'
		, 'ODG DATA-ORA PRESENZA COMPONENTI'
		, 'N'
		, 'N'
		, '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>'
	);
end;
/