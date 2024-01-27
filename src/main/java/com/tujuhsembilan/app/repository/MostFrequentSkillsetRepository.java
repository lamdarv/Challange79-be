package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.MostFrequentSkillset;
import com.tujuhsembilan.app.model.Skillset;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootApplication
@Repository
public interface MostFrequentSkillsetRepository extends JpaRepository<MostFrequentSkillset, UUID> {

    // Mencari lima MostFrequentSkillset teratas yang memiliki counter tidak null, diurutkan berdasarkan counter secara descending
    List<MostFrequentSkillset> findTop5ByCounterIsNotNullOrderByCounterDesc();

    // Mencari MostFrequentSkillset berdasarkan Skillset, mengembalikan Optional
    Optional<MostFrequentSkillset> findBySkillset(Skillset skillset);
}

