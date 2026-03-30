package com.sarmich.timetable.utils;

import com.sarmich.timetable.exception.NotFoundException;
import com.sarmich.timetable.exception.handler.ErrorCode;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
public class Util {
  public static Map<Long, Long> toLongMap(List<Object[]> list, int keyIndex, int valueIndex) {
    return list.stream()
        .collect(Collectors.toMap(item -> (Long) item[keyIndex], item -> (Long) item[valueIndex]));
  }

  public static Timestamp getTimeStampFromInstant(final Instant instant) {
    LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    return Timestamp.valueOf(ldt);
  }

  public static <T, R> R getIfNotNull(T obj, Function<T, R> getter) {
    if (obj == null) return null;
    return getter.apply(obj);
  }

  public static <T> void checkNull(T t, final Class<T> tClass) {
    if (t == null) {
      log.debug("[{}] not found", tClass.getSimpleName());
      throw new NotFoundException(tClass.getSimpleName() + " not found");
    }
  }

  public static <T> void checkNull(ErrorCode errorCode, T t, final Class<T> tClass) {
    if (t == null) {
      log.error("{} not found", tClass.getSimpleName());
      throw new NotFoundException(errorCode, tClass.getSimpleName() + " not found");
    }
  }

  public static <T> HashMap<Long, T> mapById(List<T> list, Function<T, Long> idExtractor) {
    HashMap<Long, T> map = new HashMap<>();
    list.forEach(item -> map.put(idExtractor.apply(item), item));
    return map;
  }

  public static <T> HashMap<String, T> mapByIdString(
      List<T> list, Function<T, String> idExtractor) {
    HashMap<String, T> map = new HashMap<>();
    list.forEach(item -> map.put(idExtractor.apply(item), item));
    return map;
  }

  public static <T> List<Long> idList(List<T> list, Function<T, Long> idExtractor) {
    return list.stream()
        .map(idExtractor)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet())
        .stream()
        .toList();
  }

  public static <T> List<Long> idList(Set<T> list, Function<T, Long> idExtractor) {
    return list.stream()
        .map(idExtractor)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet())
        .stream()
        .toList();
  }

  public static <T> List<Integer> intList(List<T> list, Function<T, Integer> idExtractor) {
    return list.stream()
        .map(idExtractor)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet())
        .stream()
        .toList();
  }

  public static <T> Set<UUID> idSet(List<T> list, Function<T, UUID> idExtractor) {
    return list.stream().map(idExtractor).collect(Collectors.toSet());
  }

  public static <T> List<String> idListStr(List<T> list, Function<T, String> idExtractor) {
    return list.stream().map(idExtractor).collect(Collectors.toSet()).stream().toList();
  }

  public static <E extends Enum<E>> List<String> nameEnums(final Collection<E> enums) {
    if (enums == null) return new ArrayList<>();
    return enums.stream().map(Enum::name).toList();
  }

  public static <E extends Enum<E>> List<Integer> ordinalEnum(final Collection<E> enums) {
    return enums.stream().map(Enum::ordinal).toList();
  }

  public static <E extends Enum<E>> List<Integer> ordinalEnums(final Collection<E> enums) {
    return enums.stream().map(Enum::ordinal).toList();
  }

  public static <T extends String> T getNotNull(T t1, T t2) {
    if (t1 == null || t1.isEmpty()) return t2;
    return t1;
  }

  public static String getEmptyStr(String str) {
    if (str == null) return "";
    return str;
  }

  public static <T> T getNotNull(T t1, T t2) {
    if (t1 == null) return t2;
    return t1;
  }

  public static <T> T getNotNull(T t1, T t2, T t3) {
    if (t1 == null) return getNotNull(t2, t3);
    return t1;
  }

  public static <E> boolean isEmpty(final Collection<E> elements) {
    return elements == null || elements.isEmpty();
  }

  public static <E> boolean notEmpty(final Collection<E> elements) {
    return !isEmpty(elements);
  }

  public static <E> boolean isNotEmpty(final Collection<E> elements) {
    return elements != null && !elements.isEmpty();
  }

  public static boolean isEmpty(final String str) {
    return str == null || str.isBlank();
  }

  public static String buildOrderAndPagination(Pageable pageable) {
    StringBuilder sb = new StringBuilder(" order by ");

    if (pageable != null && pageable.getSort().isSorted()) {
      List<String> orders = new ArrayList<>();
      pageable
          .getSort()
          .forEach(
              order -> {
                orders.add(order.getProperty() + " " + order.getDirection().name());
              });
      sb.append(String.join(", ", orders));
    } else {
      sb.append("id desc");
    }

    if (pageable != null) {
      sb.append(" limit ")
          .append(pageable.getPageSize())
          .append(" offset ")
          .append(pageable.getOffset());
    }

    return sb.toString();
  }

}
