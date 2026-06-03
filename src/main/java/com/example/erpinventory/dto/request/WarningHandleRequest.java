package com.example.erpinventory.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WarningHandleRequest {

    private Integer handleType;

    private String ownerName;

    private String handleNote;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followUpTime;
}
