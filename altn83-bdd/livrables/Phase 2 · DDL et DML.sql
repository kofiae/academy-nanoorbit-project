-- ------------------------------------------------------------
-- NanoOrbit - Phase 2 · Livrable L2-A et L2-B - Scripts DDL et DML
-- Schéma NANOORBIT_ADMIN · Oracle 23ai · FREEPDB1
-- Auteurs : Amel MOUCHAOUCHE, Olubusola ODUFEJO OGOE
-- ------------------------------------------------------------
-- Connexion requise : NANOORBIT_ADMIN sur FREEPDB1
-- ------------------------------------------------------------


-- ------------------------------------------------------------
-- Livrable L2-A - DDL
-- ------------------------------------------------------------

-- Nettoyage préalable
DROP TABLE IF EXISTS HISTORIQUE_STATUT CASCADE CONSTRAINTS PURGE;
DROP TABLE IF EXISTS PARTICIPATION CASCADE CONSTRAINTS PURGE;
DROP TABLE IF EXISTS FENETRE_COM CASCADE CONSTRAINTS PURGE;
DROP TABLE IF EXISTS EMBARQUEMENT CASCADE CONSTRAINTS PURGE;
DROP TABLE IF EXISTS STATION_SOL CASCADE CONSTRAINTS PURGE;
DROP TABLE IF EXISTS CENTRE_CONTROLE CASCADE CONSTRAINTS PURGE;
DROP TABLE IF EXISTS MISSION CASCADE CONSTRAINTS PURGE;
DROP TABLE IF EXISTS INSTRUMENT CASCADE CONSTRAINTS PURGE;
DROP TABLE IF EXISTS SATELLITE CASCADE CONSTRAINTS PURGE;
DROP TABLE IF EXISTS ORBITE CASCADE CONSTRAINTS PURGE;

-- ------------------------------------------------------------
-- TABLE 1 - ORBITE
-- ------------------------------------------------------------
CREATE TABLE ORBITE (
                        id_orbite        NUMBER          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        type_orbite      VARCHAR2(10)    NOT NULL,
                        altitude         NUMBER(5)       NOT NULL,
                        inclinaison      NUMBER(5,2)     NOT NULL,
                        periode_orbitale NUMBER(6,2)     NOT NULL,
                        excentricite     NUMBER(6,4)     NOT NULL,
                        zone_couverture  VARCHAR2(200)   NOT NULL,
                        CONSTRAINT chk_orbite_type CHECK (type_orbite IN ('SSO','LEO','MEO','GEO')),
                        CONSTRAINT uq_orbite_alt_incl UNIQUE (altitude, inclinaison)
);

COMMENT ON TABLE  ORBITE IS 'Référentiel des plans orbitaux NanoOrbit';
COMMENT ON COLUMN ORBITE.altitude   IS 'Altitude en km';
COMMENT ON COLUMN ORBITE.inclinaison IS 'Inclinaison en degrés';

-- ------------------------------------------------------------
-- TABLE 2 - SATELLITE
-- Q1 : SATELLITE ne peut pas être créé avant ORBITE car la FK id_orbite référence ORBITE.id_orbite. 
--      Cela traduit la règle de gestion RG-S02 : tout satellite doit être placé sur une orbite existante.
-- ------------------------------------------------------------
CREATE TABLE SATELLITE (
                           id_satellite       VARCHAR2(20)   NOT NULL,
                           nom_satellite      VARCHAR2(100)  NOT NULL,
                           date_lancement     DATE           NOT NULL,
                           masse              NUMBER(5,2)    NOT NULL CONSTRAINT chk_sat_masse CHECK (masse > 0),
                           format_cubesat     VARCHAR2(5)    NOT NULL,
                           statut             VARCHAR2(30)   NOT NULL,
                           duree_vie_prevue   NUMBER(4)      NOT NULL CONSTRAINT chk_sat_dvp CHECK (duree_vie_prevue > 0),
                           capacite_batterie  NUMBER(6,1)    NOT NULL CONSTRAINT chk_sat_bat CHECK (capacite_batterie > 0),
                           id_orbite          NUMBER         NOT NULL,
                           CONSTRAINT pk_satellite PRIMARY KEY (id_satellite),
                           CONSTRAINT fk_sat_orbite FOREIGN KEY (id_orbite) REFERENCES ORBITE (id_orbite),
                           CONSTRAINT chk_sat_format CHECK (format_cubesat IN ('1U','3U','6U','12U')),
                           CONSTRAINT chk_sat_statut CHECK (statut IN ('Opérationnel','En veille','Défaillant','Désorbité'))
);

