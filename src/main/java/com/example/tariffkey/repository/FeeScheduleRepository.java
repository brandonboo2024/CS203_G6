package com.example.tariffkey.repository;

import com.example.tariffkey.model.FeeSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeScheduleRepository extends JpaRepository<FeeSchedule, String> {
}
