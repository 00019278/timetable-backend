package com.sarmich.timetable.subject;

import com.sarmich.timetable.mapper.InstantMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InstantMapper.class)
public abstract class SubjectMapper {

    public static final SubjectMapper INSTANCE = Mappers.getMapper(SubjectMapper.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "profileId", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "createdDate", ignore = true),
            @Mapping(target = "priority", source = "priority")
    })
    public abstract SubjectEntity toEntity(SubjectRequest subject);

    @Mappings({
            @Mapping(target = "profileId", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "createdDate", ignore = true)
    })
    public abstract SubjectEntity toEntity(SubjectUpdateDto subject);

    @Mappings({
            @Mapping(target = "profile", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "createdDate", ignore = true)
    })
    public abstract SubjectResponse toResponse(SubjectEntity subject);
}
