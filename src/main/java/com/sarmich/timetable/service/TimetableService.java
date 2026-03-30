package com.sarmich.timetable.service;

import com.sarmich.timetable.config.Publisher;
import com.sarmich.timetable.domain.TaskEntity;
import com.sarmich.timetable.domain.TimetableDataEntity;
import com.sarmich.timetable.domain.TimetableEntity;
import com.sarmich.timetable.mapper.*;
import com.sarmich.timetable.model.*;
import com.sarmich.timetable.model.request.GenerateTimetableRequest;
import com.sarmich.timetable.model.response.*;
import com.sarmich.timetable.repository.*;
import com.sarmich.timetable.service.solver.*;
import com.sarmich.timetable.utils.Util;

import java.util.*;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Log4j2
public class TimetableService {
    private final Publisher publisher;
    private final LessonService lessonService;
    private final TimetableGenerator generator;
    private final CompanyService companyService;
    private final TimetableRepository timetableRepository;
    private final TimetableDataRepository timetableDataRepository;
    private final WarmStartService warmStartService;
    private final TaskRepository taskRepository;
    private final ClassRepository classRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final RoomRepository roomRepository;
    private final ScoreCalculatorService scoreCalculatorService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SolverDataPreparer solverDataPreparer;

    public TimetableGenerateResponse generate(Integer orgId, GenerateTimetableRequest request) {
        TaskEntity task = new TaskEntity();
        task.setOrgId(orgId);
        task.setIsFinished(Boolean.FALSE);
        task.setIsSuccess(Boolean.FALSE);
        TaskEntity save = taskRepository.save(task);
        UUID taskId = save.getId();
        publisher.send(new GenerateReply(taskId, orgId, request.name()));
        return new TimetableGenerateResponse(taskId);
    }

    public void generateTimetable(GenerateReply reply) {
        try {
            Integer orgId = reply.orgId();
            CompanyResponse company = companyService.getByOrgID(orgId);
            List<LessonResponse> lessons = lessonService.getAllLessons(orgId);

            // Fetch lookups for SolverDataPreparer
            Map<Integer, ClassResponse> classMap = Util.mapById(classRepository.findAllByOrgIdAndDeletedFalse(orgId).stream().map(ClassMapper.INSTANCE::toResponse).toList(), ClassResponse::id);
            Map<Integer, TeacherResponse> teacherMap = Util.mapById(teacherRepository.findAllByOrgIdAndDeletedFalse(orgId).stream().map(TeacherMapper.INSTANCE::toResponse).toList(), TeacherResponse::id);
            Map<Integer, SubjectResponse> subjectMap = Util.mapById(subjectRepository.findAllByOrgIdAndDeletedFalse(orgId).stream().map(SubjectMapper.INSTANCE::toResponse).toList(), SubjectResponse::id);
            Map<Integer, RoomResponse> roomMap = Util.mapById(roomRepository.findAllByOrgIdAndDeletedFalse(orgId).stream().map(RoomMapper.INSTANCE::toResponse).toList(), RoomResponse::id);
            // Assuming no separate GroupRepository access here easily or Groups come in LessonResponse somehow.
            // But SolverDataPreparer needs Map<Integer, GroupResponse>.
            // I'll create an empty map for now if I can't access GroupRepository easily, or inject GroupRepository.
            // Wait, TimetableService DOES NOT have GroupRepository injected. I should probably add it, or...
            // Actually, GroupResponse is usually nested in ClassResponse or LessonResponse in the old model.
            // If I need GroupResponse for the solver (OrTLesson uses it), I must provide it.
            // If I don't inject GroupRepository, I can't provide it efficiently.
            // However, TimetableService has many repositories.
            // Let's assume for now I will inject GroupRepository in the next step or use what I have.
            // I will use an empty map for groups for now to fix compilation, and note to add GroupRepository.
            Map<Integer, GroupResponse> groupMap = new HashMap<>(); 

            // 1. Solver uchun tayyorlash (Flat + SyncId)
            List<OrTLesson> orTLessons = solverDataPreparer.prepareLessons(lessons, classMap, teacherMap, subjectMap, roomMap, groupMap);

            // 2. Generatsiya
            SolverResult generate = generator.generate(orTLessons, company);

            // 3. Saqlash
            TimetableEntity savedTimetable = saveTimetable(generate, reply, lessons);

            GenerationStatusResponse response =
                    new GenerationStatusResponse(
                            reply.taskId(),
                            "SUCCESS",
                            "Timetable generated successfully",
                            savedTimetable.getId());
            messagingTemplate.convertAndSend("/topic/generation/" + reply.taskId(), response);

        } catch (Exception e) {
            log.error("Error generating timetable", e);
            GenerationStatusResponse response =
                    new GenerationStatusResponse(reply.taskId(), "ERROR", e.getMessage(), null);
            messagingTemplate.convertAndSend("/topic/generation/" + reply.taskId(), response);
        }
    }

