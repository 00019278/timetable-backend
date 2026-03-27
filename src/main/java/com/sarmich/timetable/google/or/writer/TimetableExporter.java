package com.sarmich.timetable.google.or.writer;

import com.sarmich.timetable.google.or.models.Class;
import com.sarmich.timetable.google.or.models.Response;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

public class TimetableExporter {

  public static void exportToExcel(List<Response> responses, String filePath, int maxHoursPerDay)
      throws Exception {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Timetable");

    // Header row
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Class");
    header.createCell(1).setCellValue("Hour");

    int col = 2;
    for (DayOfWeek day : DayOfWeek.values()) {
      header.createCell(col++).setCellValue(day.toString());
    }

    // Guruhlash class bo‘yicha
    Map<com.sarmich.timetable.google.or.models.Class, List<Response>> groupedByClass =
        responses.stream().collect(Collectors.groupingBy(Response::getClassObj));

    int rowNum = 1;
    for (Map.Entry<com.sarmich.timetable.google.or.models.Class, List<Response>> entry : groupedByClass.entrySet()) {
      Class classObj = entry.getKey();
      List<Response> lessons = entry.getValue();

      for (int h = 1; h <= maxHoursPerDay; h++) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(classObj.getName());
        row.createCell(1).setCellValue(h);

        col = 2;
        for (DayOfWeek day : DayOfWeek.values()) {
            int finalH = h;
            String value =
              lessons.stream()
                  .filter(r -> r.getDay() == day && Objects.equals(r.getHour(), finalH))
                  .map(r -> r.getSubject().getName() + " (" + r.getTeacher().getName() + ")")
                  .findFirst()
                  .orElse("");
          row.createCell(col++).setCellValue(value);
        }
      }
    }

    try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
      workbook.write(fileOut);
    }
    workbook.close();
  }
}