-- Q4 : format_cubesat est VARCHAR2(5) avec CHECK IN car Oracle 23ai ne dispose pas de type ENUM natif. 
-- VARCHAR2 + CHECK est la solution standard pour contraindre un domaine de valeurs fixes.

COMMENT ON TABLE  SATELLITE IS 'Parc de CubeSats NanoOrbit';
COMMENT ON COLUMN SATELLITE.id_satellite  IS 'Code immuable après mise en orbite';
COMMENT ON COLUMN SATELLITE.statut IS 'Désorbité bloque fenêtres et missions';

-- ------------------------------------------------------------
-- TABLE 3 - INSTRUMENT
-- ------------------------------------------------------------
CREATE TABLE INSTRUMENT (
                            ref_instrument   VARCHAR2(20)    NOT NULL,
                            type_instrument  VARCHAR2(50)    NOT NULL,
                            modele           VARCHAR2(100)   NOT NULL,
                            resolution       NUMBER(6,1),
                            consommation     NUMBER(5,2)     NOT NULL CONSTRAINT chk_ins_conso CHECK (consommation > 0),
                            masse            NUMBER(5,3)     NOT NULL CONSTRAINT chk_ins_masse CHECK (masse > 0),
                            CONSTRAINT pk_instrument PRIMARY KEY (ref_instrument)
);

COMMENT ON TABLE  INSTRUMENT IS 'Catalogue global des instruments embarquables';
COMMENT ON COLUMN INSTRUMENT.resolution IS 'NULL pour capteurs non optiques (AIS) - RG-I01';

-- ------------------------------------------------------------
-- TABLE 4 - EMBARQUEMENT
-- ------------------------------------------------------------
CREATE TABLE EMBARQUEMENT (
                              id_satellite        VARCHAR2(20)  NOT NULL,
                              ref_instrument      VARCHAR2(20)  NOT NULL,
                              date_integration    DATE          NOT NULL,
                              etat_fonctionnement VARCHAR2(20)  NOT NULL,
                              CONSTRAINT pk_embarquement PRIMARY KEY (id_satellite, ref_instrument),
                              CONSTRAINT fk_emb_satellite  FOREIGN KEY (id_satellite)   REFERENCES SATELLITE (id_satellite),
                              CONSTRAINT fk_emb_instrument FOREIGN KEY (ref_instrument) REFERENCES INSTRUMENT (ref_instrument),
                              CONSTRAINT chk_emb_etat CHECK (etat_fonctionnement IN ('Nominal','Dégradé','Hors service'))
);

COMMENT ON TABLE EMBARQUEMENT IS 'Association N-N SATELLITE × INSTRUMENT - attributs propres à chaque embarquement';

-- ------------------------------------------------------------
-- TABLE 5 - CENTRE_CONTROLE
-- ------------------------------------------------------------
CREATE TABLE CENTRE_CONTROLE (
                                 id_centre      NUMBER          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 nom_centre     VARCHAR2(100)   NOT NULL CONSTRAINT uq_centre_nom UNIQUE,
                                 ville          VARCHAR2(50)    NOT NULL,
                                 region_geo     VARCHAR2(50)    NOT NULL,
                                 fuseau_horaire VARCHAR2(50)    NOT NULL,
                                 statut         VARCHAR2(20)    NOT NULL,
                                 CONSTRAINT chk_centre_region CHECK (region_geo IN ('Europe','Amériques','Asie-Pacifique')),
                                 CONSTRAINT chk_centre_statut CHECK (statut IN ('Actif','Inactif'))
);

COMMENT ON TABLE CENTRE_CONTROLE IS 'Centres opérationnels NanoOrbit (Paris, Houston, Singapour)';

