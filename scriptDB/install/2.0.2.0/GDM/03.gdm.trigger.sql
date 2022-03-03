-- correggo gli assessori
update gat_delibera set assessore = '' where assessore = ' null'
/

update gat_proposta_delibera set assessore = '' where assessore = ' null'
/

-- correggo la data di adozione
update gat_delibera gd set gd.data_adozione = (select d.data_adozione from ${global.db.target.username}.delibere d where d.id_delibera = gd.id_documento_grails)
/
