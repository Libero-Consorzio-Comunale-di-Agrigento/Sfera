--liquibase formatted sql
--changeset mfrancesconi:2.6_20260209_08.update_impostazione_impatto_generazionale runOnChange:true stripComments:false failOnError:true

update impostazioni
set descrizione = 'Flag che definisce se è presente il dizionario impatto generazionale / argomento',
    etichetta   = 'Impatto generazionale / Argomento'
where codice = 'IMPATTO_GENERAZIONALE'
/