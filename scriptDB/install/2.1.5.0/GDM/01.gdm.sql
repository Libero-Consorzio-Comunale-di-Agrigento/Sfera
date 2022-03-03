grant select on impronte_file to ${global.db.target.username}
/

update tipi_documento set competenze_allegati = 'S' where area_modello = 'SEGRETERIA.ATTI.2_0' and acronimo_modello is not null
/

begin
    execute immediate 'CREATE INDEX GAT$DELI$NOTE_CTX ON GAT_DELIBERA (NOTE) INDEXTYPE IS CTXSYS.CTXCAT PARAMETERS(''lexer italian_lexer wordlist italian_wordlist stoplist italian_stoplist memory 10M'') NOPARALLEL';
exception when others then
    null;
end;
/

begin
    execute immediate 'CREATE INDEX GAT$DELI$OGGE_CTX ON GAT_DELIBERA  (OGGETTO) 	INDEXTYPE IS  CTXSYS.CTXCAT PARAMETERS(''lexer italian_lexer wordlist italian_wordlist stoplist italian_stoplist memory 10M'') NOPARALLEL';
exception when others then
    null;
end;
/

begin
    execute immediate 'CREATE INDEX GAT$DETE$OGGE_CTX ON GAT_DETERMINA (OGGETTO) 	INDEXTYPE IS CTXSYS.CTXCAT PARAMETERS(''lexer italian_lexer wordlist italian_wordlist stoplist italian_stoplist memory 10M'') NOPARALLEL';
exception when others then
    null;
end;
/

begin
    execute immediate 'CREATE INDEX GAT$DETE$NOTE_CTX ON GAT_DETERMINA (NOTE) 		INDEXTYPE IS CTXSYS.CTXCAT PARAMETERS(''lexer italian_lexer wordlist italian_wordlist stoplist italian_stoplist memory 10M'') NOPARALLEL';
exception when others then
    null;
end;
/