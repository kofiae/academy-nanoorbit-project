-- ------------------------------------------------------------
-- NanoOrbit - Phase 3 · Livrable L3-A - Paliers 1 à 5
-- ------------------------------------------------------------

SET SERVEROUTPUT ON;

-- ------------------------------------------------------------
-- PALIER 1 - BLOCS ANONYMES
-- ------------------------------------------------------------

-- Ex. 1 : Message de bienvenue + comptages
DECLARE
    v_nb_satellites NUMBER;
    v_nb_stations   NUMBER;
    v_nb_missions   NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_nb_satellites FROM SATELLITE;
    SELECT COUNT(*) INTO v_nb_stations   FROM STATION_SOL;
    SELECT COUNT(*) INTO v_nb_missions   FROM MISSION;

    DBMS_OUTPUT.PUT_LINE('--- Welcome to NanoOrbit Ground Control ---');
    DBMS_OUTPUT.PUT_LINE('Satellites   : ' || v_nb_satellites);
    DBMS_OUTPUT.PUT_LINE('Stations sol : ' || v_nb_stations);
    DBMS_OUTPUT.PUT_LINE('Missions     : ' || v_nb_missions);
END;
/
-- Résultat attendu :
-- --- Welcome to NanoOrbit Ground Control ---
-- Satellites   : 5
-- Stations sol : 3
-- Missions     : 3

-- Ex. 2 : SELECT INTO sur SAT-001 
DECLARE
    v_nom    SATELLITE.nom_satellite%TYPE;
    v_statut SATELLITE.statut%TYPE;
    v_format SATELLITE.format_cubesat%TYPE;
    v_masse  SATELLITE.masse%TYPE;
BEGIN
    SELECT nom_satellite, statut, format_cubesat, masse
    INTO   v_nom, v_statut, v_format, v_masse
    FROM   SATELLITE
    WHERE  id_satellite = 'SAT-001';

    DBMS_OUTPUT.PUT_LINE('Satellite : ' || v_nom);
    DBMS_OUTPUT.PUT_LINE('Statut    : ' || v_statut);
    DBMS_OUTPUT.PUT_LINE('Format    : ' || v_format);
    DBMS_OUTPUT.PUT_LINE('Masse     : ' || v_masse || ' kg');
END;
/
-- Résultat attendu :
-- Satellite : NanoOrbit-Alpha
-- Statut    : Opérationnel
-- Format    : 3U
-- Masse     : 1.3 kg

-- ------------------------------------------------------------
-- PALIER 2 - VARIABLES ET TYPES
-- ------------------------------------------------------------

-- Ex. 3 : %ROWTYPE sur SATELLITE
DECLARE
    v_sat SATELLITE%ROWTYPE;
BEGIN
    SELECT * INTO v_sat
    FROM   SATELLITE
    WHERE  id_satellite = 'SAT-003';

    DBMS_OUTPUT.PUT_LINE('--- Fiche SAT-003 ---');
    DBMS_OUTPUT.PUT_LINE('Nom             : ' || v_sat.nom_satellite);
    DBMS_OUTPUT.PUT_LINE('Statut          : ' || v_sat.statut);
    DBMS_OUTPUT.PUT_LINE('Capacité batterie : ' || v_sat.capacite_batterie || ' Wh');
    DBMS_OUTPUT.PUT_LINE('Durée de vie    : ' || v_sat.duree_vie_prevue || ' mois');
END;
/
-- Résultat attendu :
-- --- Fiche SAT-003 ---
-- Nom             : NanoOrbit-Gamma
-- Statut          : Opérationnel
-- Capacité batterie : 40 Wh
-- Durée de vie    : 84 mois

-- Ex. 4 : NVL sur résolution - INS-AIS-01 NULL 
DECLARE
    v_ref        INSTRUMENT.ref_instrument%TYPE;
    v_modele     INSTRUMENT.modele%TYPE;
    v_resolution VARCHAR2(20);
BEGIN
    FOR rec IN (SELECT ref_instrument, modele, resolution FROM INSTRUMENT) LOOP
            v_resolution := NVL(TO_CHAR(rec.resolution), 'N/A');
            IF v_resolution != 'N/A' THEN
                v_resolution := v_resolution || ' m';
            END IF;
            DBMS_OUTPUT.PUT_LINE(rec.ref_instrument || ' | ' || rec.modele || ' | Résolution : ' || v_resolution);
        END LOOP;
