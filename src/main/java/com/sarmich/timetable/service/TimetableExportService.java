package com.sarmich.timetable.service;

import com.sarmich.timetable.domain.ClassEntity;
import com.sarmich.timetable.domain.TimetableDataEntity;
import com.sarmich.timetable.domain.TimetableEntity;
import com.sarmich.timetable.model.TimetableGroupDetail;
import com.sarmich.timetable.repository.*;
import com.sarmich.timetable.utils.Util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableExportService {

    private final TimetableRepository timetableRepository;
    private final TimetableDataRepository timetableDataRepository;
    private final ClassRepository classRepository;
    // Boshqa repositorylar endi kerak emas, chunki ma'lumotlar slotDetails ichida to'liq bor.

    public byte[] exportToExcel(UUID timetableId) throws IOException {
        TimetableEntity timetable = timetableRepository.findByIdAndDeletedFalse(timetableId);
        Util.checkNull(timetable, TimetableEntity.class);

        Integer maxVersion = timetableDataRepository.findMaxVersion(timetableId);
        List<TimetableDataEntity> data =
                timetableDataRepository.findAllByTimetableIdAndVersion(timetableId, maxVersion);

        // Sinflar ro'yxatini yuklab olamiz (Ustunlar uchun)
        Set<Integer> classIds = data.stream()
                .map(TimetableDataEntity::getClassId)
                .collect(Collectors.toSet());

        Map<Integer, String> classNames = classRepository.findAllById(classIds).stream()
                .collect(Collectors.toMap(ClassEntity::getId, ClassEntity::getName));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Timetable");

            // Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dayStyle = createDayStyle(workbook);
            CellStyle periodStyle = createPeriodStyle(workbook);
            CellStyle cellStyle = createCellStyle(workbook);

            // Headers
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "Day", headerStyle);
            createCell(headerRow, 1, "Period", headerStyle);

            List<Integer> sortedClassIds =
                    classNames.keySet().stream().sorted(Comparator.comparing(classNames::get)).toList();
            for (int i = 0; i < sortedClassIds.size(); i++) {
                createCell(headerRow, i + 2, classNames.get(sortedClassIds.get(i)), headerStyle);
            }

            // Data
            int currentRow = 1;
            DayOfWeek[] days = DayOfWeek.values();

            // Determine max period dynamically
            int maxDataPeriod = data.stream().mapToInt(TimetableDataEntity::getHour).max().orElse(0);
            int maxPeriod = Math.max(7, maxDataPeriod);

            for (DayOfWeek day : days) {
                if (day == DayOfWeek.SUNDAY) continue;

                int startRow = currentRow;
                for (int period = 1; period <= maxPeriod; period++) {
                    Row row = sheet.createRow(currentRow);

                    // Period column
                    createCell(row, 1, String.valueOf(period), periodStyle);

                    // Class columns
                    for (int i = 0; i < sortedClassIds.size(); i++) {
                        Integer classId = sortedClassIds.get(i);

                        // YANGI: Slot ichidagi detallarni o'qish
                        String cellContent = getCellContent(data, day, period, classId);
                        createCell(row, i + 2, cellContent, cellStyle);
                    }
                    currentRow++;
                }

                // Merge Day column
                createCell(sheet.getRow(startRow), 0, toTitleCase(day.name()), dayStyle);
                sheet.addMergedRegion(new CellRangeAddress(startRow, currentRow - 1, 0, 0));
            }

            // Auto-size columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            for (int i = 0; i < sortedClassIds.size(); i++) {
                // Excelda wrap text bo'lsa, autoSize yaxshi ishlamaydi, shuning uchun fixed width yaxshiroq
                sheet.setColumnWidth(i + 2, 6000); // Taxminan 20-25 harf sig'adigan kenglik
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * YANGILANGAN METOD:
     * SlotDetails (JSON) ichidagi ma'lumotlarni o'qiydi.
     * Guruhli darslarni chiroyli formatda qaytaradi.
     */
    private String getCellContent(
            List<TimetableDataEntity> data,
            DayOfWeek day,
            int period,
            Integer classId) {

        return data.stream()
                .filter(d -> d.getClassId().equals(classId)
                        && d.getDayOfWeek() == day
                        && d.getHour() == period
                        && Boolean.TRUE.equals(d.getIsScheduled()))
                .findFirst()
                .map(entity -> {
                    if (entity.getSlotDetails() == null || entity.getSlotDetails().isEmpty()) {
                        return "";
                    }

                    List<String> parts = new ArrayList<>();
                    for (TimetableGroupDetail detail : entity.getSlotDetails()) {
                        StringBuilder sb = new StringBuilder();

//                        // Fan nomi
//                        if (detail.getSubject() != null) {
//                            sb.append(detail.getSubject().name());
//                        }
//
//                        // Guruh nomi
//                        if (detail.getGroup() != null) {
//                            sb.append(" (").append(detail.getGroup().name()).append(")");
//                        }
//
//                        // O'qituvchi
//                        if (detail.getTeacher() != null) {
//                            sb.append("\n").append(detail.getTeacher().fullName());
//                        }
//
//                        // Xona
//                        if (detail.getRoom() != null) {
//                            sb.append("\n[").append(detail.getRoom().name()).append("]");
//                        }

                        parts.add(sb.toString());
                    }

                    // Excelda 'alt+enter' efekti uchun "\n" ishlatiladi, lekin aniq ajratish uchun chiziq qo'shamiz
                    return String.join("\n-----------------\n", parts);
                })
                .orElse("");
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(style);
        return style;
    }

    private CellStyle createDayStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        // Excelda matnni vertikal aylantirish (90 daraja)
        style.setRotation((short) 90);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(style);
        return style;
    }

    private CellStyle createPeriodStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(style);
        return style;
    }

    private CellStyle createCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true); // Matn sig'masa pastga tushadi
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
        setBorder(style);
        return style;
    }

    private void setBorder(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}