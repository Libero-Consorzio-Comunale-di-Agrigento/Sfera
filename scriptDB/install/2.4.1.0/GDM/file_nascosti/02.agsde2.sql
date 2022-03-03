-- aggiorno i nomi file su sfera:
update file_allegati set nome = substr(nome, 1, length(nome)-3)||'.HD' where nome like '%_HD'
/