END;
/
-- Résultat attendu :
-- INS-CAM-01 | PlanetScope-Mini | Résolution : 3 m
-- INS-IR-01  | FLIR-Lepton-3   | Résolution : 160 m
-- INS-AIS-01 | ShipTrack-V2    | Résolution : N/A
-- INS-SPEC-01| HyperSpec-Nano  | Résolution : 30 m

-- ------------------------------------------------------------
-- PALIER 3 - STRUCTURES DE CONTRÔLE
-- ------------------------------------------------------------

-- Ex. 5 : IF/ELSIF - catégorisation satellite
DECLARE
    v_sat     SATELLITE%ROWTYPE;
    v_categorie VARCHAR2(50);
BEGIN
    SELECT * INTO v_sat FROM SATELLITE WHERE id_satellite = 'SAT-004';

    IF v_sat.statut = 'Opérationnel' AND v_sat.duree_vie_prevue > 60 THEN
        v_categorie := 'Longue durée opérationnel';
    ELSIF v_sat.statut = 'Opérationnel' THEN
        v_categorie := 'Opérationnel standard';
    ELSIF v_sat.statut = 'En veille' THEN
        v_categorie := 'En veille - réactivation possible';
    ELSIF v_sat.statut = 'Défaillant' THEN
        v_categorie := 'Défaillant - inspection requise';
    ELSE
        v_categorie := 'Désorbité - hors service';
    END IF;

    DBMS_OUTPUT.PUT_LINE(v_sat.id_satellite || ' (' || v_sat.statut || ') : ' || v_categorie);
END;
/
-- Résultat attendu :
-- SAT-004 (En veille) : En veille - réactivation possible

-- Ex. 6 : CASE - type d'orbite + vitesse orbitale
DECLARE
    v_sat    SATELLITE%ROWTYPE;
    v_orb    ORBITE%ROWTYPE;
    v_type   VARCHAR2(20);
    v_vitesse NUMBER;
    -- v = 2π × (6371 + altitude) / période (en km/min → km/s)
BEGIN
    SELECT s.* INTO v_sat FROM SATELLITE s WHERE id_satellite = 'SAT-001';
    SELECT o.* INTO v_orb FROM ORBITE o WHERE id_orbite = v_sat.id_orbite;

    v_vitesse := ROUND(2 * 3.14159265 * (6371 + v_orb.altitude) / (v_orb.periode_orbitale * 60), 3);

    v_type := CASE v_orb.type_orbite
        WHEN 'SSO' THEN 'Orbite héliosynchrone (polaire)'
        WHEN 'LEO' THEN 'Orbite basse terrestre'
        WHEN 'MEO' THEN 'Orbite moyenne terrestre'
        WHEN 'GEO' THEN 'Orbite géostationnaire'
        ELSE 'Type inconnu'
    END;

    DBMS_OUTPUT.PUT_LINE('Satellite   : ' || v_sat.nom_satellite);
    DBMS_OUTPUT.PUT_LINE('Type orbite : ' || v_type);
    DBMS_OUTPUT.PUT_LINE('Altitude    : ' || v_orb.altitude || ' km');
    DBMS_OUTPUT.PUT_LINE('Vitesse     : ' || v_vitesse || ' km/s');
END;
/
-- Résultat attendu :
-- Satellite   : NanoOrbit-Alpha
-- Type orbite : Orbite héliosynchrone (polaire)
-- Altitude    : 550 km
-- Vitesse     : 7.596 km/s (approx.)

-- Ex. 7 : Boucle FOR - grille de volumes
DECLARE
    v_debit    STATION_SOL.debit_max%TYPE;
    v_volume   NUMBER;
BEGIN
    SELECT debit_max INTO v_debit
    FROM   STATION_SOL
    WHERE  code_station = 'GS-TLS-01';

    DBMS_OUTPUT.PUT_LINE('--- Grille volumes GS-TLS-01 (débit : ' || v_debit || ' Mbps) ---');
    DBMS_OUTPUT.PUT_LINE('Durée (min) | Volume (Mo)');
    DBMS_OUTPUT.PUT_LINE('------------|------------');

    FOR i IN 5..15 LOOP
        -- Volume = débit (Mbps) × durée (s) / 8 (conversion bits → octets)
        v_volume := ROUND(v_debit * (i * 60) / 8, 1);
        DBMS_OUTPUT.PUT_LINE(LPAD(i, 10) || ' min | ' || v_volume || ' Mo');
    END LOOP;
END;
/
-- Résultat attendu (extraits) :
--  5 min | 5625 Mo
-- 10 min | 11250 Mo
-- 15 min | 16875 Mo

