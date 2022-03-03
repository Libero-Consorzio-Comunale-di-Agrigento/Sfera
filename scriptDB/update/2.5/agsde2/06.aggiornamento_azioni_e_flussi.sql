--liquibase formatted sql
--changeset svalenti:2.5.6.0_20210928_06

DECLARE

d_id_azione_abilita NUMBER;
d_id_azione_proteggi NUMBER;
d_esiste_azione_abilita NUMBER;
d_esiste_azione_proteggi NUMBER;
d_azioni_ingresso_idx NUMBER;
d_azioni_uscita_idx NUMBER;
d_id_nodo_iniziale NUMBER;

BEGIN
-- calcolo le azioni
begin
    SELECT id_azione INTO d_id_azione_abilita
    FROM wkf_diz_azioni
    WHERE nome='Abilita il campo COMMISSIONE' AND tipo_oggetto = 'PROPOSTA_DELIBERA'
    AND nome_bean = 'campiDocumentoAction' AND nome_metodo = 'abilitaCampo_COMMISSIONE';
exception
    when no_data_found then

    SELECT hibernate_sequence.nextval INTO d_id_azione_abilita FROM dual;
    INSERT INTO wkf_diz_azioni (id_azione
                               , version
                               , descrizione
                               , nome
                               , nome_bean
                               , nome_metodo
                               , tipo
                               , tipo_oggetto
                               , valido)
    VALUES (d_id_azione_abilita
           , 0
           , 'Abilita il campo COMMISSIONE'
           , 'Abilita il campo COMMISSIONE'
           , 'campiDocumentoAction'
           , 'abilitaCampo_COMMISSIONE'
           , 'AUTOMATICA'
           , 'PROPOSTA_DELIBERA'
           , 'Y');
end;

begin
    SELECT id_azione INTO d_id_azione_proteggi
    FROM wkf_diz_azioni
    WHERE nome='Protegge il campo COMMISSIONE' AND tipo_oggetto = 'PROPOSTA_DELIBERA'
    AND nome_bean = 'campiDocumentoAction' AND nome_metodo = 'proteggiCampo_COMMISSIONE';
exception
    when no_data_found then

    SELECT hibernate_sequence.nextval INTO d_id_azione_proteggi FROM dual;
    INSERT INTO wkf_diz_azioni (id_azione
                               , version
                               , descrizione
                               , nome
                               , nome_bean
                               , nome_metodo
                               , tipo
                               , tipo_oggetto
                               , valido)
    VALUES (d_id_azione_proteggi
           , 0
           , 'Protegge il campo COMMISSIONE'
           , 'Protegge il campo COMMISSIONE'
           , 'campiDocumentoAction'
           , 'proteggiCampo_COMMISSIONE'
           , 'AUTOMATICA'
           , 'PROPOSTA_DELIBERA'
           , 'Y');

end;

-- trovo tutti gli iter con nodo 'GESTIONE ODG' con il rispettivo id_cfg_step
FOR iter IN (
        SELECT DISTINCT wkf_cfg_iter.id_cfg_iter,  wkf_cfg_step.id_cfg_step
        FROM wkf_cfg_iter,  wkf_cfg_step
        WHERE wkf_cfg_iter.id_cfg_iter = wkf_cfg_step.id_cfg_iter
		AND wkf_cfg_iter.stato = 'IN_USO'
        AND wkf_cfg_step.nome='GESTIONE ODG'
  )
  LOOP

  -------------------------------------------------------
  -- al primo nodo aggiungo se non presente l'azione "Protegge il campo COMMISSIONE"
  -------------------------------------------------------

    SELECT id_cfg_step into d_id_nodo_iniziale
    FROM wkf_cfg_step
    WHERE id_cfg_iter = iter.id_cfg_iter AND sequenza = 0;

    SELECT count(1) INTO d_esiste_azione_proteggi
    FROM wkf_cfg_step_azioni_in
    WHERE id_cfg_step = d_id_nodo_iniziale AND id_azione_in = d_id_azione_proteggi;

IF d_esiste_azione_proteggi=0 THEN
    -- inserisco azione
    SELECT nvl(max(azioni_ingresso_idx),-1)+1 INTO d_azioni_ingresso_idx
    FROM wkf_cfg_step_azioni_in
    WHERE id_cfg_step = d_id_nodo_iniziale;

    INSERT INTO wkf_cfg_step_azioni_in(ID_CFG_STEP,ID_AZIONE_IN,AZIONI_INGRESSO_IDX)
    VALUES (d_id_nodo_iniziale,d_id_azione_proteggi,d_azioni_ingresso_idx);
END IF;

  -------------------------------------------------------
  -- step 'GESTIONE ODG' aggiungo se non presente l'azione "Abilita il campo COMMISSIONE" in ingresso
  -------------------------------------------------------

    SELECT count(1) INTO d_esiste_azione_abilita
    FROM wkf_cfg_step_azioni_in
    WHERE id_cfg_step = iter.id_cfg_step AND id_azione_in = d_id_azione_abilita;

IF d_esiste_azione_abilita=0 THEN
    -- inserisco azione
    SELECT nvl(max(azioni_ingresso_idx),-1)+1 INTO d_azioni_ingresso_idx
    FROM wkf_cfg_step_azioni_in
    WHERE id_cfg_step = iter.id_cfg_step;

    INSERT INTO wkf_cfg_step_azioni_in(ID_CFG_STEP,ID_AZIONE_IN,AZIONI_INGRESSO_IDX)
    VALUES (iter.id_cfg_step,d_id_azione_abilita,d_azioni_ingresso_idx);
 END IF;

  -------------------------------------------------------
  -- step 'GESTIONE ODG' aggiungo se non presente l'azione "Protegge il campo COMMISSIONE" in uscita
  -------------------------------------------------------

    SELECT count(1) INTO d_esiste_azione_proteggi
    FROM wkf_cfg_step_azioni_out
    WHERE id_cfg_step = iter.id_cfg_step
    AND id_azione_out = d_id_azione_proteggi;

IF d_esiste_azione_proteggi=0 THEN
    -- inserisco azione
    SELECT nvl(max(azioni_uscita_idx),-1)+1 INTO d_azioni_uscita_idx
    FROM wkf_cfg_step_azioni_out
    WHERE id_cfg_step = iter.id_cfg_step;

    INSERT INTO wkf_cfg_step_azioni_out(ID_CFG_STEP,ID_AZIONE_OUT,AZIONI_USCITA_IDX)
    VALUES (iter.id_cfg_step,d_id_azione_proteggi,d_azioni_uscita_idx);
END IF;

commit;

END LOOP;
END;
/