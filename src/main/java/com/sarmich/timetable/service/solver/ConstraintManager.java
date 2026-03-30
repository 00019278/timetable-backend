package com.sarmich.timetable.service.solver;

import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.LinearExprBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConstraintManager {
  private final HardConstraintProvider hardConstraintProvider;
  private final SoftConstraintProvider softConstraintProvider;

  public void applyAllConstraints(
      CpModel model,
      ModelVariables variables,
      ModelData data,
      LinearExprBuilder objective,
      ApplySoftConstraint applySoftConstraint) {
    hardConstraintProvider.apply(model, variables, data);
    softConstraintProvider.apply(model, variables, data, objective, applySoftConstraint);
  }
}