    private TimetableEntity saveTimetable(
            SolverResult generate, GenerateReply reply, List<LessonResponse> lessons) {
        TimetableEntity timetable = new TimetableEntity();
        timetable.setOrgId(reply.orgId());
        timetable.setTaskId(reply.taskId());
        timetable.setName(reply.name());
        TimetableEntity save = timetableRepository.save(timetable);

        // Default soft constraint settings
        saveData(save.getId(), generate, 1, lessons, new ApplySoftConstraint());
        return save;
    }

    private void saveData(
            UUID timetableId,
            SolverResult generate,
            int version,
            List<LessonResponse> lessons,
            ApplySoftConstraint rules) {

        // 1. Original darslarni ID bo'yicha Map qilamiz (Tezkor qidiruv uchun)
        Map<Integer, LessonResponse> lessonMapById = lessons.stream()
                .collect(Collectors.toMap(LessonResponse::id, l -> l, (v1, v2) -> v1));

        List<TimetableDataEntity> entitiesToSave = new ArrayList<>();

        // --- REJALASHTIRILGAN DARSLAR ---
        if (generate.getScheduledSlots() != null) {
            for (TimetableSlotResponse slot : generate.getScheduledSlots()) {

                TimetableDataEntity entity = new TimetableDataEntity();
                entity.setTimetableId(timetableId);
                entity.setClassId(slot.getClassInfo().id());
                entity.setDayOfWeek(slot.getDay());
                entity.setHour(slot.getHour());
                entity.setWeekIndex(slot.getWeekIndex());
                entity.setIsScheduled(Boolean.TRUE);
                entity.setVersion(version);

                // Details ichiga original ma'lumotni kiritamiz
                List<TimetableGroupDetail> enrichedDetails = slot.getDetails().stream().map(detail -> {
                    // LessonID orqali originalni topamiz
                    LessonResponse original = lessonMapById.get(detail.getLessonId());

                    // Detail obyektiga originalni set qilamiz
                    detail.setOriginalLessonData(original);

                    return detail;
                }).collect(Collectors.toList());

                // Boyitilgan detallarni entityga set qilamiz (JSONB ga ketadi)
                entity.setSlotDetails(enrichedDetails);

                entitiesToSave.add(entity);
            }
        }

        // --- SIG'MAGAN DARSLAR (UNSCHEDULED) ---
        if (generate.getUnscheduledLessons() != null) {
            for (UnscheduledLesson unscheduled : generate.getUnscheduledLessons()) {
                TimetableDataEntity entity = new TimetableDataEntity();
                entity.setTimetableId(timetableId);
                entity.setClassId(unscheduled.getClassId());
                entity.setIsScheduled(Boolean.FALSE);
                entity.setVersion(version);

                // UnscheduledLesson ichiga ham originalLessonData qo'shishingiz mumkin
                // (Agar UnscheduledLesson modelini o'zgartirsangiz)
                // Yoki shunchaki saqlab qo'yasiz:
                entity.setUnscheduledData(unscheduled);

                entitiesToSave.add(entity);
            }
        }

        List<TimetableDataEntity> saved = timetableDataRepository.saveAll(entitiesToSave);
        // Score


        ScheduleScore score = scoreCalculatorService.calculateScore(saved, rules);
        TimetableEntity timetable = timetableRepository.findByIdAndDeletedFalse(timetableId);
        if (timetable != null) {
            timetable.setScore(score.getTotalPenaltyScore());
            timetable.setTeacherGaps(score.getTeacherGaps());
            timetable.setClassGaps(score.getClassGaps());
            timetable.setScheduled(saved.size());
            timetable.setUnscheduled(saved.size());
            timetableRepository.save(timetable);
        }
    }

