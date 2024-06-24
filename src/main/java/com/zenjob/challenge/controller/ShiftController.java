package com.zenjob.challenge.controller;

import com.zenjob.challenge.controller.exceptions.ResourceNotFoundException;
import com.zenjob.challenge.dto.JobDto;
import com.zenjob.challenge.dto.ResponseDto;
import com.zenjob.challenge.entity.Job;
import com.zenjob.challenge.entity.Shift;
import com.zenjob.challenge.service.JobService;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping(path = "/shift")
@RequiredArgsConstructor
public class ShiftController {
    private final JobService jobService;

    @GetMapping(path = "/{jobId}")
    @ResponseBody
    public ResponseDto<GetShiftsResponse> getShifts(@PathVariable("jobId") UUID uuid) {
        Job job = jobService.findJobById(uuid);
        if(job == null) {
            throw new ResourceNotFoundException("Job not found with id : " + uuid);
        }
        List<ShiftResponse> shiftResponses = jobService.getShifts(uuid).stream()
                .map(shift -> ShiftResponse.builder()
                        .id(shift.getId())
                        .talentId(shift.getTalentId())
                        .jobId(shift.getJob().getId())
                        .start(shift.getCreatedAt())
                        .end(shift.getEndTime())
                        .build())
                .collect(Collectors.toList());
        if(shiftResponses.isEmpty()) {
           throw new IllegalArgumentException("A job should have at least one shift");
        }
        return ResponseDto.<GetShiftsResponse>builder()
                .data(GetShiftsResponse.builder()
                        .shifts(shiftResponses)
                        .build())
                .build();
    }

    @PatchMapping(path = "/{id}/book")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> bookTalent(@PathVariable("id") UUID shiftId, @RequestBody @Valid ShiftController.BookTalentRequestDto dto) {
        Shift shift = jobService.findShiftById(shiftId);
        if (shift == null) {
            throw new ResourceNotFoundException("Shift not found with id : " + shiftId);
        }
        jobService.bookTalent(shiftId, dto.talent);
        return ResponseEntity.status((HttpStatus.OK)).build();
    }

    @PatchMapping(path = "/{shiftId}/cancel")
    public ResponseEntity<JobDto> cancelShift(@PathVariable("shiftId") UUID shiftId) {

        JobDto updatedJob = jobService.cancelShift(shiftId);
        return ResponseEntity.ok(updatedJob);
    }

    @PatchMapping(path = "/cancelAndReplaceForTalent/{talentId}")
    public ResponseEntity<List<Shift>> cancelAndReplaceShiftsForTalent(@PathVariable("talentId") UUID talentId) {
       List<Shift> shiftsForTalents = jobService.findAllShiftsByTalentId(talentId);
       if(shiftsForTalents.isEmpty()) {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
       }
        List<Shift> updatedShifts =  jobService.cancelAndReplaceShiftsForTalent(talentId);
       return ResponseEntity.ok(updatedShifts);
    }

    @NoArgsConstructor
    @Data
    private static class BookTalentRequestDto {
        UUID talent;
    }

    @Builder
    @Data
    private static class GetShiftsResponse {
        List<ShiftResponse> shifts;
    }

    @Builder
    @Data
    private static class ShiftResponse {
        UUID    id;
        UUID    talentId;
        UUID    jobId;
        Instant start;
        Instant end;
    }
}
