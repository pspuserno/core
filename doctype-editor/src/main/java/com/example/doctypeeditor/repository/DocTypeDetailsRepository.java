package com.example.doctypeeditor.repository;

import com.example.doctypeeditor.entity.DocTypeDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocTypeDetailsRepository extends JpaRepository<DocTypeDetails, Integer> {

    @Query("SELECT DISTINCT d.dokumenttyp FROM DocTypeDetails d")
    List<String> findAllDokumenttyp();

    @Query("SELECT DISTINCT d.prozess FROM DocTypeDetails d WHERE d.prozess IS NOT NULL")
    List<String> findAllProzess();

    @Query("SELECT DISTINCT d.fachbereich FROM DocTypeDetails d WHERE d.fachbereich IS NOT NULL")
    List<String> findAllFachbereich();

    @Query("SELECT DISTINCT d.aktenplan21c FROM DocTypeDetails d WHERE d.aktenplan21c IS NOT NULL")
    List<String> findAllAktenplan21c();

    @Query("SELECT DISTINCT d.stichwort FROM DocTypeDetails d WHERE d.stichwort IS NOT NULL")
    List<String> findAllStichwort();

    @Query("SELECT DISTINCT d.specialPartnerNr FROM DocTypeDetails d WHERE d.specialPartnerNr IS NOT NULL")
    List<String> findAllSpecialPartnerNr();

    @Query("SELECT DISTINCT d.fachbereichKz FROM DocTypeDetails d WHERE d.fachbereichKz IS NOT NULL")
    List<String> findAllFachbereichKz();

    @Query("SELECT DISTINCT d.zustaendigkeit FROM DocTypeDetails d WHERE d.zustaendigkeit IS NOT NULL")
    List<String> findAllZustaendigkeit();
}