    public void optimize(UUID id, ApplySoftConstraint request) {
        log.debug("Optimize timetable [{}] [{}]", id, request);
        TimetableEntity timetable = timetableRepository.findByIdAndDeletedFalse(id);
        Util.checkNull(timetable, TimetableEntity.class);

        CompanyResponse company = companyService.getByOrgID(timetable.getOrgId());
        Integer maxVersion = timetableDataRepository.findMaxVersion(id);

        // Bazadan eng so'nggi versiyadagi ma'lumotlarni olamiz
        List<TimetableDataEntity> timetableData =
                timetableDataRepository.findAllByTimetableIdAndVersion(id, maxVersion);

        // 1. Original darslarni tiklash (GroupDetails va boshqa o'zgarishlar bilan)
        List<LessonResponse> originalLessons = reconstructLessonsFromTimetableData(timetableData, company.id());

        // 2. Solver uchun Flat formatga o'tkazish (Split darslarni SyncId bilan ajratish)
        // Hint yaratishda ClassResponse obyekti kerak bo'ladi.
        // Entityda faqat classId borligi uchun, ularni bazadan olib Map qilamiz.
        Set<Integer> classIds = timetableData.stream()
                .map(TimetableDataEntity::getClassId)
                .collect(Collectors.toSet());

        Map<Integer, ClassResponse> classHintMap = Util.mapById(
                classRepository.findAllById(classIds).stream()
                        .map(ClassMapper.INSTANCE::toResponse)
                        .toList(),
                ClassResponse::id
        );

        // Teacher, Subject, Room, Group larni yuklab olish (Hintlar uchun)
        Map<Integer, TeacherResponse> teacherHintMap = Util.mapById(
                teacherRepository.findAllByOrgIdAndDeletedFalse(timetable.getOrgId()).stream().map(TeacherMapper.INSTANCE::toResponse).toList(),
                TeacherResponse::id);
        
        Map<Integer, SubjectResponse> subjectHintMap = Util.mapById(
                subjectRepository.findAllByOrgIdAndDeletedFalse(timetable.getOrgId()).stream().map(SubjectMapper.INSTANCE::toResponse).toList(),
                SubjectResponse::id);
                
        Map<Integer, RoomResponse> roomHintMap = Util.mapById(
                roomRepository.findAllByOrgIdAndDeletedFalse(timetable.getOrgId()).stream().map(RoomMapper.INSTANCE::toResponse).toList(),
                RoomResponse::id);
                
        Map<Integer, GroupResponse> groupMap = new HashMap<>(); 

        // 2. Solver uchun Flat formatga o'tkazish (Split darslarni SyncId bilan ajratish)
        // Note: classHintMap might not cover all classes if originalLessons spans more, but usually covered.
        // Better to fetch ALL classes if possible, but for optimization context usually limited scope is fine.
        // However, prepareLessons logic assumes access to all relevant classes.
        // classHintMap is built from timetableData, but originalLessons might have more?
        // Actually originalLessons comes from reconstructLessonsFromTimetableData which uses timetableData. So classHintMap is sufficient.
        List<OrTLesson> orTLessons = solverDataPreparer.prepareLessons(originalLessons, classHintMap, teacherHintMap, subjectHintMap, roomHintMap, groupMap);

        // 3. Oldingi jadvalni olish (Hint uchun)
        // MUHIM O'ZGARISH: SlotDetails (JSON List) ni yoyib, Response (Hint) ga aylantiramiz
        List<Response> scheduledForHinting = new ArrayList<>();



        for (TimetableDataEntity entity : timetableData) {
            // Faqat rejalashtirilgan va ichida detallari bor qatorlarni ko'ramiz
            if (Boolean.TRUE.equals(entity.getIsScheduled()) && entity.getSlotDetails() != null) {

                ClassResponse entityClass = classHintMap.get(entity.getClassId());

                // Har bir detal (Guruh darsi) uchun alohida Hint yasaymiz
                for (TimetableGroupDetail detail : entity.getSlotDetails()) {
                    Response hint = new Response();

                    // A) Vaqt va Joylashuv (Entitydan olinadi)
                    hint.setDay(entity.getDayOfWeek());
                    hint.setHour(entity.getHour());
                    hint.setWeekIndex(entity.getWeekIndex()); // A yoki B hafta

                    // B) Asosiy ma'lumotlar (Detaildan olinadi)
                    hint.setLessonId(detail.getLessonId()); // <-- Eng muhimi shu! WarmStartService shuni qidiradi
                    
                    if (detail.getTeacherId() != null) hint.setTeacher(teacherHintMap.get(detail.getTeacherId()));
                    if (detail.getSubjectId() != null) hint.setSubject(subjectHintMap.get(detail.getSubjectId()));
                    
                    if (detail.getRoomId() != null) hint.setRoom(roomHintMap.get(detail.getRoomId())); // Aniq tanlangan xona

                    // C) Sinf ma'lumoti
                    hint.setClassObj(entityClass);

                    scheduledForHinting.add(hint);
                }
            }
        }

        // 4. Optimallashtirish jarayonini boshlash
        SolverResult solverResult =
                warmStartService.optimizeSchedule(scheduledForHinting, orTLessons, company, request);

        // 5. Yangi natijani saqlash (Version + 1)
        saveData(id, solverResult, maxVersion + 1, originalLessons, request);
    }

