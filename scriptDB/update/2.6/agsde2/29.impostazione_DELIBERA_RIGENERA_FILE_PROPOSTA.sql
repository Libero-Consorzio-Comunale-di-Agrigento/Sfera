--liquibase formatted sql
--changeset mfrancesconi:2.6_20261019_29.impostazione_DELIBERA_RIGENERA_FILE_PROPOSTA runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'DELIBERA_RIGENERA_FILE_PROPOSTA'
		, 'Rigenera delibera: toglie la firma anche agli allegati della proposta di delibera'
		, 'DELIBERA: RIGENERA FILE PROPOSTA'
		, 'N'
		, 'Y'
		, '<rowset><row label="No" value="N" /><row label="Si" value="Y" /></rowset>'
	);
end;
/