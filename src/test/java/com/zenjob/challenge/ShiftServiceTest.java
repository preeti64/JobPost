package com.zenjob.challenge;

import com.zenjob.challenge.entity.Job;
import com.zenjob.challenge.entity.Shift;
import com.zenjob.challenge.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.UUID;

@SpringBootTest
class ShiftServiceTest {

    @Autowired
    private JobService jobService;

    private UUID shiftId;

    @BeforeEach
    public void setup()
    {
        Job job = jobService.createJob(UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(1));
        Shift shift = job.getShifts().get(0);
        shiftId = shift.getId();
    }

    @Test
    public void testCancelShift()
    {
        jobService.cancelShift(shiftId);

    }

    @Test
    public void testCancelAndReplaceShift()
    {
        UUID talentId = UUID . randomUUID ();
        jobService.cancelAndReplaceShiftsForTalent(talentId);
    }
}

