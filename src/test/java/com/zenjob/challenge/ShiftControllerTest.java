package com.zenjob.challenge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenjob.challenge.dto.JobDto;
import com.zenjob.challenge.entity.Shift;
import com.zenjob.challenge.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zenjob.challenge.controller.exceptions.ResourceNotFoundException;
import org.springframework.test.web.servlet.MvcResult;


@SpringBootTest
@AutoConfigureMockMvc
public class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    private UUID shiftId;

    @BeforeEach
    public void setup() {
        shiftId = UUID.randomUUID();
    }

    @Test
    public void testCancelShift() throws Exception {
        UUID shiftId = UUID.randomUUID();
        JobDto expectedJobDto = new JobDto();

        Mockito.when(jobService.cancelShift(shiftId)).thenReturn(expectedJobDto);

        MvcResult mvcResult = mockMvc.perform(patch("/shift/" + shiftId + "/cancel"))
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        JobDto actualJobDto = new ObjectMapper().readValue(actualResponseBody, JobDto.class);

        Mockito.verify(jobService, Mockito.times(1)).cancelShift(shiftId);
        assertEquals(expectedJobDto, actualJobDto);
    }

    @Test
    public void testCancelShiftWhenShiftDoesNotExist() throws Exception {
        UUID nonExistentShiftId = UUID.randomUUID();
        doThrow(ResourceNotFoundException.class).when(jobService).cancelShift(nonExistentShiftId);

        mockMvc.perform(patch("/shift/" + nonExistentShiftId + "/cancel"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCancelAndReplaceShiftsForTalent() throws Exception {
        UUID talentId = UUID.randomUUID();

        List<Shift> shifts = new ArrayList<>();
        Shift shift = new Shift();
        shift.setId(UUID.randomUUID());
        shifts.add(shift);

        Mockito.when(jobService.findAllShiftsByTalentId(talentId)).thenReturn(shifts);

        List<Shift> updatedShifts = new ArrayList<>();
        Shift updatedShift = new Shift();
        updatedShift.setId(UUID.randomUUID());
        updatedShifts.add(updatedShift);

        Mockito.when(jobService.cancelAndReplaceShiftsForTalent(talentId)).thenReturn(updatedShifts);

        MvcResult mvcResult = mockMvc.perform(patch("/shift/cancelAndReplaceForTalent/" + talentId))
                .andExpect(status().isOk())
                .andReturn();

        Mockito.verify(jobService, Mockito.times(1)).findAllShiftsByTalentId(talentId);
        Mockito.verify(jobService, Mockito.times(1)).cancelAndReplaceShiftsForTalent(talentId);

        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        List<Shift> actualShifts = new ObjectMapper().readValue(actualResponseBody, new TypeReference<List<Shift>>() {
        });
        for (int i = 0; i < updatedShifts.size(); i++) {
            assertEquals(updatedShifts.get(i).getId(), actualShifts.get(i).getId());
        }
    }

}