-- ------------------------------------------------------------
-- TABLE 6 - STATION_SOL
-- ------------------------------------------------------------
CREATE TABLE STATION_SOL (
                             code_station      VARCHAR2(20)   NOT NULL,
                             nom_station       VARCHAR2(100)  NOT NULL CONSTRAINT uq_station_nom UNIQUE,
                             latitude          NUMBER(9,6)    NOT NULL,
                             longitude         NUMBER(9,6)    NOT NULL,
                             diametre_antenne  NUMBER(4,1)    NOT NULL CONSTRAINT chk_sta_diam CHECK (diametre_antenne > 0),
                             bande_frequence   VARCHAR2(10)   NOT NULL,
                             statut            VARCHAR2(20)   NOT NULL,
                             id_centre         NUMBER         NOT NULL,
                             debit_max         NUMBER(6,1)    NOT NULL,
                             CONSTRAINT pk_station_sol PRIMARY KEY (code_station),
                             CONSTRAINT fk_sta_centre FOREIGN KEY (id_centre) REFERENCES CENTRE_CONTROLE (id_centre),
                             CONSTRAINT chk_sta_lat CHECK (latitude BETWEEN -90 AND 90),    
                             CONSTRAINT chk_sta_lon CHECK (longitude BETWEEN -180 AND 180),      
                             CONSTRAINT chk_sta_bande CHECK (bande_frequence IN ('UHF','S','X','Ka')),
                             CONSTRAINT chk_sta_debit CHECK (debit_max > 0),
                             CONSTRAINT chk_sta_statut CHECK (statut IN ('Active','Maintenance','Inactive'))
);

COMMENT ON TABLE  STATION_SOL IS 'Antennes au sol mondiales NanoOrbit';
COMMENT ON COLUMN STATION_SOL.statut IS 'Maintenance bloque nouvelles fenêtres - RG-G03 (trigger T1)';
COMMENT ON COLUMN STATION_SOL.id_centre IS 'FK directe 1-N - RG-G04 (pas de table AFFECTATION_STATION)';

-- ------------------------------------------------------------
-- TABLE 7 - MISSION
-- ------------------------------------------------------------
CREATE TABLE MISSION (
                         id_mission      VARCHAR2(20)    NOT NULL,
                         nom_mission     VARCHAR2(100)   NOT NULL,
                         objectif        VARCHAR2(500)   NOT NULL,
                         zone_geo_cible  VARCHAR2(200)   NOT NULL,
                         date_debut      DATE            NOT NULL,
                         date_fin        DATE,
                         statut_mission  VARCHAR2(20)    NOT NULL,
                         CONSTRAINT pk_mission PRIMARY KEY (id_mission),
                         CONSTRAINT chk_mis_dates CHECK (date_fin IS NULL OR date_fin > date_debut),
                         CONSTRAINT chk_mis_statut CHECK (statut_mission IN ('Active','Terminée'))
);

COMMENT ON TABLE  MISSION IS 'Missions scientifiques NanoOrbit';
COMMENT ON COLUMN MISSION.date_fin IS 'NULL si mission en cours - RG-M01';
COMMENT ON COLUMN MISSION.statut_mission IS 'Terminée bloque tout ajout satellite - RG-M04 (trigger T4)';

-- ------------------------------------------------------------
-- TABLE 8 - FENETRE_COM
-- Q3 : RG-F02 (chevauchement satellite) ne peut pas être exprimée en CHECK car elle nécessite une requête sur
--      d'autres lignes de la table. Seul un trigger BEFORE INSERT OR UPDATE peut vérifier l'absence de chevauchement.
-- ------------------------------------------------------------
CREATE TABLE FENETRE_COM (
                             id_fenetre      NUMBER          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             datetime_debut  TIMESTAMP       NOT NULL,
                             duree           NUMBER(4)       NOT NULL,
                             elevation_max   NUMBER(5,2)     NOT NULL,
                             volume_donnees  NUMBER(8,1),
                             statut          VARCHAR2(20)    NOT NULL,
                             id_satellite    VARCHAR2(20)    NOT NULL,
                             code_station    VARCHAR2(20)    NOT NULL,
                             CONSTRAINT fk_fen_satellite FOREIGN KEY (id_satellite)  REFERENCES SATELLITE  (id_satellite),
                             CONSTRAINT fk_fen_station   FOREIGN KEY (code_station)  REFERENCES STATION_SOL (code_station),
                             CONSTRAINT chk_fen_duree CHECK (duree BETWEEN 1 AND 900),
                             CONSTRAINT chk_fen_statut CHECK (statut IN ('Planifiée','Réalisée'))
);

