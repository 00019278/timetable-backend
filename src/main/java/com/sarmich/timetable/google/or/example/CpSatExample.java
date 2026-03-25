package com.sarmich.timetable.google.or.example;
import static java.util.Arrays.stream;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;

/** Minimal CP-SAT example to showcase calling the solver. */
public final class CpSatExample {
  public static void main(String[] args) {
    Loader.loadNativeLibraries();
    // Create the model.
    CpModel model = new CpModel();

    // Create the variables.
    int varUpperBound = stream(new int[] {50, 45, 37}).max().getAsInt();

    IntVar x = model.newIntVar(0, varUpperBound, "x");
    IntVar y = model.newIntVar(0, varUpperBound, "y");
    IntVar z = model.newIntVar(0, varUpperBound, "z");

    // Create the constraints.
    model.addLessOrEqual(LinearExpr.weightedSum(new IntVar[] {x, y, z}, new long[] {2, 7, 3}), 50);
    model.addLessOrEqual(LinearExpr.weightedSum(new IntVar[] {x, y, z}, new long[] {3, -5, 7}), 45);
    model.addLessOrEqual(LinearExpr.weightedSum(new IntVar[] {x, y, z}, new long[] {5, 2, -6}), 37);

    model.maximize(LinearExpr.weightedSum(new IntVar[] {x, y, z}, new long[] {2, 2, 3}));

    // Create a solver and solve the model.
    CpSolver solver = new CpSolver();
    CpSolverStatus status = solver.solve(model);

    if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
      System.out.printf("Maximum of objective function: %f%n", solver.objectiveValue());
      System.out.println("x = " + solver.value(x));
      System.out.println("y = " + solver.value(y));
      System.out.println("z = " + solver.value(z));
    } else {
      System.out.println("No solution found.");
    }

    // Statistics.
    System.out.println("Statistics");
    System.out.printf("  conflicts: %d%n", solver.numConflicts());
    System.out.printf("  branches : %d%n", solver.numBranches());
    System.out.printf("  wall time: %f s%n", solver.wallTime());
  }

  private CpSatExample() {}
}