-- ------------------------------------------------------------
-- PALIER 4 - CURSEURS
-- ------------------------------------------------------------

-- Ex. 8 : SQL%ROWCOUNT
DECLARE
    v_nb NUMBER;
BEGIN
    UPDATE SATELLITE
    SET    statut = 'En veille'
    WHERE  statut = 'Opérationnel'
    AND    duree_vie_prevue < 70;

    v_nb := SQL%ROWCOUNT;
    DBMS_OUTPUT.PUT_LINE(v_nb || ' satellite(s) passé(s) en veille.');
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('ROLLBACK effectué');
END;
/
-- Résultat attendu :
-- 2 satellites passés en veille. (SAT-001 et SAT-002, duree_vie_prevue=60)
-- ROLLBACK effectué

-- Ex. 9 : Cursor FOR Loop - satellites + orbite + instruments
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- Constellation NanoOrbit ---');
    FOR sat IN (
        SELECT s.id_satellite, s.nom_satellite, s.statut,
               o.type_orbite, o.altitude,
               COUNT(e.ref_instrument) AS nb_instruments
        FROM   SATELLITE s
        JOIN   ORBITE o ON o.id_orbite = s.id_orbite
        LEFT JOIN EMBARQUEMENT e ON e.id_satellite = s.id_satellite
        GROUP BY s.id_satellite, s.nom_satellite, s.statut, o.type_orbite, o.altitude
        ORDER BY s.id_satellite
    ) LOOP
        DBMS_OUTPUT.PUT_LINE(
            sat.id_satellite || ' | ' || sat.nom_satellite ||
            ' | ' || sat.statut ||
            ' | ' || sat.type_orbite || ' ' || sat.altitude || 'km' ||
            ' | ' || sat.nb_instruments || ' instrument(s)'
        );
    END LOOP;
END;
/
-- Résultat attendu :
-- SAT-001 | NanoOrbit-Alpha   | Opérationnel | SSO 550km | 2 instrument(s)
-- SAT-002 | NanoOrbit-Beta    | Opérationnel | SSO 550km | 1 instrument(s)
-- SAT-003 | NanoOrbit-Gamma   | Opérationnel | SSO 700km | 2 instrument(s)
-- SAT-004 | NanoOrbit-Delta   | En veille    | SSO 700km | 1 instrument(s)
-- SAT-005 | NanoOrbit-Epsilon | Désorbité    | LEO 400km | 1 instrument(s)

-- Ex. 10 : Curseur explicite OPEN/FETCH/CLOSE
DECLARE
    CURSOR c_sat_fenetre IS
        SELECT s.id_satellite, s.nom_satellite, s.statut,
               f.datetime_debut, f.code_station
        FROM   SATELLITE s
        JOIN   FENETRE_COM f ON f.id_satellite = s.id_satellite
        WHERE  s.statut = 'Opérationnel'
        AND    f.statut = 'Réalisée'
        ORDER BY s.id_satellite, f.datetime_debut DESC;

    v_rec c_sat_fenetre%ROWTYPE;
BEGIN
    OPEN c_sat_fenetre;
    DBMS_OUTPUT.PUT_LINE('--- Satellites opérationnels - dernière fenêtre réalisée ---');
    LOOP
        FETCH c_sat_fenetre INTO v_rec;
        EXIT WHEN c_sat_fenetre%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE(
            v_rec.id_satellite || ' | ' || v_rec.nom_satellite ||
            ' | ' || v_rec.code_station ||
            ' | ' || TO_CHAR(v_rec.datetime_debut, 'DD/MM/YYYY HH24:MI')
        );
    END LOOP;
    CLOSE c_sat_fenetre;
END;
/
-- Résultat attendu :
-- SAT-001 | NanoOrbit-Alpha | GS-KIR-01 | 15/01/2024 09:14
-- SAT-002 | NanoOrbit-Beta  | GS-TLS-01 | 15/01/2024 11:52
-- SAT-003 | NanoOrbit-Gamma | GS-KIR-01 | 16/01/2024 08:30

