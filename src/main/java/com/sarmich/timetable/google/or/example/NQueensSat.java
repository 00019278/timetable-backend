package com.sarmich.timetable.google.or.example;
import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverSolutionCallback;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;

/** OR-Tools solution to the N-queens problem. */
public final class NQueensSat {
  static class SolutionPrinter extends CpSolverSolutionCallback {
    public SolutionPrinter(IntVar[] queensIn) {
      solutionCount = 0;
      queens = queensIn;
    }

    @Override
    public void onSolutionCallback() {
      System.out.println("Solution " + solutionCount);
      for (int i = 0; i < queens.length; ++i) {
        for (int j = 0; j < queens.length; ++j) {
          if (value(queens[j]) == i) {
            System.out.print("Q");
          } else {
            System.out.print("_");
          }
          if (j != queens.length - 1) {
            System.out.print(" ");
          }
        }
        System.out.println();
      }
      solutionCount++;
    }

    public int getSolutionCount() {
      return solutionCount;
    }

    private int solutionCount;
    private final IntVar[] queens;
  }

  public static void main(String[] args) {
    Loader.loadNativeLibraries();
    // Create the model.
    CpModel model = new CpModel();

    int boardSize = 15;
    // There are `BoardSize` number of variables, one for a queen in each column of the board. The
    // value of each variable is the row that the queen is in.
    IntVar[] queens = new IntVar[boardSize];
    for (int i = 0; i < boardSize; ++i) {
      queens[i] = model.newIntVar(0, boardSize - 1, "x" + i);
    }

    // Define constraints.
    // All rows must be different.
    model.addAllDifferent(queens);

    // No two queens can be on the same diagonal.
    LinearExpr[] diag1 = new LinearExpr[boardSize];
    LinearExpr[] diag2 = new LinearExpr[boardSize];
    for (int i = 0; i < boardSize; ++i) {
      diag1[i] = LinearExpr.newBuilder().add(queens[i]).add(i).build();
      diag2[i] = LinearExpr.newBuilder().add(queens[i]).add(-i).build();
    }
    model.addAllDifferent(diag1);
    model.addAllDifferent(diag2);

    // Create a solver and solve the model.
    CpSolver solver = new CpSolver();
    SolutionPrinter cb = new SolutionPrinter(queens);
    // Tell the solver to enumerate all solutions.
    solver.getParameters().setEnumerateAllSolutions(true);
    // And solve.
    solver.solve(model, cb);

    // Statistics.
    System.out.println("Statistics");
    System.out.println("  conflicts : " + solver.numConflicts());
    System.out.println("  branches  : " + solver.numBranches());
    System.out.println("  wall time : " + solver.wallTime() + " s");
    System.out.println("  solutions : " + cb.getSolutionCount());
  }

  private NQueensSat() {}
}