package com.quadrah.sims.service;

import com.quadrah.sims.model.Grade;
import com.quadrah.sims.model.ClassRoom;
import com.quadrah.sims.repository.GradesRepository;
import com.quadrah.sims.repository.ClassRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GradeService {

    private final GradesRepository gradesRepository;
    private final ClassRoomRepository classRoomRepository;

    @Autowired
    public GradeService(GradesRepository gradesRepository,
                        ClassRoomRepository classRoomRepository) {
        this.gradesRepository = gradesRepository;
        this.classRoomRepository = classRoomRepository;
    }

    // Grade Methods
    public List<Grade> getAllGrades() {
        return gradesRepository.findAllByOrderBySortOrderAsc();
    }

    public List<String> getAllGradeNames() {
        List<Grade> grades = gradesRepository.findAllByOrderBySortOrderAsc();

        if (grades.isEmpty()) {
            seedDefaultGrades();
            grades = gradesRepository.findAllByOrderBySortOrderAsc();
        }

        // Convert to DTO or just get names
        return grades.stream()
                .map(Grade::getGradeName)
                .collect(Collectors.toList());
    }

    public Grade getGradeById(Long id) {
        return gradesRepository.findById(id).orElse(null);
    }

    public Grade saveGrade(Grade grade) {
        return gradesRepository.save(grade);
    }

    public void deleteGrade(Long id) {
        gradesRepository.deleteById(id);
    }

    // Classroom Methods
    public List<ClassRoom> getAllClassRooms() {
        return classRoomRepository.findAllByOrderByClassNameAsc();
    }

    public List<String> getAllClassNames() {
        List<String> classNames = classRoomRepository.findAllClassNames();
        if (classNames.isEmpty()) {
            // Seed default classrooms if none exist
            seedDefaultClassRooms();
            classNames = classRoomRepository.findAllClassNames();
        }
        return classNames;
    }

    public List<ClassRoom> getClassRoomsByGradeId(Long gradeId) {
        return classRoomRepository.findByGradeIdOrderByClassNameAsc(gradeId);
    }

    public List<String> getClassNamesByGradeName(String gradeName) {
        List<ClassRoom> classRooms = classRoomRepository.findByGradeName(gradeName);
        return classRooms.stream()
                .map(ClassRoom::getClassName)
                .collect(Collectors.toList());
    }

    public ClassRoom saveClassRoom(ClassRoom classRoom) {
        return classRoomRepository.save(classRoom);
    }

    public void deleteClassRoom(Long id) {
        classRoomRepository.deleteById(id);
    }

    // Seeding methods
    private void seedDefaultGrades() {
        List<Grade> defaultGrades = List.of(
                createGrade("Form 1", "First Form", 1),
                createGrade("Form 2", "Second Form", 2),
                createGrade("Form 3", "Third Form", 3)
        );

        gradesRepository.saveAll(defaultGrades);
    }

    private void seedDefaultClassRooms() {
        // First, ensure grades exist
        if (gradesRepository.count() == 0) {
            seedDefaultGrades();
        }

        List<Grade> grades = gradesRepository.findAllByOrderBySortOrderAsc();

        List<ClassRoom> defaultClassRooms = List.of(
                // Form 1 classrooms (starting with 1)
                createClassRoom("1GS2", "Class 1GS2", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1HE1", "Class 1HE1", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GS5", "Class 1GS5", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GS4", "Class 1GS4", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GA1", "Class 1GA1", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1BS2", "Class 1BS2", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GS9", "Class 1GS9", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GA3", "Class 1GA3", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GA4", "Class 1GA4", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1HE5", "Class 1HE5", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GA9", "Class 1GA9", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GS3", "Class 1GS3", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GA8", "Class 1GA8", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1G S8", "Class 1G S8", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1HE4", "Class 1HE4", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GS1", "Class 1GS1", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GA7", "Class 1GA7", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GA2", "Class 1GA2", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GA6", "Class 1GA6", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GA5", "Class 1GA5", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1AS1", "Class 1AS1", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1PA1", "Class 1PA1", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1HE2", "Class 1HE2", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1HE3", "Class 1HE3", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GS8", "Class 1GS8", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GA10", "Class 1GA10", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1GA11", "Class 1GA11", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1BS1", "Class 1BS1", 30, getGradeByName(grades, "Form 1")),
                createClassRoom("1BS3", "Class 1BS3", 30, getGradeByName(grades, "Form 1")),

                // Form 2 classrooms (starting with 2)
                createClassRoom("2GA1", "Class 2GA1", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2HE3", "Class 2HE3", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2HE2", "Class 2HE2", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GA9", "Class 2GA9", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GA10", "Class 2GA10", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GS1", "Class 2GS1", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GS2", "Class 2GS2", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GS10", "Class 2GS10", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2BS1", "Class 2BS1", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GA7", "Class 2GA7", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GA5", "Class 2GA5", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GS7", "Class 2GS7", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GS3", "Class 2GS3", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GS9", "Class 2GS9", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GA4", "Class 2GA4", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2PA1", "Class 2PA1", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2HE5", "Class 2HE5", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2AS2", "Class 2AS2", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GS6", "Class 2GS6", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GS5", "Class 2GS5", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2BS3", "Class 2BS3", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2HE1", "Class 2HE1", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GS4", "Class 2GS4", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GA6", "Class 2GA6", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2BS2", "Class 2BS2", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GA8", "Class 2GA8", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2HE4", "Class 2HE4", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2PA2", "Class 2PA2", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GS8", "Class 2GS8", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GA3", "Class 2GA3", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GA2", "Class 2GA2", 30, getGradeByName(grades, "Form 2")),
                createClassRoom("2GS11", "Class 2GS11", 30, getGradeByName(grades, "Form 2")),

                // Form 3 classrooms (starting with 3)
                createClassRoom("3S5", "Class 3S5", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3S13", "Class 3S13", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3S7", "Class 3S7", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3H3", "Class 3H3", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3S1", "Class 3S1", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3S2", "Class 3S2", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3S11", "Class 3S11", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3B1", "Class 3B1", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3S9", "Class 3S9", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3H2", "Class 3H2", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3H5", "Class 3H5", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3S4", "Class 3S4", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3H4", "Class 3H4", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3H1", "Class 3H1", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3S15", "Class 3S15", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3A1", "Class 3A1", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3A2", "Class 3A2", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3S14", "Class 3S14", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3H6", "Class 3H6", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3S8", "Class 3S8", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3A4", "Class 3A4", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3B2", "Class 3B2", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3V1", "Class 3V1", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3A6", "Class 3A6", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3S12", "Class 3S12", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3A3", "Class 3A3", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3S10", "Class 3S10", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3S6", "Class 3S6", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3A5", "Class 3A5", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3A8", "Class 3A8", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3A7", "Class 3A7", 30, getGradeByName(grades, "Form 3")),
                createClassRoom("3B3", "Class 3B3", 30, getGradeByName(grades, "Form 3"))
        );

        classRoomRepository.saveAll(defaultClassRooms);
    }

    private Grade createGrade(String name, String description, int order) {
        Grade grade = new Grade();
        grade.setGradeName(name);
        grade.setDescription(description);
        grade.setSortOrder(order);
        return grade;
    }

    private ClassRoom createClassRoom(String name, String description, int capacity, Grade grade) {
        ClassRoom classRoom = new ClassRoom();
        classRoom.setClassName(name);
        classRoom.setDescription(description);
        classRoom.setCapacity(capacity);
        classRoom.setGrade(grade);
        return classRoom;
    }

    private Grade getGradeByName(List<Grade> grades, String name) {
        return grades.stream()
                .filter(g -> g.getGradeName().equals(name))
                .findFirst()
                .orElse(null);
    }
}