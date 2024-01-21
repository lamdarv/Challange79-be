package com.tujuhsembilan.app.model;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class TalentSkillsetId implements Serializable {
    private UUID talent;
    private UUID skillset;
}
