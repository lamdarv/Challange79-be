package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmployeeStatusRepository extends JpaRepository<EmployeeStatus, UUID> {
}