-- Ex. 11 : Curseur paramétré
DECLARE
    CURSOR c_fenetres(p_station VARCHAR2) IS
        SELECT f.id_fenetre, f.datetime_debut, f.duree,
               f.statut, f.volume_donnees
        FROM   FENETRE_COM f
        WHERE  f.code_station = p_station
        ORDER BY f.datetime_debut;

    v_volume_total NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- Fenêtres GS-KIR-01 ---');
    FOR rec IN c_fenetres('GS-KIR-01') LOOP
        DBMS_OUTPUT.PUT_LINE(
            'Fenêtre ' || rec.id_fenetre ||
            ' | ' || TO_CHAR(rec.datetime_debut, 'DD/MM/YYYY HH24:MI') ||
            ' | ' || rec.duree || 's' ||
            ' | ' || rec.statut ||
            ' | Volume : ' || NVL(TO_CHAR(rec.volume_donnees), 'NULL') || ' Mo'
        );
        v_volume_total := v_volume_total + NVL(rec.volume_donnees, 0);
    END LOOP;
    DBMS_OUTPUT.PUT_LINE('Volume total téléchargé : ' || v_volume_total || ' Mo');
END;
/
-- Résultat attendu :
-- Fenêtre 1 | 15/01/2024 09:14 | 420s | Réalisée | Volume : 1250 Mo
-- Fenêtre 3 | 16/01/2024 08:30 | 540s | Réalisée | Volume : 1680 Mo
-- Volume total téléchargé : 2930 Mo

-- ------------------------------------------------------------
-- PALIER 5 - PROCÉDURES ET FONCTIONS STANDALONE
-- ------------------------------------------------------------

-- Ex. 12 : Exceptions prédéfinies 
DECLARE
    v_sat SATELLITE%ROWTYPE;
BEGIN
    SELECT * INTO v_sat
    FROM   SATELLITE
    WHERE  id_satellite = 'SAT-999'; -- n'existe pas
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('ERREUR : Satellite SAT-999 introuvable (NO_DATA_FOUND).');
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERREUR inattendue : ' || SQLERRM);
END;
/
-- Résultat attendu :
-- ERREUR : Satellite SAT-999 introuvable (NO_DATA_FOUND).

-- Ex. 13 : RAISE_APPLICATION_ERROR - validation fenêtre
DECLARE
    v_statut_sat SATELLITE.statut%TYPE;
    v_statut_sta STATION_SOL.statut%TYPE;
    v_duree      NUMBER := 950; -- dépasse 900 - doit lever une erreur
    v_id_sat     VARCHAR2(20) := 'SAT-001';
    v_code_sta   VARCHAR2(20) := 'GS-TLS-01';
BEGIN
    -- Vérification satellite
    SELECT statut INTO v_statut_sat FROM SATELLITE WHERE id_satellite = v_id_sat;
    IF v_statut_sat = 'Désorbité' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Satellite ' || v_id_sat || ' Désorbité.');
    END IF;

    -- Vérification station
    SELECT statut INTO v_statut_sta FROM STATION_SOL WHERE code_station = v_code_sta;
    IF v_statut_sta = 'Maintenance' THEN
        RAISE_APPLICATION_ERROR(-20002, 'Station ' || v_code_sta || ' en Maintenance.');
    END IF;

    -- Vérification durée
    IF v_duree < 1 OR v_duree > 900 THEN
        RAISE_APPLICATION_ERROR(-20005, 'Durée ' || v_duree || 's invalide - doit être entre 1 et 900 (RG-F04).');
    END IF;

    DBMS_OUTPUT.PUT_LINE('Fenêtre valide - insertion autorisée.');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Validation échouée : ' || SQLERRM);
END;
/
-- Résultat attendu :
-- Validation échouée : ORA-20005: Durée 950s invalide - doit être entre 1 et 900 (RG-F04)

-- Ex. 14 : Procédure afficher_statut_satellite 
CREATE OR REPLACE PROCEDURE afficher_statut_satellite(
    p_id IN VARCHAR2
) AS
    v_sat  SATELLITE%ROWTYPE;
    v_orb  ORBITE%ROWTYPE;
BEGIN
    BEGIN
        SELECT * INTO v_sat FROM SATELLITE WHERE id_satellite = p_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20010, 'Satellite ' || p_id || ' introuvable.');
    END;

    SELECT * INTO v_orb FROM ORBITE WHERE id_orbite = v_sat.id_orbite;

    DBMS_OUTPUT.PUT_LINE('--- ' || v_sat.id_satellite || ' - ' || v_sat.nom_satellite || ' ---');
    DBMS_OUTPUT.PUT_LINE('Statut  : ' || v_sat.statut);
    DBMS_OUTPUT.PUT_LINE('Format  : ' || v_sat.format_cubesat);
    DBMS_OUTPUT.PUT_LINE('Orbite  : ' || v_orb.type_orbite || ' - ' || v_orb.altitude || ' km');
    DBMS_OUTPUT.PUT_LINE('Instruments :');

    FOR ins IN (
        SELECT i.ref_instrument, i.type_instrument, e.etat_fonctionnement
        FROM   EMBARQUEMENT e
        JOIN   INSTRUMENT i ON i.ref_instrument = e.ref_instrument
        WHERE  e.id_satellite = p_id
    ) LOOP
        DBMS_OUTPUT.PUT_LINE('  • ' || ins.ref_instrument || ' (' || ins.type_instrument || ') - ' || ins.etat_fonctionnement);
    END LOOP;