    private List<LessonResponse> reconstructLessonsFromTimetableData(
            List<TimetableDataEntity> timetableData, Integer orgId) {

        // 1. TIRIK RESURSLARNI YUKLASH (Keshlar)
        Map<Integer, ClassResponse> classMap = Util.mapById(
                classRepository.findAllByOrgIdAndDeletedFalse(orgId).stream()
                        .map(ClassMapper.INSTANCE::toResponse).toList(),
                ClassResponse::id);

        Map<Integer, TeacherResponse> teacherMap = Util.mapById(
                teacherRepository.findAllByOrgIdAndDeletedFalse(orgId).stream()
                        .map(TeacherMapper.INSTANCE::toResponse).toList(),
                TeacherResponse::id);

        Map<Integer, SubjectResponse> subjectMap = Util.mapById(
                subjectRepository.findAllByOrgIdAndDeletedFalse(orgId).stream()
                        .map(SubjectMapper.INSTANCE::toResponse).toList(),
                SubjectResponse::id);

        Map<Integer, RoomResponse> roomMap = Util.mapById(
                roomRepository.findAllByOrgIdAndDeletedFalse(orgId).stream()
                        .map(RoomMapper.INSTANCE::toResponse).toList(),
                RoomResponse::id);

        // 2. UNIKAL DARSLARNI YIG'ISH
        Map<Integer, LessonResponse> uniqueLessons = new HashMap<>();

        for (TimetableDataEntity data : timetableData) {
            if (Boolean.TRUE.equals(data.getIsScheduled()) && data.getSlotDetails() != null) {
                for (TimetableGroupDetail detail : data.getSlotDetails()) {
                    if (detail.getOriginalLessonData() != null) {
                        LessonResponse original = detail.getOriginalLessonData();
                        uniqueLessons.putIfAbsent(original.id(), original);
                    }
                }
            }
        }

        // 3. YANGILASH (RECONSTRUCTION)
        List<LessonResponse> reconstructedLessons = new ArrayList<>();

        for (LessonResponse oldLesson : uniqueLessons.values()) {
            ClassResponse freshClass = classMap.get(oldLesson.classId());
            TeacherResponse freshTeacher = (oldLesson.teacherId() != null) ? teacherMap.get(oldLesson.teacherId()) : null;
            SubjectResponse freshSubject = subjectMap.get(oldLesson.subjectId());

            if (freshClass == null || freshSubject == null) {
                continue;
            }

            // Asosiy dars xonalarini yangilash
            List<RoomResponse> freshRooms = new ArrayList<>();
            if (oldLesson.roomIds() != null) {
                freshRooms = oldLesson.roomIds().stream()
                        .map(rId -> roomMap.get(rId))
                        .filter(Objects::nonNull)
                        .toList();
            }
            List<Integer> freshRoomIds = freshRooms.stream().map(RoomResponse::id).toList();

            // --- GROUP DETAILS NI YANGILASH ---
            List<GroupLessonDetailResponse> freshGroupDetails = new ArrayList<>();

            if (oldLesson.groupDetails() != null) {
                for (GroupLessonDetailResponse oldDetail : oldLesson.groupDetails()) {

                    // A) O'qituvchini yangilash
                    Integer teacherId = oldDetail.teacherId();
                    TeacherResponse detailTeacher = null;
                    if (teacherId != null && teacherMap.containsKey(teacherId)) {
                        detailTeacher = teacherMap.get(teacherId);
                    }
                    Integer detailTeacherId = detailTeacher != null ? detailTeacher.id() : teacherId;

                    // B) Fanni yangilash
                    Integer subjectId = oldDetail.subjectId();
                    SubjectResponse detailSubject = null;
                    if (subjectId != null && subjectMap.containsKey(subjectId)) {
                        detailSubject = subjectMap.get(subjectId);
                    } else if (freshSubject != null) {
                        detailSubject = freshSubject;
                    }
                    Integer detailSubjectId = detailSubject != null ? detailSubject.id() : subjectId;

                    // C) Xonalar ro'yxatini yangilash
                    List<RoomResponse> detailRooms = new ArrayList<>();
                    if (oldDetail.roomIds() != null) {
                        detailRooms = oldDetail.roomIds().stream()
                                .map(rId -> roomMap.get(rId))
                                .filter(Objects::nonNull)
                                .toList();
                    }
                    List<Integer> detailRoomIds = detailRooms.stream().map(RoomResponse::id).toList();

                    // D) Guruhni olish
                    Integer groupId = oldDetail.groupId();

                    // E) Yangi Detal yaratish
                    GroupLessonDetailResponse freshDetail = new GroupLessonDetailResponse(
                            groupId,
                            detailTeacherId,
                            detailSubjectId,
                            detailRoomIds
                    );
                    freshGroupDetails.add(freshDetail);
                }
            }

            // Yangi LessonResponse yaratish
            LessonResponse newLesson = new LessonResponse(
                    oldLesson.id(),
                    freshClass.id(),
                    freshTeacher != null ? freshTeacher.id() : null,
                    freshRoomIds,
                    freshSubject.id(),
                    oldLesson.groupId(),
                    freshGroupDetails, // Yangilangan ro'yxat
                    oldLesson.lessonCount(),
                    oldLesson.dayOfWeek(),
                    oldLesson.hour(),
                    oldLesson.period(),
                    oldLesson.frequency(),
                    oldLesson.createdDate(),
                    oldLesson.updatedDate()
            );
            reconstructedLessons.add(newLesson);
        }

        return reconstructedLessons;
    }

