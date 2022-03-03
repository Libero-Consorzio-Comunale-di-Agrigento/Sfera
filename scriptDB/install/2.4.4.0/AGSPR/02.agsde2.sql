UPDATE odg_sedute_stampe ss
   SET ss.id_documento_lettera =
          (SELECT id_documento_esterno
             FROM ${global.db.agspr.username}.gdo_documenti d
            WHERE     d.tipo_oggetto = 'LETTERA'
                  AND d.id_documento = ss.id_documento_lettera)
 WHERE     ss.id_documento_lettera IS NOT NULL
       and exists (SELECT id_documento_esterno
             FROM ${global.db.agspr.username}.gdo_documenti d
            WHERE     d.tipo_oggetto = 'LETTERA'
                  AND d.id_documento = ss.id_documento_lettera)
/