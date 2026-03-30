package com.sarmich.timetable.mapper;

import com.sarmich.timetable.domain.ClassEntity;
import com.sarmich.timetable.domain.CompanyEntity;
import com.sarmich.timetable.model.request.ClassRequest;
import com.sarmich.timetable.model.request.ClassUpdateRequest;
import com.sarmich.timetable.model.response.CompanyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public abstract class CompanyMapper {

  public static final CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);

  public abstract ClassEntity toEntity(ClassRequest classDto);

  public abstract ClassEntity toEntity(ClassUpdateRequest classDto);

  public abstract CompanyResponse toResponse(CompanyEntity classDto);
}
