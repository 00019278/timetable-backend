package com.sarmich.timetable.google.or.example;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Generator {
    public static void main(String[] args) {
        List<ClassGenerator> a= new ArrayList<>();
        generate(a);
    }

    private static void generate(final List<ClassGenerator> a) {
        Loader.loadNativeLibraries();
        final int numberTeachers = 4;
        final int numberSubjects = 3;
        final int numberClass = 3;

        final int[] allNurses = IntStream.range(0, numberTeachers).toArray();
        final int[] allDays = IntStream.range(0, numberSubjects).toArray();
        final int[] allShifts = IntStream.range(0, numberClass).toArray();
        // Creates the model.
        CpModel model = new CpModel();

        // Creates shift variables.
        // shifts[(n, d, s)]: nurse 'n' works shift 's' on day 'd'.
        Literal[][][] shifts = new Literal[numberTeachers][numberSubjects][numberClass];
        for (int n : allNurses) {
            for (int d : allDays) {
                for (int s : allShifts) {
                    shifts[n][d][s] = model.newBoolVar("shifts_n" + n + "d" + d + "s" + s);
                }
            }
        }

        // Each shift is assigned to exactly one nurse in the schedule period.
        for (int d : allDays) {
            for (int s : allShifts) {
                List<Literal> nurses = new ArrayList<>();
                for (int n : allNurses) {
                    nurses.add(shifts[n][d][s]);
                }
                model.addExactlyOne(nurses);
            }
        }

        // Each nurse works at most one shift per day.
        for (int n : allNurses) {
            for (int d : allDays) {
                List<Literal> work = new ArrayList<>();
                for (int s : allShifts) {
                    work.add(shifts[n][d][s]);
                }
                model.addAtMostOne(work);
            }
        }

        // Try to distribute the shifts evenly, so that each nurse works
        // minShiftsPerNurse shifts. If this is not possible, because the total
        // number of shifts is not divisible by the number of nurses, some nurses will
        // be assigned one more shift.
        int minShiftsPerNurse = (numberClass * numberSubjects) / numberTeachers;
        int maxShiftsPerNurse;
        maxShiftsPerNurse = minShiftsPerNurse + 1;
        for (int n : allNurses) {
            LinearExprBuilder shiftsWorked = LinearExpr.newBuilder();
            for (int d : allDays) {
                for (int s : allShifts) {
                    shiftsWorked.add(shifts[n][d][s]);
                }
            }
            model.addLinearConstraint(shiftsWorked, minShiftsPerNurse, maxShiftsPerNurse);
        }

        CpSolver solver = new CpSolver();
        solver.getParameters().setLinearizationLevel(0);
        // Tell the solver to enumerate all solutions.
        solver.getParameters().setEnumerateAllSolutions(true);

        // Display the first five solutions.
        final int solutionLimit = 5;
    }
}