-- Q2 : RG-S06 (satellite désorbité bloqué) ne peut pas être vérifiée au niveau DDL seul car elle nécessite de lire
--      le statut dans la table SATELLITE lors de l'INSERT.
--      Solution : trigger T1 (trg_valider_fenetre) BEFORE INSERT.

COMMENT ON TABLE  FENETRE_COM IS 'Créneaux de communication satellite ↔ station';
COMMENT ON COLUMN FENETRE_COM.duree IS '[1-900] secondes - RG-F04 CHECK';
COMMENT ON COLUMN FENETRE_COM.volume_donnees IS 'NULL si Planifiée - RG-F05 (trigger T3)';

-- ------------------------------------------------------------
-- TABLE 9 - PARTICIPATION
-- ------------------------------------------------------------
CREATE TABLE PARTICIPATION (
                               id_satellite    VARCHAR2(20)    NOT NULL,
                               id_mission      VARCHAR2(20)    NOT NULL,
                               role_satellite  VARCHAR2(100)   NOT NULL,
                               CONSTRAINT pk_participation PRIMARY KEY (id_satellite, id_mission),
                               CONSTRAINT fk_par_satellite FOREIGN KEY (id_satellite) REFERENCES SATELLITE (id_satellite),
                               CONSTRAINT fk_par_mission   FOREIGN KEY (id_mission)   REFERENCES MISSION   (id_mission)
);

COMMENT ON TABLE PARTICIPATION IS 'Association N-N SATELLITE x MISSION - rôle propre à chaque participation';

-- ------------------------------------------------------------
-- TABLE 10 - HISTORIQUE_STATUT
-- Alimentée par le trigger T5
-- ------------------------------------------------------------
CREATE TABLE HISTORIQUE_STATUT (
                                   id_historique   NUMBER          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   id_satellite    VARCHAR2(20)    NOT NULL,
                                   ancien_statut   VARCHAR2(30)    NOT NULL,
                                   nouveau_statut  VARCHAR2(30)    NOT NULL,
                                   date_changement TIMESTAMP       NOT NULL,
                                   motif           VARCHAR2(255),
                                   CONSTRAINT fk_hist_satellite FOREIGN KEY (id_satellite) REFERENCES SATELLITE (id_satellite)
);

COMMENT ON TABLE  HISTORIQUE_STATUT IS 'Traçabilité des changements de statut - peuplée par trigger T5 uniquement';

-- ------------------------------------------------------------
-- INDEX
-- ------------------------------------------------------------
CREATE INDEX idx_satellite_statut    ON SATELLITE    (statut);
CREATE INDEX idx_satellite_orbite    ON SATELLITE    (id_orbite);
CREATE INDEX idx_fen_satellite       ON FENETRE_COM  (id_satellite);
CREATE INDEX idx_fen_station         ON FENETRE_COM  (code_station);
CREATE INDEX idx_fen_debut           ON FENETRE_COM  (datetime_debut);
CREATE INDEX idx_par_mission         ON PARTICIPATION (id_mission);
CREATE INDEX idx_emb_instrument      ON EMBARQUEMENT (ref_instrument);
CREATE INDEX idx_sta_centre          ON STATION_SOL  (id_centre);
CREATE INDEX idx_hist_satellite      ON HISTORIQUE_STATUT (id_satellite);

-- ------------------------------------------------------------
-- Vérification
-- ------------------------------------------------------------
SELECT table_name, num_rows
FROM   user_tables
WHERE  table_name IN (
                      'ORBITE','SATELLITE','INSTRUMENT','EMBARQUEMENT',
                      'CENTRE_CONTROLE','STATION_SOL','MISSION',
                      'FENETRE_COM','PARTICIPATION','HISTORIQUE_STATUT'
    )
ORDER BY table_name;



-- ------------------------------------------------------------
-- Livrable L2-b - DML
-- ------------------------------------------------------------

