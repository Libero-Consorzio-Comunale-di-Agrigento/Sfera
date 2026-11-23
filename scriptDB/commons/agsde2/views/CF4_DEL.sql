--liquibase formatted sql
--changeset mfrancesconi:CF4_DEL runOnChange:true stripComments:false

create or replace force view cf4_del
(
    id_documento,
    anno_del,
    numero_del,
    sede_del,
    data_del,
    descrizione,
    tipo_esec,
    numero_app,
    data_app,
    d_data_del,
    d_data_app,
    anno_pro,
    numero_pro,
    unita_pro,
    data_firma,
    firmatario,
    impegno_obbligatorio,
    url_documento
)
as
    select dete.id_determina,
           dete.anno_determina
               anno_del,
           dete.numero_determina,
           nvl(tr.registro_esterno, tr.tipo_registro),
           to_number (to_char (dete.data_numero_determina, 'J')),
           substr (dete.oggetto, 1, 140),
           case
               when dete.stato = 'ANNULLATO' then 4 -- ANNULLATO
               when dete.stato = 'NON_ESECUTIVO' then 4 -- ANNULLATO
               when dete.data_esecutivita is not null then 1 -- ESECUTIVO
               else 5 -- IN ATTESA DI ESECUTIVITA
           end case,
           null,
           to_number (null),
           trunc (dete.data_numero_determina),
           trunc (data_esecutivita),
           dete.anno_proposta,
           dete.numero_proposta,
           nvl(trp.registro_esterno, trp.tipo_registro),
           to_char (utility_pkg.get_prima_data_firma_determina (dete.id_determina), 'dd/mm/yyyy'),
           utility_pkg.get_primo_firmatario_determina (dete.id_determina),
           td.impegno_obbligatorio,
           trim (impostazioni_pkg.get_impostazione ('URL_SERVER', dete.ente)) || '/standalone.zul?operazione=APRI_DOCUMENTO&tipoDocumento=DETERMINA&id=' || dete.id_determina
      from determine dete, tipi_registro tr, tipi_registro trp, tipi_determina td
     where dete.numero_determina is not null
       and dete.valido = 'Y'
       and nvl (dete.stato, 'X') not in ('ANNULLATO')
       and tr.tipo_registro = dete.registro_determina
       and trp.tipo_registro = dete.registro_proposta
       and dete.id_tipo_determina = td.id_tipo_determina
       and not exists
               (select '1'
                  from delibere deli, tipi_registro tr_del
                 where deli.registro_delibera = tr.tipo_registro
                   and deli.anno_delibera = dete.anno_determina
                   and nvl(tr_del.registro_esterno, tr_del.tipo_registro) = nvl(tr.registro_esterno, tr.tipo_registro)
                   and deli.numero_delibera = dete.numero_determina)
    union all
    select deli.id_delibera,
           deli.anno_delibera,
           deli.numero_delibera,
           nvl(tr.registro_esterno, tr.tipo_registro),
           to_number (to_char (deli.data_adozione, 'J')),
           deli.oggetto,
           case -- Il dizionario di questi valori ¿ la tabella CF4.T10
               when deli.eseguibilita_immediata = 'Y' and deli.data_esecutivita is null then 3 -- immediatamente eseguibile
               when deli.data_esecutivita is not null then 2 -- esecutiva per decorrenza termini
               when deli.stato = 'ANNULLATO' then 4 -- dete annullata
               else 5 -- in attesa di esecutivit¿
           end as tipo_esec,
           null, --del.numero_app
           to_number (null), --del.data_app
           trunc (deli.data_adozione),
           null, --deli.d_data_app
           pd.anno_proposta,
           pd.numero_proposta,
           nvl(trp.registro_esterno, trp.tipo_registro), --deli.unita_pro
           to_char (utility_pkg.get_prima_data_firma_prop_deli (pd.id_proposta_delibera), 'dd/mm/yyyy'),
           utility_pkg.get_primo_firmatario_prop_deli (pd.id_proposta_delibera),
           td.impegno_obbligatorio,
           trim (impostazioni_pkg.get_impostazione ('URL_SERVER', deli.ente)) || '/standalone.zul?operazione=APRI_DOCUMENTO&tipoDocumento=DELIBERA&id=' || deli.id_delibera
      from proposte_delibera  pd,
           delibere           deli,
           tipi_registro      tr,
           tipi_registro      trp,
           tipi_delibera     td
     where deli.registro_delibera = tr.tipo_registro
       and trp.tipo_registro = pd.registro_proposta
       and deli.id_proposta_delibera = pd.id_proposta_delibera
       and pd.id_tipo_delibera= td.id_tipo_delibera
       and not exists
               (select '1'
                  from determine dete, tipi_registro tr_dete
                 where dete.registro_determina = tr.tipo_registro
                   and dete.anno_determina = deli.anno_delibera
                   and nvl(tr_dete.registro_esterno, tr_dete.tipo_registro) = nvl(tr.registro_esterno, tr.tipo_registro)
                   and dete.numero_determina = deli.numero_delibera)
/
