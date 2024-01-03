package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.Talent;
import com.tujuhsembilan.app.model.TalentPosition;
import com.tujuhsembilan.app.model.TalentSkillset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SkillsetRequestTalentRepository extends JpaRepository<TalentSkillset, TalentSkillset.TalentSkillsetId> {
    List<TalentSkillset> findByTalent(Talent talent);
}