-- ------------------------------------------------------------
-- 1. ORBITE - 3 lignes (2 SSO, 1 LEO)
-- ------------------------------------------------------------
INSERT INTO ORBITE (type_orbite, altitude, inclinaison, periode_orbitale, excentricite, zone_couverture)
VALUES ('SSO', 550, 97.60, 95.50, 0.0010, 'Polaire globale - Europe / Arctique');

INSERT INTO ORBITE (type_orbite, altitude, inclinaison, periode_orbitale, excentricite, zone_couverture)
VALUES ('SSO', 700, 98.20, 98.80, 0.0008, 'Polaire globale - haute latitude');

INSERT INTO ORBITE (type_orbite, altitude, inclinaison, periode_orbitale, excentricite, zone_couverture)
VALUES ('LEO', 400, 51.60, 92.60, 0.0020, 'Équatoriale - zone tropicale');

-- ------------------------------------------------------------
-- 2. SATELLITE - 5 lignes (dont SAT-005 Désorbité - cas test RG-S06)
-- ------------------------------------------------------------
-- SAT-001 et SAT-002 : orbite ORB-001 (id_orbite - 1)
INSERT INTO SATELLITE (id_satellite, nom_satellite, date_lancement, masse, format_cubesat, statut, duree_vie_prevue, capacite_batterie, id_orbite)
VALUES ('SAT-001', 'NanoOrbit-Alpha',   DATE '2022-03-15', 1.30, '3U', 'Opérationnel', 60, 20.0, 1);

INSERT INTO SATELLITE (id_satellite, nom_satellite, date_lancement, masse, format_cubesat, statut, duree_vie_prevue, capacite_batterie, id_orbite)
VALUES ('SAT-002', 'NanoOrbit-Beta',    DATE '2022-03-15', 1.30, '3U', 'Opérationnel', 60, 20.0, 1);

-- SAT-003 et SAT-004 : orbite ORB-002 (id_orbite - 2)
INSERT INTO SATELLITE (id_satellite, nom_satellite, date_lancement, masse, format_cubesat, statut, duree_vie_prevue, capacite_batterie, id_orbite)
VALUES ('SAT-003', 'NanoOrbit-Gamma',   DATE '2023-06-10', 2.00, '6U', 'Opérationnel', 84, 40.0, 2);

INSERT INTO SATELLITE (id_satellite, nom_satellite, date_lancement, masse, format_cubesat, statut, duree_vie_prevue, capacite_batterie, id_orbite)
VALUES ('SAT-004', 'NanoOrbit-Delta',   DATE '2023-06-10', 2.00, '6U', 'En veille',    84, 40.0, 2);

-- SAT-005 : orbite ORB-003 (id_orbite - 3) - Désorbité, cas de test trigger T1
INSERT INTO SATELLITE (id_satellite, nom_satellite, date_lancement, masse, format_cubesat, statut, duree_vie_prevue, capacite_batterie, id_orbite)
VALUES ('SAT-005', 'NanoOrbit-Epsilon', DATE '2021-11-20', 4.50, '12U', 'Désorbité',   36, 80.0, 3);

-- ------------------------------------------------------------
-- 3. INSTRUMENT - 4 lignes (INS-AIS-01 : resolution NULL)
-- ------------------------------------------------------------
INSERT INTO INSTRUMENT (ref_instrument, type_instrument, modele, resolution, consommation, masse)
VALUES ('INS-CAM-01', 'Caméra optique',  'PlanetScope-Mini',  3.0,  2.5, 0.400);

INSERT INTO INSTRUMENT (ref_instrument, type_instrument, modele, resolution, consommation, masse)
VALUES ('INS-IR-01',  'Infrarouge',      'FLIR-Lepton-3',   160.0,  1.2, 0.150);

-- INS-AIS-01 : resolution NULL - capteur non optique (cas test NVL Phase 3)
INSERT INTO INSTRUMENT (ref_instrument, type_instrument, modele, resolution, consommation, masse)
VALUES ('INS-AIS-01', 'Récepteur AIS',   'ShipTrack-V2',     NULL,  0.8, 0.120);

INSERT INTO INSTRUMENT (ref_instrument, type_instrument, modele, resolution, consommation, masse)
VALUES ('INS-SPEC-01','Spectromètre',    'HyperSpec-Nano',   30.0,  3.1, 0.600);

