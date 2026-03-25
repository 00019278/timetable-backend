package com.sarmich.timetable.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sarmich.timetable.domain.enums.ProfileRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtDTO {

    private String mail;
    private ProfileRole role;
    private Integer id;

    public JwtDTO(String mail, ProfileRole role) {
        this.mail = mail;
        this.role = role;
    }

    public JwtDTO(String mail, Integer id) {
        this.mail = mail;
        this.id = id;
    }
}
