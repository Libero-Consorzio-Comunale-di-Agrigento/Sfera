--liquibase formatted sql
--changeset mfrancesconi:2.5_20221028_25_impostazione_url_anagrafica runOnChange:true stripComments:false failOnError:false

begin
	impostazioni_pkg.add_impostazione (
		  'URL_ANAGRAFICA'
		, 'Url applicativo Anagrafica (se sullo stesso server di sfera è sufficiente il percorso relativo)'
		, 'URL ANAGRAFICA'
		, '/Anagrafica'
		, 'Y'
		, ''
	);
end;
/