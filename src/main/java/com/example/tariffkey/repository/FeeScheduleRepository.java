package com.example.tariffkey.repository;

import com.example.tariffkey.entity.FeeSchedule;
import com.example.tariffkey.entity.FeeType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeScheduleRepository extends JpaRepository<FeeSchedule, FeeType> {
}
