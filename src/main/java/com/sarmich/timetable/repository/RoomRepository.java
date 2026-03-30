package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.RoomEntity;
import org.springframework.data.repository.CrudRepository;

public interface RoomRepository extends CrudRepository<RoomEntity, Integer> {
}
