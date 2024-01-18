package com.tujuhsembilan.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Data
public class DownloadCVCountRequestDTO {
    private UUID talentId;
}
