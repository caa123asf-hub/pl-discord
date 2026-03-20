package com.ranksync.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LinkData {
    private UUID playerUuid;
    private String discordId;
    private long linkedAt;
}