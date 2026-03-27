package com.sarmich.timetable.google.or.generate;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Writer {
    public static void writer(List<Response> list) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Timetable by Class");

        Row headerRow = sheet.createRow(2);
        headerRow.createCell(1).setCellValue("Day/Hour");

        Row classHeaderRow = sheet.createRow(1);
        List<Class> classes = DemoData.classList();
        for (int i = 0; i < classes.size(); i++) {
            classHeaderRow.createCell(2 + i).setCellValue(classes.get(i).name);
        }
        for (DayOfWeek day : DayOfWeek.values()) {
            for (int i = 0; i < 6; i++) {  // Assuming 6 hours per day
                Row row = sheet.createRow(2 + (day.getValue() - 1) * 7 + i);
                row.createCell(1).setCellValue(day.name() + "/" + (i + 1));
                for (int c = 0; c < classes.size(); c++) {
                    int finalI = i;
                    int finalC = c;
                    Response response = list.stream().filter(l -> l.day == day && l.hour == finalI && Objects.equals(l.aClass.id, DemoData.getClassMap().get(finalC) != null ? DemoData.getClassMap().get(finalC).id : null)).findFirst().orElse(null);
                    if (response != null) {
                        row.createCell(2 + c).setCellValue(response.subject.getName() + "/" + response.teacher.getName());
                    }
                }
            }
        }
        try (FileOutputStream fileOut = new FileOutputStream("Timetable.xlsx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
