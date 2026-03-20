package com.ranksync.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StaffData {
    private String key;
    private String name;
    private String discordId;
    private String minecraftGroup;
    private String description;
}