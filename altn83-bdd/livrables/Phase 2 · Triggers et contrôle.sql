-- ------------------------------------------------------------
-- NanoOrbit - Phase 2 · Livrable L2-C et L2-D - Script Triggers et Contrôle
-- 5 triggers métier NanoOrbit
-- ------------------------------------------------------------

SET SERVEROUTPUT ON;

-- ------------------------------------------------------------
-- Livrable L2-C - Triggers
-- ------------------------------------------------------------

-- ------------------------------------------------------------
-- T1 - trg_valider_fenetre
-- BEFORE INSERT ON FENETRE_COM
-- RG-S06 : bloque si satellite Désorbité
-- RG-G03 : bloque si station en Maintenance
-- ------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_valider_fenetre
BEFORE INSERT ON FENETRE_COM
FOR EACH ROW
DECLARE
    v_statut_sat  SATELLITE.statut%TYPE;
    v_statut_sta  STATION_SOL.statut%TYPE;
BEGIN
    -- Vérification statut satellite (RG-S06)
    SELECT statut INTO v_statut_sat
    FROM   SATELLITE
    WHERE  id_satellite = :NEW.id_satellite;

    IF v_statut_sat = 'Désorbité' THEN
        RAISE_APPLICATION_ERROR(-20001,
            'ORA-20001 : Le satellite ' || :NEW.id_satellite ||
            ' est Désorbité et ne peut plus recevoir de fenêtres de communication (RG-S06).');
    END IF;

    -- Vérification statut station (RG-G03)
    SELECT statut INTO v_statut_sta
    FROM   STATION_SOL
    WHERE  code_station = :NEW.code_station;

    IF v_statut_sta = 'Maintenance' THEN
        RAISE_APPLICATION_ERROR(-20002,
            'ORA-20002 : La station ' || :NEW.code_station ||
            ' est en Maintenance et ne peut pas planifier de nouvelles fenêtres (RG-G03).');
    END IF;
END trg_valider_fenetre;

-- Tests pour le trigger T1
-- Cas valide : doit réussir (SAT-001, GS-KIR-01 Active)
INSERT INTO FENETRE_COM VALUES (DEFAULT, TIMESTAMP '2024-02-01 10:00:00', 300, 75.0, NULL, 'Planifiée', 'SAT-001', 'GS-KIR-01');
-- Résultat attendu : 1 ligne insérée

-- Cas erreur RG-S06 : doit lever ORA-20001
INSERT INTO FENETRE_COM VALUES (DEFAULT, TIMESTAMP '2024-02-01 10:00:00', 300, 75.0, NULL, 'Planifiée', 'SAT-005', 'GS-KIR-01');
-- Résultat attendu : ORA-20001 - SAT-005 est Désorbité

-- Cas erreur RG-G03 : doit lever ORA-20002
INSERT INTO FENETRE_COM VALUES (DEFAULT, TIMESTAMP '2024-02-01 10:00:00', 300, 75.0, NULL, 'Planifiée', 'SAT-001', 'GS-SGP-01');
-- Résultat attendu : ORA-20002 - GS-SGP-01 est en Maintenance

-- ------------------------------------------------------------
-- T2 - trg_no_chevauchement
-- BEFORE INSERT OR UPDATE ON FENETRE_COM
-- RG-F02 : pas de chevauchement temporel pour un même satellite
-- RG-F03 : pas de chevauchement temporel pour une même station
-- ------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_no_chevauchement
BEFORE INSERT OR UPDATE ON FENETRE_COM
FOR EACH ROW
DECLARE
    v_count_sat NUMBER;
    v_count_sta NUMBER;
    v_fin_new   TIMESTAMP;
BEGIN
    -- Calcul de la fin de la nouvelle fenêtre
    v_fin_new := :NEW.datetime_debut + (:NEW.duree / 86400);

    -- Vérification chevauchement satellite
    SELECT COUNT(*) INTO v_count_sat
    FROM   FENETRE_COM
    WHERE  id_satellite = :NEW.id_satellite
    AND    id_fenetre  != NVL(:NEW.id_fenetre, -1)
    AND    datetime_debut < v_fin_new
    AND    (datetime_debut + (duree / 86400)) > :NEW.datetime_debut;

    IF v_count_sat > 0 THEN
        RAISE_APPLICATION_ERROR(-20003,
            'ORA-20003 : Chevauchement de fenêtres détecté pour le satellite ' ||
            :NEW.id_satellite || ' (RG-F02).');
    END IF;

    -- Vérification chevauchement station
    SELECT COUNT(*) INTO v_count_sta
    FROM   FENETRE_COM
    WHERE  code_station = :NEW.code_station
    AND    id_fenetre  != NVL(:NEW.id_fenetre, -1)
    AND    datetime_debut < v_fin_new
    AND    (datetime_debut + (duree / 86400)) > :NEW.datetime_debut;

    IF v_count_sta > 0 THEN
        RAISE_APPLICATION_ERROR(-20003,
            'ORA-20003 : Chevauchement de fenêtres détecté pour la station ' ||
            :NEW.code_station || ' (RG-F03).');
    END IF;
