package com.sarmich.timetable.google.or.writer;

import com.sarmich.timetable.google.or.models.Response;
import com.sarmich.timetable.google.or.models.Teacher;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeacherTimetableExporter {

  public static void exportTeacherTimetable(List<Response> responses, String filePath)
      throws Exception {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("TeacherTimetable");

    // Header row
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("Teacher");
    header.createCell(1).setCellValue("Hour");

    int col = 2;
    for (DayOfWeek day : DayOfWeek.values()) {
      header.createCell(col++).setCellValue(day.toString());
    }

    // Guruhlash teacher bo‘yicha
    Map<Teacher, List<Response>> groupedByTeacher =
        responses.stream().collect(Collectors.groupingBy(Response::getTeacher));

    int rowNum = 1;
    for (Map.Entry<Teacher, List<Response>> entry : groupedByTeacher.entrySet()) {
      Teacher teacher = entry.getKey();
      List<Response> lessons = entry.getValue();

      for (int h = 1; h <= 7; h++) { // har kuni 7 soat
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(teacher.getName());
        row.createCell(1).setCellValue(h);

        col = 2;
        for (DayOfWeek day : DayOfWeek.values()) {
          int finalH = h;
          String value =
              lessons.stream()
                  .filter(r -> r.getDay() == day && r.getHour() == finalH)
                  .map(r -> r.getClassObj().getName() + " (" + r.getSubject().getName() + ")")
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
