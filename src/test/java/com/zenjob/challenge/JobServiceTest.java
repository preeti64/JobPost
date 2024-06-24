package com.zenjob.challenge;

import com.zenjob.challenge.controller.exceptions.ResourceNotFoundException;
import com.zenjob.challenge.entity.Job;
import com.zenjob.challenge.entity.JobStatus;
import com.zenjob.challenge.entity.Shift;
import com.zenjob.challenge.entity.ShiftStatus;
import com.zenjob.challenge.repository.JobRepository;
import com.zenjob.challenge.repository.ShiftRepository;
import com.zenjob.challenge.service.JobService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@SpringBootTest
class JobServiceTest {
    private JobService jobService;
    private JobRepository jobRepository;
    private ShiftRepository shiftRepository;
    ;

    private UUID jobId;

    @BeforeEach
    public void setup() {
        jobRepository = Mockito.mock(JobRepository.class);
        shiftRepository = Mockito.mock(ShiftRepository.class);
        jobService = new JobService(jobRepository, shiftRepository);

        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setStartTime(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        job.setEndTime(LocalDate.now().plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        jobId = job.getId();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
    }

    @Test
    public void testCancelJob() {

        jobService.cancelJob(jobId);
        Job job = jobRepository.findById(jobId).get();
        Assertions.assertEquals(job.getStatus(), JobStatus.CANCELLED);
    }

    @Test
    public void testCancelJobWhenJobDoesNotExist() {
        UUID nonExistentJobId = UUID.randomUUID();
        when(jobRepository.findById(nonExistentJobId)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            jobService.cancelJob(nonExistentJobId);
        });
    }

    @Test
    public void testCancelJobShiftsState() {
        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.ACTIVE);
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        List<Shift> shifts = new ArrayList<>();
        when(shiftRepository.findAllByJobId(job.getId())).thenReturn(shifts);

        jobService.cancelJob(job.getId());

        for (Shift shift : shifts) {
            Assertions.assertEquals(ShiftStatus.CANCELLED, shift.getStatus());
        }
    }

    @Test
    public void testCancelShiftWhenShiftDoesNotExist() {
        UUID nonExistentShiftId = UUID.randomUUID();
        when(shiftRepository.findById(nonExistentShiftId)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            jobService.cancelShift(nonExistentShiftId);
        });

        Mockito.verify(shiftRepository, Mockito.times(1)).findById(nonExistentShiftId);
    }

}
