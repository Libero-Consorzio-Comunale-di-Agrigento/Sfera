--liquibase formatted sql
--changeset rdestasio:20230125_02.synonyms runOnChange:true stripComments:false

begin
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_COMUNI FOR ${global.db.ad4.username}.COMUNI';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_DIRITTI_ACCESSO FOR ${global.db.ad4.username}.DIRITTI_ACCESSO';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_MODULI FOR ${global.db.ad4.username}.MODULI';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_PROGETTI FOR ${global.db.ad4.username}.PROGETTI';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_PROVINCE FOR ${global.db.ad4.username}.PROVINCE';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_REGIONI FOR ${global.db.ad4.username}.REGIONI';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_RUOLI FOR ${global.db.ad4.username}.RUOLI';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_STATI_TERRITORI FOR ${global.db.ad4.username}.STATI_TERRITORI';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_UTENTI FOR ${global.db.ad4.username}.UTENTI';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_UTENTI_SOGGETTI FOR ${global.db.ad4.username}.UTENTI_SOGGETTI';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_ISTANZE FOR ${global.db.ad4.username}.ISTANZE';
exception when others then null; 
end;
/
begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_UTENTE FOR ${global.db.ad4.username}.UTENTE';
exception when others then null; 
end;
/
begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_SOGGETTO FOR ${global.db.ad4.username}.SOGGETTO';
exception when others then null; 
end;
/

begin
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_REGISTRO_UTILITY FOR ${global.db.ad4.username}.REGISTRO_UTILITY';
exception when others then null;
end;
/

begin
    execute immediate 'CREATE OR REPLACE SYNONYM  AD4_ENTI for ${global.db.ad4.username}.ENTI';
exception when others then null;
end;
/

begin 
    execute immediate ' CREATE OR REPLACE SYNONYM  AS4_ANAGRAFE_SOGGETTI FOR ${global.db.as4.username}.ANAGRAFE_SOGGETTI';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  AS4_TIPI_SOGGETTO FOR ${global.db.as4.username}.TIPI_SOGGETTO';
exception when others then null; 
end;
/ 
begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_INDIRIZZI_TELEMATICI FOR ${global.db.so4.username}.INDIRIZZI_TELEMATICI';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_AOO_VIEW FOR ${global.db.so4.username}.AOO_VIEW';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_ATTRIBUTI_COMPONENTE FOR ${global.db.so4.username}.VISTA_ATCO_GRAILS';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_ATTRIBUTI_COMPONENTE_PUBB FOR ${global.db.so4.username}.VISTA_ATCO_GRAILS_PUBB';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_COMPONENTI FOR ${global.db.so4.username}.VISTA_COMP_GRAILS';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_COMPONENTI_PUBB FOR ${global.db.so4.username}.VISTA_COMP_GRAILS_PUBB';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_RUOLI_COMPONENTE FOR ${global.db.so4.username}.RUOLI_COMPONENTE';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_RUOLI_COMPONENTE_PUBB FOR ${global.db.so4.username}.VISTA_PUBB_RUCO';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_UNITA_ORGANIZZATIVE FOR ${global.db.so4.username}.VISTA_UNITA_ORGANIZZATIVE';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_UNITA_ORGANIZZATIVE_PUBB FOR ${global.db.so4.username}.VISTA_UNITA_ORGANIZZATIVE_PUBB';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_AMMINISTRAZIONI FOR ${global.db.so4.username}.AMMINISTRAZIONI';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_AOO FOR ${global.db.so4.username}.AOO';
exception when others then null; 
end;
/

begin 
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_SUDDIVISIONI_STRUTTURA FOR ${global.db.so4.username}.SUDDIVISIONI_STRUTTURA';
exception when others then null; 
end;
/

begin
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_OTTICHE FOR ${global.db.so4.username}.OTTICHE';
exception when others then null;
end;
/

begin
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_VISTA_UNITA_ORG_PUBB FOR ${global.db.so4.username}.VISTA_UNITA_ORGANIZZATIVE_PUB';
exception when others then null;
end;
/ 

begin
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_INDIRIZZI_TELEMATICI FOR ${global.db.so4.username}.INDIRIZZI_TELEMATICI';
exception when others then null;
end;
/

begin
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_VISTA_UNITA_ORG_PUBB FOR ${global.db.so4.username}.VISTA_UNITA_ORGANIZZATIVE_PUBB';
exception when others then null;
end;
/

---TODO---
begin
    execute immediate 'CREATE OR REPLACE SYNONYM  AS4_ANAGRAFICI FOR ${global.db.as4.username}.ANAGRAFICI';
exception when others then null;
end;
/

begin
    execute immediate 'CREATE OR REPLACE SYNONYM  AS4_RECAPITI FOR ${global.db.as4.username}.RECAPITI';
exception when others then null;
end;
/

begin
    execute immediate 'CREATE OR REPLACE SYNONYM  AS4_TIPI_RECAPITO FOR ${global.db.as4.username}.TIPI_RECAPITO';
exception when others then null;
end;
/

begin
    execute immediate 'CREATE OR REPLACE SYNONYM  AS4_CONTATTI FOR ${global.db.as4.username}.CONTATTI';
exception when others then null;
end;
/

begin
    execute immediate 'CREATE OR REPLACE SYNONYM  AS4_TIPI_CONTATTO FOR ${global.db.as4.username}.TIPI_CONTATTO';
exception when others then null;
end;
/

begin
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_SOGGETTI_AOO FOR ${global.db.so4.username}.SOGGETTI_AOO';
exception when others then null;
end;
/

begin
    execute immediate 'CREATE OR REPLACE SYNONYM  SO4_SOGGETTI_UNITA FOR ${global.db.so4.username}.SOGGETTI_UNITA';
exception when others then null;
end;
/