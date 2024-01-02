package com.tujuhsembilan.app.dto.talentRequest;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class WishlistRequestDTO {
    private UUID userId;
    private List<WishlistItemDTO> wishlist;
}
