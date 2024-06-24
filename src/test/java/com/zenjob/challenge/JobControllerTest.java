package com.zenjob.challenge;

import com.zenjob.challenge.controller.exceptions.ResourceNotFoundException;
import com.zenjob.challenge.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    private UUID jobId;

    @BeforeEach
    public void setup() {
        jobId = UUID.randomUUID();
    }


    @Test
    public void testCancelJob() throws Exception {
        UUID jobId = UUID.randomUUID();
        doNothing().when(jobService).cancelJob(jobId);
        mockMvc.perform(patch("/job/" + jobId + "/cancel"))
                .andExpect(status().isOk());
    }

    @Test
    public void testCancelJobWhenJobDoesNotExist() throws Exception {
        UUID nonExistentJobId = UUID.randomUUID();
        Mockito.doThrow(ResourceNotFoundException.class).when(jobService).cancelJob(nonExistentJobId);

        mockMvc.perform(patch("/job/" + nonExistentJobId + "/cancel"))
                .andExpect(status().isNotFound());
    }

}

