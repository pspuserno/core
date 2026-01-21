package com.example.doctypeeditor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "DocTypeDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocTypeDetails {

    @Id
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "Dokumenttyp", nullable = false, length = 255)
    private String dokumenttyp;

    @Column(name = "LeseKVNr", nullable = false)
    private Boolean leseKVNr;

    @Column(name = "LeseIKNr", nullable = false)
    private Boolean leseIKNr;

    @Column(name = "LeseAGNr", nullable = false)
    private Boolean leseAGNr;

    @Column(name = "FührendePartnerNr", nullable = false)
    private Integer fuhrendePartnerNr;

    @Column(name = "Prozessauslösend", nullable = false)
    private Boolean prozessauslosend;

    @Column(name = "Prozess", nullable = false, length = 255)
    private String prozess;

    @Column(name = "Aufbewahrungszeit")
    private Integer aufbewahrungszeit;

    @Column(name = "Ablagemodus")
    private Integer ablagemodus;

    @Column(name = "Code")
    private Integer code;

    @Column(name = "Fachbereich", length = 255)
    private String fachbereich;

    @Column(name = "NegativlisteRecherche", nullable = false)
    private Boolean negativlisteRecherche;

    @Column(name = "NegativlisteLöschauftrag", nullable = false)
    private Boolean negativlisteLoschauftrag;

    @Column(name = "Priorität")
    private Integer prioritat;

    @Column(name = "Eskalation1")
    private Integer eskalation1;

    @Column(name = "Aktenplan21c", length = 255)
    private String aktenplan21c;

    @Column(name = "Eskalation2")
    private Integer eskalation2;

    @Column(name = "Eskalation3")
    private Integer eskalation3;

    @Column(name = "Eskalation4")
    private Integer eskalation4;

    @Column(name = "Eskalation5")
    private Integer eskalation5;

    @Column(name = "Eskalation6")
    private Integer eskalation6;

    @Column(name = "Vorzugsdokument")
    private Boolean vorzugsdokument;

    @Column(name = "StichwortId")
    private Integer stichwortId;

    @Column(name = "Stichwort", length = 255)
    private String stichwort;

    @Column(name = "LesePartnerNr")
    private Boolean lesePartnerNr;

    @Column(name = "ExportXML")
    private Boolean exportXML;

    @Column(name = "ExportStatistik")
    private Boolean exportStatistik;

    @Column(name = "MobileRelevant")
    private Boolean mobileRelevant;

    @Column(name = "PartnerNrAll")
    private Boolean partnerNrAll;

    @Column(name = "SpecialPartnerNr", length = 255)
    private String specialPartnerNr;

    @Column(name = "Empfangsbestätigung")
    private Boolean empfangsbestatigung;

    @Column(name = "InterforumSoLe")
    private Boolean interforumSoLe;

    @Column(name = "Tieferlesung")
    private Boolean tieferlesung;

    @Column(name = "Export21cNG")
    private Boolean export21cNG;

    @Column(name = "FuzzyProjekt")
    private Boolean fuzzyProjekt;

    @Column(name = "FachbereichKz", length = 10)
    private String fachbereichKz;

    @Column(name = "FachlicheNachbearbeitung", nullable = false)
    private Boolean fachlicheNachbearbeitung;

    @Column(name = "Zustaendigkeit", length = 255)
    private String zustaendigkeit;
}
