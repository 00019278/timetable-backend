package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.ProfileEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ProfileRepository extends CrudRepository<ProfileEntity, Integer> {

    @Query("from ProfileEntity where email = ?1 ")
    Optional<ProfileEntity> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("update ProfileEntity set password = ?2 where id = ?1 ")
    int updatePassword(Integer id, String pass);

    @Modifying
    @Transactional
    @Query("update ProfileEntity set email = ?2 where id = ?1 ")
    int updateEmail(Integer id, String email);

    @Modifying
    @Transactional
    @Query("update ProfileEntity set name = ?2, surname = ?3 where id = ?1 ")
    int updateNameAndSurname(Integer id, String name, String surname);

    @Query(value = "select * from profile where id = ?1 ", nativeQuery = true)
    ProfileEntity findByProfile(Integer id);
}
