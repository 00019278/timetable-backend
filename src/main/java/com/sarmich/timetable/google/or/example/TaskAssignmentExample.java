//package com.sarmich.timetable.google.or.example;// Import the necessary libraries
//import com.google.ortools.sat.BoolVar;
//
//import java.util.ArrayList;
//
//public class TaskAssignmentExample {
//    public static void main(String[] args) {
//        // Define the number of tasks and workers
//        int numTasks = 5;
//        int numWorkers = 3;
//
//        // Create a new model
//        Model model = new Model("Task Assignment");
//
//        // Create an array to store the Boolean variables for task assignment
//        BoolVar[][] taskAssignment = model.boolVarMatrix(numTasks, numWorkers);
//
//        // Constraint: Each task must be assigned to at least one worker
//        for (int t = 0; t < numTasks; t++) {
//            ArrayList<BoolVar> boolVars = new ArrayList<>();
//            for (int w = 0; w < numWorkers; w++) {
//                boolVars.add(taskAssignment[t][w]);
//            }
//            model.addAtLeastOne(boolVars.toArray(new BoolVar[0]));
//        }
//
//        // Other constraints and objective function can be added here...
//
//        // Solve the model
//        model.getSolver().solve();
//
//        // Print the solution
//        for (int t = 0; t < numTasks; t++) {
//            System.out.print("Task " + t + " is assigned to: ");
//            for (int w = 0; w < numWorkers; w++) {
//                if (taskAssignment[t][w].getValue() == 1) {
//                    System.out.print("Worker " + w + " ");
//                }
//            }
//            System.out.println();
//        }
//    }
//}