END trg_no_chevauchement;

-- Tests pour le trigger T2
-- Cas valide (pas de chevauchement) :
INSERT INTO FENETRE_COM VALUES (DEFAULT, TIMESTAMP '2024-01-15 10:00:00', 300, 70.0, NULL, 'Planifiée', 'SAT-001', 'GS-TLS-01');
-- Résultat attendu : 1 ligne insérée

-- Cas chevauchement satellite : fenêtre 1 = SAT-001 09:14 + 420s (fin ~09:21)
INSERT INTO FENETRE_COM VALUES (DEFAULT, TIMESTAMP '2024-01-15 09:15:00', 300, 70.0, NULL, 'Planifiée', 'SAT-001', 'GS-TLS-01');
-- Résultat attendu : ORA-20003 - chevauchement SAT-001

-- ------------------------------------------------------------
-- T3 - trg_volume_realise
-- BEFORE INSERT OR UPDATE ON FENETRE_COM
-- RG-F05 : volume_donnees NULL si statut != 'Réalisée'
-- ------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_volume_realise
BEFORE INSERT OR UPDATE ON FENETRE_COM
FOR EACH ROW
BEGIN
    -- Si statut != Réalisée, force volume_donnees à NULL
    IF :NEW.statut != 'Réalisée' THEN
        :NEW.volume_donnees := NULL;
    END IF;
END trg_volume_realise;


-- Tests pour le trigger T3
-- Cas : tentative d'insérer un volume sur une fenêtre Planifiée
INSERT INTO FENETRE_COM VALUES (DEFAULT, TIMESTAMP '2024-02-10 08:00:00', 300, 70.0, 500.0, 'Planifiée', 'SAT-002', 'GS-KIR-01');
SELECT volume_donnees FROM FENETRE_COM WHERE datetime_debut = TIMESTAMP '2024-02-10 08:00:00';
-- Résultat attendu : volume_donnees = NULL (forcé par le trigger)

-- Cas Réalisée avec volume : volume conservé
INSERT INTO FENETRE_COM VALUES (DEFAULT, TIMESTAMP '2024-02-11 08:00:00', 300, 70.0, 750.0, 'Réalisée', 'SAT-002', 'GS-KIR-01');
-- Résultat attendu : volume_donnees = 750

-- ------------------------------------------------------------
-- T4 - trg_mission_terminee
-- BEFORE INSERT ON PARTICIPATION
-- RG-M04 : bloque l'ajout d'un satellite à une mission Terminée
-- RG-S06 : bloque l'ajout d'un satellite Désorbité à une mission
-- ------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_mission_terminee
BEFORE INSERT ON PARTICIPATION
FOR EACH ROW
DECLARE
    v_statut_mis MISSION.statut_mission%TYPE;
    v_statut_sat SATELLITE.statut%TYPE;
BEGIN
    -- Vérification statut mission (RG-M04)
    SELECT statut_mission INTO v_statut_mis
    FROM   MISSION
    WHERE  id_mission = :NEW.id_mission;

    IF v_statut_mis = 'Terminée' THEN
        RAISE_APPLICATION_ERROR(-20004,
            'ORA-20004 : La mission ' || :NEW.id_mission ||
            ' est Terminée - aucun nouveau satellite ne peut être ajouté (RG-M04).');
    END IF;

    -- Vérification statut satellite (RG-S06)
    SELECT statut INTO v_statut_sat
    FROM   SATELLITE
    WHERE  id_satellite = :NEW.id_satellite;

    IF v_statut_sat = 'Désorbité' THEN
        RAISE_APPLICATION_ERROR(-20001,
            'ORA-20001 : Le satellite ' || :NEW.id_satellite ||
            ' est Désorbité et ne peut plus être assigné à une mission (RG-S06).');
    END IF;
END trg_mission_terminee;

-- Tests pour le trigger T4
-- Cas erreur RG-M04 : mission Terminée
INSERT INTO PARTICIPATION VALUES ('SAT-003', 'MSN-DEF-2022', 'Satellite de secours');
-- Résultat attendu : ORA-20004 - MSN-DEF-2022 est Terminée

-- Cas erreur RG-S06 : satellite Désorbité
INSERT INTO PARTICIPATION VALUES ('SAT-005', 'MSN-ARC-2023', 'Satellite de relais');
-- Résultat attendu : ORA-20001 - SAT-005 est Désorbité

-- Cas valide :
INSERT INTO PARTICIPATION VALUES ('SAT-004', 'MSN-ARC-2023', 'Satellite de secours');
-- Résultat attendu : 1 ligne insérée

