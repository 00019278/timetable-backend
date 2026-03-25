package com.sarmich.timetable.google.or.generate;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class NurseSchedulingProblem {


    public static void main(String[] args) {
        System.out.println("start");
        Loader.loadNativeLibraries();
        // Create the model.
        CpModel model = new CpModel();
        // Define the data
        int numTeachers = 20;
        int numRooms = 20;
        int numCourses = 20;
        int numHours = 20;

        // Create the timetable variables
        BoolVar[][][]timetable = new BoolVar[numCourses][numRooms][numTeachers];
//        for (int h = 0; h < numHours; h++) {
            for (int c = 0; c < numCourses; c++) {
                for (int r = 0; r < numRooms; r++) {
                    for (int t = 0; t < numTeachers; t++) {
                        timetable[c][r][t] = model.newBoolVar("_c" + c + "_r" + r + "_t" + t);
                    }
                }
            }
//        }

        // Add constraint 1
        // Constraint 1: At most one course is assigned to a teacher in a specific hour
        for (int t = 0; t < numTeachers; t++) {
//            for (int h = 0; h < numHours; h++) {
                ArrayList<Literal> boolVars = new ArrayList<>();
                for (int r = 0; r < numRooms; r++) {
                    for (int c = 0; c < numCourses; c++) {
                        boolVars.add(timetable[c][r][t]);
                    }
                }
                model.addAtMostOne(boolVars);
//            }
        }


        // Add constraint 2
        // Constraint 2: At most one course is scheduled in a room at a specific hour
        for (int r = 0; r < numRooms; r++) {
//            for (int h = 0; h < numHours; h++) {
                ArrayList<Literal> boolVars = new ArrayList<>();
                for (int t = 0; t < numTeachers; t++) {
                    for (int c = 0; c < numCourses; c++) {
                        boolVars.add(timetable[c][r][t]);
                    }
                }
                model.addAtMostOne(boolVars);
//            }
        }


        // Constraint 3: At most one teacher teaches a course at a specific hour
        for (int c = 0; c < numCourses; c++) {
//            for (int h = 0; h < numHours; h++) {
                ArrayList<Literal> boolVars = new ArrayList<>();
                for (int t = 0; t < numTeachers; t++) {
                    for (int r = 0; r < numRooms; r++) {
                        boolVars.add(timetable[c][r][t]);
                    }
                }
                model.addAtMostOne(boolVars);
//            }
        }


        // Constraint: All courses must be scheduled at least once
        for (int c = 0; c < numCourses; c++) {
            ArrayList<Literal> boolVars = new ArrayList<>();
            for (int t = 0; t < numTeachers; t++) {
//                for (int h = 0; h < numHours; h++) {
                    for (int r = 0; r < numRooms; r++) {
                        boolVars.add(timetable[c][r][t]);
                    }
//                }
            }
            model.addAtLeastOne(boolVars);
        }

        // Constraint: Use the maximum hours
//        for (int h = 0; h < numHours; h++) {
//            ArrayList<Literal> boolVars = new ArrayList<>();
//            for (int c = 0; c < numCourses; c++) {
//                for (int r = 0; r < numRooms; r++) {
//                    for (int t = 0; t < numTeachers; t++) {
//                        boolVars.add(timetable[h][c][r][t]);
//                    }
//                }
//            }
//            model.addAtLeastOne(boolVars);
//        }


        // Create the solver and solve
        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);

        // Print solution
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            System.out.println("Solution found:");
//            for (int h = 0; h < numHours; h++) {
                for (int c = 0; c < numCourses; c++) {
                    for (int r = 0; r < numRooms; r++) {
                        for (int t = 0; t < numTeachers; t++) {
                            if (solver.booleanValue(timetable[c][r][t])) {
                                System.out.println(" Course: " + c + ", Room: " + r + ", Teacher: " + t);
                            }
                        }
                    }
//                }
            }
        } else {
            System.out.println("No solution found.");
        }

        // Print statistics
        System.out.println("\nStatistics");
        System.out.println("  - conflicts      : " + solver.numConflicts());
        System.out.println("  - branches       : " + solver.numBranches());
        System.out.println("  - wall time      : " + solver.wallTime() + " s");
    }
}