END afficher_statut_satellite;
/
SHOW ERRORS PROCEDURE afficher_statut_satellite;

-- Test Ex. 14
BEGIN afficher_statut_satellite('SAT-001'); END;
/
-- Résultat attendu :
-- --- SAT-001 - NanoOrbit-Alpha ---
-- Statut  : Opérationnel
-- Format  : 3U
-- Orbite  : SSO - 550 km
-- Instruments :
--   • INS-CAM-01 (Caméra optique) - Nominal
--   • INS-IR-01 (Infrarouge) - Nominal

-- Ex. 15 : Procédure mettre_a_jour_statut
CREATE OR REPLACE PROCEDURE mettre_a_jour_statut(
    p_id            IN  VARCHAR2,
    p_statut        IN  VARCHAR2,
    p_ancien_statut OUT VARCHAR2
) AS
BEGIN
    SELECT statut INTO p_ancien_statut
    FROM   SATELLITE
    WHERE  id_satellite = p_id;

    IF p_statut NOT IN ('Opérationnel','En veille','Défaillant','Désorbité') THEN
        RAISE_APPLICATION_ERROR(-20011, 'Statut invalide : ' || p_statut);
    END IF;

    UPDATE SATELLITE
    SET    statut = p_statut
    WHERE  id_satellite = p_id;

    DBMS_OUTPUT.PUT_LINE(p_id || ' : ' || p_ancien_statut || ' → ' || p_statut);
    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20010, 'Satellite ' || p_id || ' introuvable.');
END mettre_a_jour_statut;
/
SHOW ERRORS PROCEDURE mettre_a_jour_statut;

-- Test Ex. 15
DECLARE
    v_ancien VARCHAR2(30);
BEGIN
    mettre_a_jour_statut('SAT-004', 'Défaillant', v_ancien);
    DBMS_OUTPUT.PUT_LINE('Ancien statut récupéré : ' || v_ancien);
    -- Remise en état
    mettre_a_jour_statut('SAT-004', 'En veille', v_ancien);
END;
/
-- Résultat attendu :
-- SAT-004 : En veille → Défaillant
-- Ancien statut récupéré : En veille
-- SAT-004 : Défaillant → En veille

-- Ex. 16 : Fonction calculer_volume_session
CREATE OR REPLACE FUNCTION calculer_volume_theorique(
    p_id_fenetre IN NUMBER
) RETURN NUMBER AS
    v_duree      FENETRE_COM.duree%TYPE;
    v_debit      STATION_SOL.debit_max%TYPE;
    v_volume     NUMBER;
BEGIN
    SELECT f.duree, s.debit_max
    INTO   v_duree, v_debit
    FROM   FENETRE_COM f
    JOIN   STATION_SOL s ON s.code_station = f.code_station
    WHERE  f.id_fenetre = p_id_fenetre;

    -- Volume théorique = débit (Mbps) × durée (s) / 8 (Mo)
    v_volume := ROUND(v_debit * v_duree / 8, 1);
    RETURN v_volume;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20012, 'Fenêtre ' || p_id_fenetre || ' introuvable.');
END calculer_volume_theorique;
/
SHOW ERRORS FUNCTION calculer_volume_theorique;

-- Test Ex. 16
DECLARE
    v_vol NUMBER;
BEGIN
    FOR i IN 1..5 LOOP
        v_vol := calculer_volume_theorique(i);
        DBMS_OUTPUT.PUT_LINE('Fenêtre ' || i || ' - Volume théorique : ' || v_vol || ' Mo');
    END LOOP;
END;
/
-- Résultat attendu :
-- Fenêtre 1 - Volume théorique : 21000 Mo  (400 Mbps × 420s / 8)
-- Fenêtre 2 - Volume théorique : 5812.5 Mo (150 Mbps × 310s / 8)
-- Fenêtre 3 - Volume théorique : 27000 Mo  (400 Mbps × 540s / 8)
-- Fenêtre 4 - Volume théorique : 7125 Mo   (150 Mbps × 380s / 8)
-- Fenêtre 5 - Volume théorique : 5437.5 Mo (150 Mbps × 290s / 8)