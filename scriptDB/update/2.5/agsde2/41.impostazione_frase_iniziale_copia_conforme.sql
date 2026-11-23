--liquibase formatted sql
--changeset mfrancesconi:2.5_20230518_41.impostazione_frase_iniziale_copia_conforme runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'COPIA_CONFORME_FRASE_INIZIALE'
		, 'Frase iniziale della copia conforme'
		, 'Copia Conforme - Frase Iniziale'
		, 'Riproduzione cartacea del documento informatico sottoscritto digitalmente da'
		, 'Y'
		, null
	);
end;
/