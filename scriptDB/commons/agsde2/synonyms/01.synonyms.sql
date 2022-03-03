--liquibase formatted sql
--changeset rdestasio:20200220_01.synonyms runOnChange:true stripComments:false

-- AD4
CREATE OR REPLACE SYNONYM AD4_ASSISTENTE_VIRTUALE_PKG FOR ${global.db.ad4.username}.ASSISTENTE_VIRTUALE_PKG
/

CREATE OR REPLACE SYNONYM AD4_KILL_USER_JOBS FOR ${global.db.ad4.username}.KILL_USER_JOBS
/

-- AS4
create or replace synonym as4_anagrafe_soggetti for ${global.db.as4.username}.anagrafe_soggetti
/

create or replace synonym as4_anagrafe_soggetti_pkg for ${global.db.as4.username}.anagrafe_soggetti_pkg
/

create or replace synonym as4_anagrafe_soggetti_refresh for ${global.db.as4.username}.anagrafe_soggetti_refresh
/

create or replace synonym as4_anagrafe_soggetti_tpk for ${global.db.as4.username}.anagrafe_soggetti_tpk
/

create or replace synonym as4_anagrafici for ${global.db.as4.username}.anagrafici
/

create or replace synonym as4_anagrafici_pkg for ${global.db.as4.username}.anagrafici_pkg
/

create or replace synonym as4_anagrafici_tpk for ${global.db.as4.username}.anagrafici_tpk
/

create or replace synonym as4_contatti for ${global.db.as4.username}.contatti
/

create or replace synonym as4_contatti_pkg for ${global.db.as4.username}.contatti_pkg
/

create or replace synonym as4_contatti_tpk for ${global.db.as4.username}.contatti_tpk
/

create or replace synonym as4_recapiti for ${global.db.as4.username}.recapiti
/

create or replace synonym as4_recapiti_pkg for ${global.db.as4.username}.recapiti_pkg
/

create or replace synonym as4_recapiti_tpk for ${global.db.as4.username}.recapiti_tpk
/

create or replace synonym as4_soggetti for ${global.db.as4.username}.soggetti
/

create or replace synonym as4_tipi_contatto for ${global.db.as4.username}.tipi_contatto
/

create or replace synonym as4_tipi_contatto_tpk for ${global.db.as4.username}.tipi_contatto_tpk
/

create or replace synonym as4_tipi_recapito for ${global.db.as4.username}.tipi_recapito
/

create or replace synonym as4_tipi_recapito_tpk for ${global.db.as4.username}.tipi_recapito_tpk
/

create or replace synonym as4_tipi_soggetto for ${global.db.as4.username}.tipi_soggetto
/

create or replace synonym as4_tipi_soggetto_tpk for ${global.db.as4.username}.tipi_soggetto_tpk
/

-- SO4
create or replace synonym so4_ags_pkg for ${global.db.so4.username}.so4_ags_pkg
/

create or replace synonym so4_albero_unita_org for ${global.db.so4.username}.unita_organizzative
/

create or replace synonym so4_amministrazioni for ${global.db.so4.username}.amministrazioni
/

create or replace synonym so4_aoo for ${global.db.so4.username}.aoo
/

create or replace synonym so4_aoo_view for ${global.db.so4.username}.aoo_view
/

create or replace synonym so4_attributi_componente for ${global.db.so4.username}.vista_atco_grails
/

create or replace synonym so4_attributi_componente_pubb for ${global.db.so4.username}.vista_atco_grails_pubb
/

create or replace synonym so4_auor for ${global.db.so4.username}.anagrafe_unita_organizzative
/

create or replace synonym so4_componenti for ${global.db.so4.username}.vista_comp_grails
/

create or replace synonym so4_componenti_pubb for ${global.db.so4.username}.vista_comp_grails_pubb
/

create or replace synonym so4_indirizzi_telematici for ${global.db.so4.username}.indirizzi_telematici
/

create or replace synonym so4_ottiche for ${global.db.so4.username}.ottiche
/

create or replace synonym so4_ruoli_componente for ${global.db.so4.username}.ruoli_componente
/

create or replace synonym so4_ruoli_componente_pubb for ${global.db.so4.username}.vista_pubb_ruco
/

create or replace synonym so4_soggetti_unita for ${global.db.so4.username}.soggetti_unita
/

create or replace synonym so4_suddivisioni_struttura for ${global.db.so4.username}.suddivisioni_struttura
/

create or replace synonym so4_unita_organizzative for ${global.db.so4.username}.vista_unita_organizzative
/

create or replace synonym so4_unita_organizzative_pubb for ${global.db.so4.username}.vista_unita_organizzative_pubb
/

create or replace synonym so4_util for ${global.db.so4.username}.so4_util
/

create or replace synonym so4_vista_pubb_unita for ${global.db.so4.username}.vista_pubb_unita
/

create or replace synonym so4_soggetti_aoo for ${global.db.so4.username}.soggetti_aoo
/

create or replace synonym SO4_ANAGRAFE_UNITA FOR ${global.db.so4.username}.ANAGRAFE_UNITA_ORGANIZZATIVE
/

CREATE OR REPLACE SYNONYM SO4_INDIRIZZO_TELEMATICO FOR ${global.db.so4.username}.INDIRIZZO_TELEMATICO
/

CREATE OR REPLACE SYNONYM SO4_CODICI_IPA_TPK FOR ${global.db.so4.username}.CODICI_IPA_TPK
/

create or replace synonym SO4_ANA_UNOR_PKG for ${global.db.so4.username}.anagrafe_unita_organizzativa
/

create or replace synonym so4_unor for ${global.db.so4.username}.unita_organizzative
/

create or replace synonym so4_codici_ipa for ${global.db.so4.username}.codici_ipa
/