-- ------------------------------------------------------------
-- 4. EMBARQUEMENT - 7 lignes
-- ------------------------------------------------------------
INSERT INTO EMBARQUEMENT (id_satellite, ref_instrument, date_integration, etat_fonctionnement)
VALUES ('SAT-001', 'INS-CAM-01', DATE '2022-03-15', 'Nominal');

INSERT INTO EMBARQUEMENT (id_satellite, ref_instrument, date_integration, etat_fonctionnement)
VALUES ('SAT-001', 'INS-IR-01',  DATE '2022-03-15', 'Nominal');

INSERT INTO EMBARQUEMENT (id_satellite, ref_instrument, date_integration, etat_fonctionnement)
VALUES ('SAT-002', 'INS-CAM-01', DATE '2022-03-15', 'Nominal');

INSERT INTO EMBARQUEMENT (id_satellite, ref_instrument, date_integration, etat_fonctionnement)
VALUES ('SAT-003', 'INS-CAM-01', DATE '2023-06-10', 'Nominal');

INSERT INTO EMBARQUEMENT (id_satellite, ref_instrument, date_integration, etat_fonctionnement)
VALUES ('SAT-003', 'INS-SPEC-01',DATE '2023-06-10', 'Nominal');

INSERT INTO EMBARQUEMENT (id_satellite, ref_instrument, date_integration, etat_fonctionnement)
VALUES ('SAT-004', 'INS-IR-01',  DATE '2023-06-10', 'Dégradé');

-- SAT-005 Désorbité - instrument Hors service (cas test RG-I04)
INSERT INTO EMBARQUEMENT (id_satellite, ref_instrument, date_integration, etat_fonctionnement)
VALUES ('SAT-005', 'INS-AIS-01', DATE '2021-11-20', 'Hors service');

-- ------------------------------------------------------------
-- 5. CENTRE_CONTROLE - 2 lignes initiales (CTR-003 via MERGE Phase 4)
-- ------------------------------------------------------------
INSERT INTO CENTRE_CONTROLE (nom_centre, ville, region_geo, fuseau_horaire, statut)
VALUES ('NanoOrbit Paris HQ', 'Paris',    'Europe',        'Europe/Paris',    'Actif');

INSERT INTO CENTRE_CONTROLE (nom_centre, ville, region_geo, fuseau_horaire, statut)
VALUES ('NanoOrbit Houston',  'Houston',  'Amériques',     'America/Chicago', 'Actif');

-- ------------------------------------------------------------
-- 6. STATION_SOL - 3 lignes (GS-SGP-01 : Maintenance - cas test RG-G03)
-- id_centre : CTR-001 - 1, CTR-002 - 2
-- ------------------------------------------------------------
INSERT INTO STATION_SOL (code_station, nom_station, latitude, longitude, diametre_antenne, bande_frequence, debit_max, statut, id_centre)
VALUES ('GS-TLS-01', 'Toulouse Ground Station', 43.604700, 1.444200, 3.5, 'S', 150.0, 'Active',      1);

INSERT INTO STATION_SOL (code_station, nom_station, latitude, longitude, diametre_antenne, bande_frequence, debit_max, statut, id_centre)
VALUES ('GS-KIR-01', 'Kiruna Arctic Station',   67.855700, 20.225300, 5.4, 'X', 400.0, 'Active',      1);

-- GS-SGP-01 : Maintenance - trigger T1 doit bloquer toute fenêtre vers cette station
INSERT INTO STATION_SOL (code_station, nom_station, latitude, longitude, diametre_antenne, bande_frequence, debit_max, statut, id_centre)
VALUES ('GS-SGP-01', 'Singapore Station',        1.352100, 103.819800, 3.0, 'S', 120.0, 'Maintenance', 2);

-- ------------------------------------------------------------
-- 7. MISSION - 3 lignes (MSN-DEF-2022 : Terminée - cas test RG-M04)
-- ------------------------------------------------------------
INSERT INTO MISSION (id_mission, nom_mission, objectif, zone_geo_cible, date_debut, date_fin, statut_mission)
VALUES ('MSN-ARC-2023', 'ArcticWatch 2023',
        'Surveillance de la fonte des glaces et dynamique des banquises arctiques',
        'Arctique / Groenland',
        DATE '2023-01-01', NULL, 'Active');

