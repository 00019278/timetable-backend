// Cryptarithmetic puzzle
//
// First attempt to solve equation CP + IS + FUN = TRUE
// where each letter represents a unique digit.
//
// This problem has 72 different solutions in base 10.
package com.sarmich.timetable.google.or.example;
import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.DecisionBuilder;
import com.google.ortools.constraintsolver.IntVar;
import com.google.ortools.constraintsolver.Solver;

/** Cryptarithmetic puzzle. */
public final class CpIsFunCp {
  public static void main(String[] args) throws Exception {
    Loader.loadNativeLibraries();
    // Instantiate the solver.
    Solver solver = new Solver("CP is fun!");

    final int base = 10;

    // Decision variables.
    final IntVar c = solver.makeIntVar(1, base - 1, "C");
    final IntVar p = solver.makeIntVar(0, base - 1, "P");
    final IntVar i = solver.makeIntVar(1, base - 1, "I");
    final IntVar s = solver.makeIntVar(0, base - 1, "S");
    final IntVar f = solver.makeIntVar(1, base - 1, "F");
    final IntVar u = solver.makeIntVar(0, base - 1, "U");
    final IntVar n = solver.makeIntVar(0, base - 1, "N");
    final IntVar t = solver.makeIntVar(1, base - 1, "T");
    final IntVar r = solver.makeIntVar(0, base - 1, "R");
    final IntVar e = solver.makeIntVar(0, base - 1, "E");

    // Group variables in a vector so that we can use AllDifferent.
    final IntVar[] letters = new IntVar[] {c, p, i, s, f, u, n, t, r, e};

    // Verify that we have enough digits.

      // Define constraints.
    solver.addConstraint(solver.makeAllDifferent(letters));

    // CP + IS + FUN = TRUE
    final IntVar sum1 =
        solver
            .makeSum(new IntVar[] {p, s, n,
                solver.makeProd(solver.makeSum(new IntVar[] {c, i, u}).var(), base).var(),
                solver.makeProd(f, base * base).var()})
            .var();
    final IntVar sum2 = solver
                            .makeSum(new IntVar[] {e, solver.makeProd(u, base).var(),
                                solver.makeProd(r, base * base).var(),
                                solver.makeProd(t, base * base * base).var()})
                            .var();
    solver.addConstraint(solver.makeEquality(sum1, sum2));

    int countSolution = 0;
    // Create the decision builder to search for solutions.
    final DecisionBuilder db =
        solver.makePhase(letters, Solver.CHOOSE_FIRST_UNBOUND, Solver.ASSIGN_MIN_VALUE);
    solver.newSearch(db);
    while (solver.nextSolution()) {
      System.out.println("C=" + c.value() + " P=" + p.value());
      System.out.println(" I=" + i.value() + " S=" + s.value());
      System.out.println(" F=" + f.value() + " U=" + u.value());
      System.out.println(" N=" + n.value() + " T=" + t.value());
      System.out.println(" R=" + r.value() + " E=" + e.value());

      // Is CP + IS + FUN = TRUE?
      if (p.value() + s.value() + n.value() + base * (c.value() + i.value() + u.value())
              + base * base * f.value()
          != e.value() + base * u.value() + base * base * r.value()
              + base * base * base * t.value()) {
        throw new Exception("CP + IS + FUN != TRUE");
      }
      countSolution++;
    }
    solver.endSearch();
    System.out.println("Number of solutions found: " + countSolution);
  }

  private CpIsFunCp() {}
}