-- ------------------------------------------------------------
-- T5 - trg_historique_statut
-- AFTER UPDATE OF statut ON SATELLITE
-- RG-S06 traçabilité : trace tout changement de statut
-- ------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_historique_statut
AFTER UPDATE OF statut ON SATELLITE
FOR EACH ROW
WHEN (OLD.statut != NEW.statut)
BEGIN
    INSERT INTO HISTORIQUE_STATUT (
        id_satellite, ancien_statut, nouveau_statut, date_changement, motif
    ) VALUES (
        :NEW.id_satellite,
        :OLD.statut,
        :NEW.statut,
        SYSTIMESTAMP,
        'Changement automatique tracé par trigger T5'
    );
END trg_historique_statut;

-- Tests pour le trigger T5
-- Cas valide : changement de statut SAT-004
UPDATE SATELLITE SET statut = 'Défaillant' WHERE id_satellite = 'SAT-004';
SELECT * FROM HISTORIQUE_STATUT ORDER BY date_changement DESC;
-- Résultat attendu : 1 ligne avec ancien_statut='En veille', nouveau_statut='Défaillant'

-- Pas de trace si statut identique :
UPDATE SATELLITE SET statut = 'Défaillant' WHERE id_satellite = 'SAT-004';
-- Résultat attendu : 0 nouvelle ligne dans HISTORIQUE_STATUT

-- ------------------------------------------------------------
-- Vérification de la création des triggers
-- ------------------------------------------------------------
SELECT trigger_name, trigger_type, triggering_event, status
FROM   user_triggers
ORDER BY table_name, trigger_name;


-- ------------------------------------------------------------
-- Livrable L2-D - Contrôle et validation
-- ------------------------------------------------------------

-- Vérification des tables
SELECT table_name
FROM   user_tables
ORDER BY table_name;

-- Vérification des contraintes
SELECT table_name, constraint_name, constraint_type, status, search_condition
FROM   user_constraints
ORDER BY table_name, constraint_type, constraint_name;

-- Vérification des triggers
SELECT trigger_name, trigger_type, triggering_event, status
FROM   user_triggers
ORDER BY table_name, trigger_name;

-- Vérification des index
SELECT index_name, table_name, column_name, column_position
FROM   user_ind_columns
ORDER BY table_name, index_name;

-- Comptage des données insérées
SELECT 'ORBITE' AS table_name, COUNT(*) AS nb_lignes FROM ORBITE
UNION ALL
SELECT 'SATELLITE', COUNT(*) FROM SATELLITE
UNION ALL
SELECT 'INSTRUMENT', COUNT(*) FROM INSTRUMENT
UNION ALL
SELECT 'EMBARQUEMENT', COUNT(*) FROM EMBARQUEMENT
UNION ALL
SELECT 'CENTRE_CONTROLE', COUNT(*) FROM CENTRE_CONTROLE
UNION ALL
SELECT 'STATION_SOL', COUNT(*) FROM STATION_SOL
UNION ALL
SELECT 'MISSION', COUNT(*) FROM MISSION
UNION ALL
SELECT 'FENETRE_COM', COUNT(*) FROM FENETRE_COM
UNION ALL
SELECT 'PARTICIPATION', COUNT(*) FROM PARTICIPATION
UNION ALL
SELECT 'HISTORIQUE_STATUT',COUNT(*) FROM HISTORIQUE_STATUT
ORDER BY table_name;

-- Vérification des cas limites
-- SAT-005 doit être Désorbité
SELECT id_satellite, statut
FROM   SATELLITE
WHERE  id_satellite = 'SAT-005';
-- Attendu : SAT-005 | Désorbité

-- GS-SGP-01 doit être en Maintenance
SELECT code_station, statut
FROM   STATION_SOL
WHERE  code_station = 'GS-SGP-01';
-- Attendu : GS-SGP-01 | Maintenance

-- MSN-DEF-2022 doit être Terminée
SELECT id_mission, statut_mission, date_fin
FROM   MISSION
WHERE  id_mission = 'MSN-DEF-2022';
-- Attendu : MSN-DEF-2022 | Terminée | 2023-05-31

-- INS-AIS-01 doit avoir resolution NULL
SELECT ref_instrument, resolution
FROM   INSTRUMENT
WHERE  ref_instrument = 'INS-AIS-01';
-- Attendu : INS-AIS-01 | (null)

-- Fenêtres Planifiées doivent avoir volume_donnees NULL
SELECT id_fenetre, statut, volume_donnees
FROM   FENETRE_COM
WHERE  statut = 'Planifiée';
-- Attendu : id_fenetre 4 et 5 | Planifiée | (null)

-- ── 7. Test trigger T5 - vérification HISTORIQUE_STATUT ─────
UPDATE SATELLITE SET statut = 'Défaillant' WHERE id_satellite = 'SAT-004';
SELECT id_satellite, ancien_statut, nouveau_statut, date_changement
FROM   HISTORIQUE_STATUT
ORDER BY date_changement DESC;
-- Attendu : 1 ligne SAT-004 | En veille → Défaillant
ROLLBACK;
