--liquibase formatted sql
--changeset mfrancesconi:2.6_20260120_02.update_campi_protetti_impatto_generazionale runOnChange:true stripComments:false failOnError:true

update determine
   set campi_protetti = replace(campi_protetti, 'IMPATTO_GENERAZIONALE', 'IMPATTO_GENERAZIONALE_ARGOMENTO')
 where campi_protetti like '%IMPATTO_GENERAZIONALE%'
/

update proposte_delibera
   set campi_protetti = replace(campi_protetti, 'IMPATTO_GENERAZIONALE', 'IMPATTO_GENERAZIONALE_ARGOMENTO')
 where campi_protetti like '%IMPATTO_GENERAZIONALE%'
/

update delibere
   set campi_protetti = replace(campi_protetti, 'IMPATTO_GENERAZIONALE', 'IMPATTO_GENERAZIONALE_ARGOMENTO')
 where campi_protetti like '%IMPATTO_GENERAZIONALE%'
/