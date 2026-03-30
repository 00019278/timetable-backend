package com.sarmich.timetable.mapper;

import com.sarmich.timetable.domain.TimetableDataEntity;
import com.sarmich.timetable.domain.TimetableEntity;
import com.sarmich.timetable.model.TimetableDataResponse;
import com.sarmich.timetable.model.response.TimetableResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TimetableMapper {

  TimetableMapper INSTANCE = Mappers.getMapper(TimetableMapper.class);

  TimetableResponse toResponse(TimetableEntity request);

  TimetableDataResponse toDataResponse(TimetableDataEntity request);
}
