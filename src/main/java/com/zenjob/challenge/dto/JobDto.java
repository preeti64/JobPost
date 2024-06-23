package com.zenjob.challenge.dto;

import com.zenjob.challenge.entity.JobStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class JobDto {
    private UUID id;
    private UUID comanyId;
    private Instant startTime;
    private Instant endTime;
    private JobStatus status;;
}