-- MSN-DEF-2022 : Terminée - trigger T4 doit bloquer tout ajout satellite
INSERT INTO MISSION (id_mission, nom_mission, objectif, zone_geo_cible, date_debut, date_fin, statut_mission)
VALUES ('MSN-DEF-2022', 'DeforestAlert',
        'Détection et cartographie de la déforestation en temps quasi-réel',
        'Amazonie / Congo',
        DATE '2022-06-01', DATE '2023-05-31', 'Terminée');

INSERT INTO MISSION (id_mission, nom_mission, objectif, zone_geo_cible, date_debut, date_fin, statut_mission)
VALUES ('MSN-COAST-2024', 'CoastGuard 2024',
        'Surveillance de l''évolution du trait de côte et détection d''érosion côtière',
        'Méditerranée / Atlantique',
        DATE '2024-03-01', NULL, 'Active');

-- ------------------------------------------------------------
-- 8. FENETRE_COM - 5 lignes (3 Réalisées, 2 Planifiées)
-- Les fenêtres Planifiées ont volume_donnees NULL (RG-F05)
-- ------------------------------------------------------------
INSERT INTO FENETRE_COM (datetime_debut, duree, elevation_max, volume_donnees, statut, id_satellite, code_station)
VALUES (TIMESTAMP '2024-01-15 09:14:00', 420, 82.30, 1250.0, 'Réalisée',  'SAT-001', 'GS-KIR-01');

INSERT INTO FENETRE_COM (datetime_debut, duree, elevation_max, volume_donnees, statut, id_satellite, code_station)
VALUES (TIMESTAMP '2024-01-15 11:52:00', 310, 67.10,  890.0, 'Réalisée',  'SAT-002', 'GS-TLS-01');

INSERT INTO FENETRE_COM (datetime_debut, duree, elevation_max, volume_donnees, statut, id_satellite, code_station)
VALUES (TIMESTAMP '2024-01-16 08:30:00', 540, 88.90, 1680.0, 'Réalisée',  'SAT-003', 'GS-KIR-01');

-- Fenêtres Planifiées - volume_donnees NULL (trigger T3 vérifie cette contrainte)
INSERT INTO FENETRE_COM (datetime_debut, duree, elevation_max, volume_donnees, statut, id_satellite, code_station)
VALUES (TIMESTAMP '2024-01-20 14:22:00', 380, 71.40,   NULL, 'Planifiée', 'SAT-001', 'GS-TLS-01');

INSERT INTO FENETRE_COM (datetime_debut, duree, elevation_max, volume_donnees, statut, id_satellite, code_station)
VALUES (TIMESTAMP '2024-01-21 07:45:00', 290, 59.80,   NULL, 'Planifiée', 'SAT-003', 'GS-TLS-01');

-- ------------------------------------------------------------
-- 9. PARTICIPATION - 7 lignes
-- ------------------------------------------------------------
INSERT INTO PARTICIPATION (id_satellite, id_mission, role_satellite)
VALUES ('SAT-001', 'MSN-ARC-2023',  'Imageur principal');

INSERT INTO PARTICIPATION (id_satellite, id_mission, role_satellite)
VALUES ('SAT-002', 'MSN-ARC-2023',  'Imageur secondaire');

INSERT INTO PARTICIPATION (id_satellite, id_mission, role_satellite)
VALUES ('SAT-003', 'MSN-ARC-2023',  'Satellite de relais');

INSERT INTO PARTICIPATION (id_satellite, id_mission, role_satellite)
VALUES ('SAT-001', 'MSN-DEF-2022',  'Imageur principal');

-- SAT-005 dans MSN-DEF-2022 (Terminée) - valide car antérieur à la désobritation
INSERT INTO PARTICIPATION (id_satellite, id_mission, role_satellite)
VALUES ('SAT-005', 'MSN-DEF-2022',  'Imageur secondaire');

INSERT INTO PARTICIPATION (id_satellite, id_mission, role_satellite)
VALUES ('SAT-003', 'MSN-COAST-2024','Imageur principal');

INSERT INTO PARTICIPATION (id_satellite, id_mission, role_satellite)
VALUES ('SAT-004', 'MSN-COAST-2024','Satellite de secours');

COMMIT;
