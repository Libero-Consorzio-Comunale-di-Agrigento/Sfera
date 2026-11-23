--liquibase formatted sql
--changeset mfrancesconi:2.5_20240329_61.smartPnd_mapping_integrazioni runOnChange:true

declare
   d_valore       varchar2 (255);
begin
   select valore_esterno
     into d_valore
     from mapping_integrazioni
    where categoria = 'SMARTPND' and codice = 'TIPO_COMUNICAZIONE' and valore_interno = '*' and rownum = 1;

   for c in (select id_tipo_determina
               from tipi_determina
              where valido = 'Y' and pnd_notifica = 'Y')
   loop
      insert into mapping_integrazioni (id_mapping_integrazione,
                                        categoria,
                                        codice,
                                        valore_esterno,
                                        valore_interno,
                                        ente,
                                        sequenza,
                                        descrizione)
         select hibernate_sequence.nextval,
                'SMARTPND',
                'TIPO_COMUNICAZIONE',
                d_valore,
                to_char (c.id_tipo_determina),
                impostazioni_pkg.get_impostazione ('ENTI_SO4', '*'),
                0,
                null
           from dual
          where not exists
                   (select 1
                      from mapping_integrazioni
                     where categoria = 'SMARTPND'
                       and codice = 'TIPO_COMUNICAZIONE'
                       and valore_esterno = to_char (c.id_tipo_determina));
   end loop;

   delete mapping_integrazioni
    where categoria = 'SMARTPND' and codice = 'TIPO_COMUNICAZIONE' and valore_interno = '*';
exception
   when others then
      null;
end;
/