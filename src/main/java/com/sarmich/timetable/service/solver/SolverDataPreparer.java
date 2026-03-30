package com.sarmich.timetable.service.solver;

import com.sarmich.timetable.model.response.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class SolverDataPreparer {

    public List<OrTLesson> prepareLessons(
        List<LessonResponse> rawLessons,
        java.util.Map<Integer, ClassResponse> classMap,
        java.util.Map<Integer, TeacherResponse> teacherMap,
        java.util.Map<Integer, SubjectResponse> subjectMap,
        java.util.Map<Integer, RoomResponse> roomMap,
        java.util.Map<Integer, GroupResponse> groupMap) {

        List<OrTLesson> flatLessons = new ArrayList<>();

        // Solver uchun vaqtincha ID generator (Har safar noldan boshlanadi)
        AtomicInteger solverIdGenerator = new AtomicInteger(1);

        for (LessonResponse raw : rawLessons) {
            // Lookup objects
            ClassResponse classInfo = raw.classId() != null ? classMap.get(raw.classId()) : null;
            if (classInfo == null && raw.classId() != null) {
                // Log or throw? Usually should be consistent.
            }
            TeacherResponse teacher = raw.teacherId() != null ? teacherMap.get(raw.teacherId()) : null;
            SubjectResponse subject = raw.subjectId() != null ? subjectMap.get(raw.subjectId()) : null;
            List<RoomResponse> rooms = new ArrayList<>();
            if (raw.roomIds() != null) {
                for (Integer roomId : raw.roomIds()) {
                    RoomResponse r = roomMap.get(roomId);
                    if (r != null) rooms.add(r);
                }
            }
            
            // 1-holat: Dars guruhlarga bo'lingan (Split Lesson)
            if (raw.groupDetails() != null && !raw.groupDetails().isEmpty()) {

                String sharedSyncId = UUID.randomUUID().toString();

                for (GroupLessonDetailResponse detail : raw.groupDetails()) {
                    TeacherResponse dTeacher = detail.teacherId() != null ? teacherMap.get(detail.teacherId()) : null;
                    SubjectResponse dSubject = detail.subjectId() != null ? subjectMap.get(detail.subjectId()) : null;
                    GroupResponse dGroup = detail.groupId() != null ? groupMap.get(detail.groupId()) : null;
                    List<RoomResponse> dRooms = new ArrayList<>();
                    if (detail.roomIds() != null) {
                        for (Integer id : detail.roomIds()) {
                             RoomResponse r = roomMap.get(id);
                             if (r != null) dRooms.add(r);
                        }
                    }

                    OrTLesson splitLesson = new OrTLesson(
                            solverIdGenerator.getAndIncrement(), // <-- UNIKAL ID (1, 2, 3...)
                            raw.id(),                            // <-- ORIGINAL ID (1064)
                            classInfo,
                            dTeacher,
                            dRooms,
                            dSubject != null ? dSubject : subject, // Subject fallback
                            dGroup,
                            sharedSyncId,
                            raw.lessonCount(),
                            raw.dayOfWeek(),
                            raw.hour(),
                            raw.period(),
                            raw.frequency()
                    );
                    flatLessons.add(splitLesson);
                }
            }
            // 2-holat: Oddiy dars (Butun sinf uchun)
            else {
                OrTLesson simpleLesson = new OrTLesson(
                        solverIdGenerator.getAndIncrement(), // <-- UNIKAL ID
                        raw.id(),                            // <-- ORIGINAL ID
                        classInfo,
                        teacher,
                        rooms,
                        subject,
                        raw.groupId() != null ? groupMap.get(raw.groupId()) : null,
                        null,
                        raw.lessonCount(),
                        raw.dayOfWeek(),
                        raw.hour(),
                        raw.period(),
                        raw.frequency()
                );
                flatLessons.add(simpleLesson);
            }
        }
        return flatLessons;
    }
}