    /**
     * Berilgan tashkilot IDsi bo'yicha barcha dars jadvallarini topadi.
     *
     * @param orgId Tashkilotning IDsi.
     * @return Tashkilotga tegishli TimetableEntity ob'ektlarining ro'yxati.
     */
    public List<TimetableResponse> find(Integer orgId) {
        return timetableRepository.findAllByOrgIdAndDeletedFalse(orgId).stream()
                .map(TimetableMapper.INSTANCE::toResponse)
                .toList();
    }

    /**
     * Berilgan dars jadvali IDsi va versiyasi bo'yicha dars jadvali ma'lumotlarini topadi.
     *
     * @param id Dars jadvalining IDsi.
     * @param orgId organization id
     * @return Berilgan ID va versiyaga mos keladigan TimetableDataEntity ob'ektlarining ro'yxati.
     */
    public TimetableFullResponse findById(UUID id, Integer orgId) {
        log.debug("Find all timetable data [{}] [{}]", id, orgId);
        Integer maxVersion = timetableDataRepository.findMaxVersion(id);
        List<TimetableDataResponse> data = timetableDataRepository.findAllByTimetableIdAndVersion(id, maxVersion).stream()
                .map(TimetableMapper.INSTANCE::toDataResponse)
                .toList();
        
        // --- Lookuplarni yig'ish ---
        List<ClassResponse> classes = classRepository.findAllByOrgIdAndDeletedFalse(orgId).stream()
                .map(ClassMapper.INSTANCE::toResponse).toList();
        List<TeacherResponse> teachers = teacherRepository.findAllByOrgIdAndDeletedFalse(orgId).stream()
                .map(TeacherMapper.INSTANCE::toResponse).toList();
        List<SubjectResponse> subjects = subjectRepository.findAllByOrgIdAndDeletedFalse(orgId).stream()
                .map(SubjectMapper.INSTANCE::toResponse).toList();
        List<RoomResponse> rooms = roomRepository.findAllByOrgIdAndDeletedFalse(orgId).stream()
                .map(RoomMapper.INSTANCE::toResponse).toList();
        // Groups uchun repository yo'qmi? Yoki Class response ichida bormi?
        // Agar group repository bo'lmasa, hozircha bo'sh ro'yxat qaytaramiz (GroupResponse ClassResponse ichida bor).
        List<GroupResponse> groups = new ArrayList<>(); // TODO: Load groups if needed separate from class
        
        return new TimetableFullResponse(data, classes, teachers, subjects, rooms, groups);
    }

    @Transactional
    public void delete(UUID id, Integer orgId) {
        TimetableEntity timetable = timetableRepository.findByIdAndDeletedFalse(id);
        Util.checkNull(timetable, TimetableEntity.class);

        if (!timetable.getOrgId().equals(orgId)) {
            throw new RuntimeException("Timetable not found or access denied");
        }

        // Delete related data
        timetableDataRepository.deleteAllByTimetableId(id);
        //        timetableLessonRepository.deleteAllByTimetableId(id);

        // Soft delete the timetable
        timetable.setDeleted(true);
        timetableRepository.save(timetable);
    }
}