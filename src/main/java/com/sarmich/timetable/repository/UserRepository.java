package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {

  @Query("from UserEntity where email = ?1 ")
  Optional<UserEntity> findByEmail(String email);

  UserEntity findByIdAndDeletedFalse(Integer id);

  @Modifying
  @Transactional
  @Query("update UserEntity set password = ?2 where id = ?1 ")
  int updatePassword(Integer id, String pass);

  @Modifying
  @Transactional
  @Query("update UserEntity set email = ?2 where id = ?1 ")
  int updateEmail(Integer id, String email);

  @Modifying
  @Transactional
  @Query("update UserEntity set name = ?2, surname = ?3 where id = ?1 ")
  int updateNameAndSurname(Integer id, String name, String surname);
}
