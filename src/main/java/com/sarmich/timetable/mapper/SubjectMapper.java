package com.sarmich.timetable.mapper;

import com.sarmich.timetable.domain.SubjectEntity;
import com.sarmich.timetable.model.request.SubjectRequest;
import com.sarmich.timetable.model.response.SubjectResponse;
import com.sarmich.timetable.model.request.SubjectUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InstantMapper.class)
public abstract class SubjectMapper {

    public static final SubjectMapper INSTANCE = Mappers.getMapper(SubjectMapper.class);


    public abstract SubjectEntity toEntity(SubjectRequest subject);


    public abstract SubjectEntity toEntity(SubjectUpdateRequest subject);


    public abstract SubjectResponse toResponse(SubjectEntity subject);
}
