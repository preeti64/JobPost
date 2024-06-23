package com.zenjob.challenge;

import com.zenjob.challenge.entity.Job;
import com.zenjob.challenge.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.UUID;

@SpringBootTest
class JobServiceTest {

    @Autowired
    private JobService jobService;

    private UUID jobId;

    @BeforeEach
    public void setup() {
        Job job  = jobService.createJob(UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(1));
        jobId = job.getId();
    }

    @Test
    public void testCancelJob() {
        jobService.cancelJob(jobId);
    }
}
