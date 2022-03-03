-- aggiorno il formato-file
update formati_file set nome = 'HD' where nome = '_HD'
/

-- aggiorno i nomi file su gdm:
update oggetti_file set filename = substr(filename, 1, length(filename)-3)||'.HD' where filename like '%_HD'
/

-- aggiorno i dati del formato file che in alcuni casi è "sbagliato"
update oggetti_file set id_formato = (select id_formato from formati_file where nome = 'HD') where filename like '%.HD' and id_formato not in ((select id_formato from formati_file where nome = 'HD'))
/

-- aggiorno i nomi file su gdm log:
update oggetti_file_log set filename = substr(filename, 1, length(filename)-3)||'.HD', nome_formato = 'HD' where filename like '%_HD'
/