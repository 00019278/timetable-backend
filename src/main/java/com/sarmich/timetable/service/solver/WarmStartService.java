package com.sarmich.timetable.service.solver;

import com.sarmich.timetable.model.Response;
import com.sarmich.timetable.model.SolverResult;
import com.sarmich.timetable.model.response.CompanyResponse;
import com.sarmich.timetable.model.response.OrTLesson;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class WarmStartService {

  private final TimetableGenerator generator;

  //  public void apply(CpModel model, ModelVariables vars, SolvedTimetable previousSolution) {
  //    log.info("Warm start disabled during Interval-based refactor.");
  //  }

  public SolverResult optimizeSchedule(
      List<Response> scheduled,
      List<OrTLesson> lessons,
      CompanyResponse company,
      ApplySoftConstraint request) {

    log.info(
        "Optimization requested. Falling back to fresh generation for Interval-based refactor.");
    // Pass empty list for rooms if not available in this context, or pass null if
    // handled.
    // Helper overload in TimetableGenerator uses empty list.
    return generator.generate(lessons, Collections.emptyList(), company, request);
  }
}
