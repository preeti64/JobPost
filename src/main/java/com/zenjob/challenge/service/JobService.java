package com.zenjob.challenge.service;

import com.zenjob.challenge.controller.exceptions.ResourceNotFoundException;
import com.zenjob.challenge.dto.JobDto;
import com.zenjob.challenge.entity.Job;
import com.zenjob.challenge.entity.JobStatus;
import com.zenjob.challenge.entity.Shift;
import com.zenjob.challenge.entity.ShiftStatus;
import com.zenjob.challenge.repository.JobRepository;
import com.zenjob.challenge.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@RequiredArgsConstructor
@Repository
@Transactional
public class JobService {
    private final JobRepository jobRepository;
    private final ShiftRepository shiftRepository;
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    public Job createJob(UUID uuid, LocalDate date1, LocalDate date2) {
        validateJobDates(date1, date2);

        Job job = Job.builder()
                .id(uuid)
                .companyId(UUID.randomUUID())
                .startTime(date1.atTime(8, 0, 0).toInstant(ZoneOffset.UTC))
                .endTime(date2.atTime(17, 0, 0).toInstant(ZoneOffset.UTC))
                .status(JobStatus.ACTIVE)
                .build();
        List<Shift> shifts = LongStream.range(0, ChronoUnit.DAYS.between(date1, date2))
                .mapToObj(idx -> date1.plus(idx, ChronoUnit.DAYS))
                .map(date -> Shift.builder()
                        .id(UUID.randomUUID())
                        .job(job)
                        .startTime(date.atTime(8, 0, 0).toInstant(ZoneOffset.UTC))
                        .endTime(date.atTime(17, 0, 0).toInstant(ZoneOffset.UTC))
                        .status(ShiftStatus.ACTIVE)
                        .build())
                .collect(Collectors.toList());
        job.setShifts(shifts);
        return jobRepository.save(job);
    }

    public List<Shift> getShifts(UUID jobId) {
        return shiftRepository.findAllByJobIdAndStatusNot(jobId, ShiftStatus.CANCELLED);
    }

    public List<Shift> getShiftsForTalent(UUID jobId, UUID talentId) {
        return shiftRepository.findAllByJobIdAndTalentIdAndStatusNot(jobId, talentId, ShiftStatus.CANCELLED);
    }

    public void bookTalent(UUID shiftId, UUID talent) {
        shiftRepository.findById(shiftId).map(shift -> shiftRepository.save(shift.setTalentId(talent)));
    }

    public Shift findShiftById(UUID shiftId) {
        return shiftRepository.findById(shiftId).orElseThrow(() -> new ResourceNotFoundException("Shift not found"));
    }

    public Job findJobById(UUID jobId) {
        return jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
    }

    public void validateJobDates(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        if (startDate.isBefore(today)) {
            throw new IllegalArgumentException("Start date of the job cannot be in the past");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date of the job should be after the start date");
        }
    }

    public void cancelJob(UUID jobId) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found with id:" + jobId));
        job.setStatus(JobStatus.CANCELLED);
        jobRepository.save(job);

        List<Shift> shifts = shiftRepository.findAllByJobId(jobId);
        shifts.forEach(shift -> {
            shift.setStatus(ShiftStatus.CANCELLED);
            shiftRepository.save(shift);
        });
    }

    @Transactional
    public JobDto cancelShift(UUID shiftId) {
        Shift shift = shiftRepository.findById(shiftId).orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + shiftId));
        shift.setStatus(ShiftStatus.CANCELLED);
        shift = shiftRepository.save(shift);
        shiftRepository.flush();

        Job job = shift.getJob();
        if (job.getShifts().stream().allMatch(s -> s.getStatus() == ShiftStatus.CANCELLED)) {
            job.setStatus(JobStatus.CANCELLED);
            jobRepository.save(job);
        }

        JobDto jobDto = new JobDto();
        jobDto.setId(job.getId());
        jobDto.setComanyId(job.getCompanyId());
        jobDto.setStartTime(job.getStartTime());
        jobDto.setEndTime(job.getEndTime());
        jobDto.setStatus(job.getStatus());
        return jobDto;
    }

    public List<Shift> findAllShiftsByTalentId(UUID talentId) {
        return shiftRepository.findAllByTalentId(talentId);
    }

    public List<Shift> cancelAndReplaceShiftsForTalent(UUID talentId) {
        List<Shift> shiftsForTalent = shiftRepository.findAllByTalentId(talentId);
        List<Shift> replacementShifts = new ArrayList<>();
        shiftsForTalent.forEach(shift -> {
            shift.setStatus(ShiftStatus.CANCELLED);
            shiftRepository.save(shift);

            Shift replacementShift = Shift.builder()
                    .id(UUID.randomUUID())
                    .job(shift.getJob())
                    .startTime(shift.getStartTime())
                    .endTime(shift.getEndTime())
                    .status(ShiftStatus.ACTIVE)
                    .talentId((talentId))
                    .build();
            replacementShifts.add(replacementShift);
            shiftRepository.save(replacementShift);
        });
        return getShiftsForTalent(shiftsForTalent.get(0).getJob().getId(), talentId);
    }
}
