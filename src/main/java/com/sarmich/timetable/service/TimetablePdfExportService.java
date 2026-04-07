package com.sarmich.timetable.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.sarmich.timetable.domain.*;
import com.sarmich.timetable.model.TimetableGroupDetail;
import com.sarmich.timetable.repository.*;
import com.sarmich.timetable.utils.Util;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetablePdfExportService {

  private final TimetableRepository timetableRepository;
  private final TimetableDataRepository timetableDataRepository;
  private final ClassRepository classRepository;

  // Boshqa repositorylar kerak emas, chunki ism-familiyalar 'slotDetails' ichida saqlangan
  // (Response obyekti sifatida).
  // Lekin agar siz Entitydan ID olib, qayta nomini qidirmoqchi bo'lsangiz, repositorylar qoladi.
  // Eng optimali: slotDetails ichidagi tayyor 'TeacherResponse', 'SubjectResponse' obyektlaridan
  // ismini olish.
  // Shunda DB ga ortiqcha so'rov bo'lmaydi.

  public byte[] exportToPdf(UUID timetableId) throws IOException {
    TimetableEntity timetable = timetableRepository.findByIdAndDeletedFalse(timetableId);
    Util.checkNull(timetable, TimetableEntity.class);

    Integer maxVersion = timetableDataRepository.findMaxVersion(timetableId);
    List<TimetableDataEntity> data =
        timetableDataRepository.findAllByTimetableIdAndVersion(timetableId, maxVersion);

    // Sinflar ro'yxatini yuklab olamiz (Ustunlar uchun)
    Set<Integer> classIds =
        data.stream().map(TimetableDataEntity::getClassId).collect(Collectors.toSet());

    Map<Integer, String> classNames =
        classRepository.findAllById(classIds).stream()
            .collect(Collectors.toMap(ClassEntity::getId, ClassEntity::getName));

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Document document = new Document(PageSize.A4.rotate());
      PdfWriter.getInstance(document, out);
      document.open();

      // Title
      Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
      Paragraph title = new Paragraph("Timetable: " + timetable.getName(), titleFont);
      title.setAlignment(Element.ALIGN_CENTER);
      title.setSpacingAfter(20);
      document.add(title);

      // Table Setup
      List<Integer> sortedClassIds =
          classNames.keySet().stream().sorted(Comparator.comparing(classNames::get)).toList();

      int numColumns = 2 + sortedClassIds.size(); // Day + Period + Classes
      PdfPTable table = new PdfPTable(numColumns);
      table.setWidthPercentage(100);

      // Widths
      if (numColumns > 0) {
        float[] widths = new float[numColumns];
        widths[0] = 0.6f; // Day
        widths[1] = 0.4f; // Period
        for (int i = 2; i < numColumns; i++) {
          widths[i] = 2f; // Class columns
        }
        table.setWidths(widths);
      }

      // Headers
      addHeaderCell(table, "Day");
      addHeaderCell(table, "Period");
      for (Integer classId : sortedClassIds) {
        addHeaderCell(table, classNames.get(classId));
      }

      // Rows
      DayOfWeek[] days = DayOfWeek.values();
      int maxDataPeriod = data.stream().mapToInt(TimetableDataEntity::getHour).max().orElse(0);
      int maxPeriod = Math.max(7, maxDataPeriod);

      for (DayOfWeek day : days) {
        if (day == DayOfWeek.SUNDAY) continue;

        // Day Cell
        PdfPCell dayCell =
            new PdfPCell(
                new Phrase(
                    toTitleCase(day.name()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        dayCell.setRowspan(maxPeriod);
        dayCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        dayCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        dayCell.setRotation(90);
        table.addCell(dayCell);

        for (int period = 1; period <= maxPeriod; period++) {
          // Period Cell
          PdfPCell periodCell =
              new PdfPCell(
                  new Phrase(
                      String.valueOf(period), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
          periodCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
          periodCell.setHorizontalAlignment(Element.ALIGN_CENTER);
          table.addCell(periodCell);

          // Class Cells
          for (Integer classId : sortedClassIds) {
            // YANGI METOD: Slot ichidagi detallarni o'qib beradi
            String content = getCellContent(data, day, period, classId);

            PdfPCell cell =
                new PdfPCell(new Phrase(content, FontFactory.getFont(FontFactory.HELVETICA, 8)));
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
          }
        }
      }

      document.add(table);
      document.close();
      return out.toByteArray();
    }
  }

  private void addHeaderCell(PdfPTable table, String text) {
    PdfPCell cell =
        new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
    cell.setBackgroundColor(Color.LIGHT_GRAY);
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cell.setPadding(5);
    table.addCell(cell);
  }

  /**
   * YANGILANGAN METOD: SlotDetails (JSON) ichidagi ma'lumotlarni o'qiydi. Agar guruhli dars bo'lsa
   * (Split), ularni chiroyli qilib (/) bilan ajratib ko'rsatadi.
   */
  private String getCellContent(
      List<TimetableDataEntity> data, DayOfWeek day, int period, Integer classId) {

    // 1. Shu vaqtga mos keladigan entityni topamiz
    return data.stream()
        .filter(
            d ->
                d.getClassId().equals(classId)
                    && d.getDayOfWeek() == day
                    && d.getHour() == period
                    && Boolean.TRUE.equals(d.getIsScheduled()))
        .findFirst()
        .map(
            entity -> {
              // 2. SlotDetails ni tekshiramiz
              if (entity.getSlotDetails() == null || entity.getSlotDetails().isEmpty()) {
                return "";
              }

              // 3. Har bir detalni formatlaymiz
              List<String> parts = new ArrayList<>();
              for (TimetableGroupDetail detail : entity.getSlotDetails()) {
                StringBuilder sb = new StringBuilder();

                // Fan nomi
                //                        if (detail.getSubject() != null) {
                //                            sb.append(detail.getSubject().name());
                //                        }

                // Guruh nomi (Agar bo'lsa)
                //                        if (detail.getGroup() != null) {
                //                            sb.append("
                // (").append(detail.getGroup().name()).append(")");
                //                        }
                //
                //                        // O'qituvchi
                //                        if (detail.getTeacher() != null) {
                //
                // sb.append("\n").append(detail.getTeacher().fullName()); // Yoki shortName
                //                        }
                //
                //                        // Xona
                //                        if (detail.getRoom() != null) {
                //
                // sb.append("\n[").append(detail.getRoom().name()).append("]");
                //                        }

                parts.add(sb.toString());
              }

              // 4. Birlashtirish (Agar 2 ta guruh bo'lsa, chiziq bilan ajratamiz)
              return String.join("\n-----------------\n", parts);
            })
        .orElse("");
  }

  private String toTitleCase(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
  }
}
