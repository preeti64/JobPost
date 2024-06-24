package com.zenjob.challenge.controller;

import com.zenjob.challenge.dto.ResponseDto;
import com.zenjob.challenge.entity.Job;
import com.zenjob.challenge.service.JobService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping(path = "/job")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @PostMapping
    @ResponseBody
    public ResponseDto<RequestJobResponse> requestJob(@RequestBody @Valid RequestJobRequestDto dto) {
        if(!dto.getEnd().isAfter(dto.getStart())) {
            throw new IllegalArgumentException("End date should be after start date");
        }
        Job job = jobService.createJob(UUID.randomUUID(), dto.start, dto.end);
        return ResponseDto.<RequestJobResponse>builder()
                .data(RequestJobResponse.builder()
                        .jobId(job.getId())
                        .build())
                .build();
    }

    @PatchMapping(path = "/{jobId}/cancel")
    public ResponseEntity<Void> cancelJob(@PathVariable("jobId") UUID jobId) {
        jobService.cancelJob(jobId);
        return ResponseEntity.ok().build();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class RequestJobRequestDto {
        @NotNull
        private UUID companyId;
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate start;
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate end;
    }

    @Builder
    @Data
    private static class RequestJobResponse {
        UUID jobId;
    }
}
