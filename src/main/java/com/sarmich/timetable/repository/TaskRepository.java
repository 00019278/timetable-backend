package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.TaskEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {}
