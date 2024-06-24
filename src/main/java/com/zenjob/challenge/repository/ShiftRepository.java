package com.zenjob.challenge.repository;

import com.zenjob.challenge.entity.Shift;
import com.zenjob.challenge.entity.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    List<Shift> findAllByJobId(UUID jobId);

    List<Shift> findAllByTalentId(UUID talentId);
    List<Shift> findAllByJobIdAndStatusNot(UUID jobId, ShiftStatus status);

    List<Shift> findAllByJobIdAndTalentIdAndStatusNot(UUID jobId, UUID talentId, ShiftStatus status);

    Optional<Shift> findById(UUID shiftId);

}
