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
                createGrade("Grade 7", "Seventh Grade", 1),
                createGrade("Grade 8", "Eighth Grade", 2),
                createGrade("Grade 9", "Ninth Grade", 3),
                createGrade("Grade 10", "Tenth Grade", 4),
                createGrade("Grade 11", "Eleventh Grade", 5),
                createGrade("Grade 12", "Twelfth Grade", 6)
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
                createClassRoom("7A", "Class 7A", 30, getGradeByName(grades, "Grade 7")),
                createClassRoom("7B", "Class 7B", 30, getGradeByName(grades, "Grade 7")),
                createClassRoom("7C", "Class 7C", 30, getGradeByName(grades, "Grade 7")),
                createClassRoom("8A", "Class 8A", 30, getGradeByName(grades, "Grade 8")),
                createClassRoom("8B", "Class 8B", 30, getGradeByName(grades, "Grade 8")),
                createClassRoom("8C", "Class 8C", 30, getGradeByName(grades, "Grade 8")),
                createClassRoom("9A", "Class 9A", 30, getGradeByName(grades, "Grade 9")),
                createClassRoom("9B", "Class 9B", 30, getGradeByName(grades, "Grade 9")),
                createClassRoom("9C", "Class 9C", 30, getGradeByName(grades, "Grade 9")),
                createClassRoom("10A", "Class 10A", 30, getGradeByName(grades, "Grade 10")),
                createClassRoom("10B", "Class 10B", 30, getGradeByName(grades, "Grade 10")),
                createClassRoom("11A", "Class 11A", 30, getGradeByName(grades, "Grade 11")),
                createClassRoom("11B", "Class 11B", 30, getGradeByName(grades, "Grade 11")),
                createClassRoom("12A", "Class 12A", 30, getGradeByName(grades, "Grade 12")),
                createClassRoom("12B", "Class 12B", 30, getGradeByName(grades, "Grade 12"))
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