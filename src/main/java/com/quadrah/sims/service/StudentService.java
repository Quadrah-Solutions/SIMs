package com.quadrah.sims.service;

import com.quadrah.sims.dto.BulkUploadError;
import com.quadrah.sims.dto.BulkUploadResponse;
import com.quadrah.sims.dto.StudentDTO;
import com.quadrah.sims.exception.ResourceNotFoundException;
import com.quadrah.sims.model.Allergy;
import com.quadrah.sims.model.EmergencyContact;
import com.quadrah.sims.model.Student;
import com.quadrah.sims.repository.StudentRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.sql.JDBCType.BOOLEAN;
import static java.sql.JDBCType.NUMERIC;
import static javax.management.openmbean.SimpleType.STRING;
import static org.openxmlformats.schemas.spreadsheetml.x2006.main.STCfvoType.FORMULA;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    // Add this method for filtering with pagination
    public Page<StudentDTO> getStudentsWithFilters(
            Pageable pageable,
            String search,
            String grade,
            String className) {

        Specification<Student> spec = Specification.where(null);

        // Search filter (search in firstName, lastName, studentId)
        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("firstName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("lastName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("studentId")), "%" + search.toLowerCase() + "%")
                    )
            );
        }

        // Grade filter
        if (grade != null && !grade.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("gradeLevel"), grade)
            );
        }

        // Class filter (using homeroom)
        if (className != null && !className.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("homeroom"), className)
            );
        }

        // Apply pagination and sorting
        Page<Student> studentsPage = studentRepository.findAll(spec, pageable);

        // Convert to DTOs
        List<StudentDTO> studentDTOs = studentsPage.getContent().stream()
                .map(StudentDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(studentDTOs, pageable, studentsPage.getTotalElements());
    }

    public List<StudentDTO> getAllStudentsDTO() {
        return studentRepository.findAll().stream()
                .map(StudentDTO::new)
                .collect(Collectors.toList());
    }



    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAllByOrderByLastNameAscFirstNameAsc();
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Optional<Student> getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    public List<Student> getStudentsByGradeLevel(String gradeLevel) {
        return studentRepository.findByGradeLevel(gradeLevel);
    }

    public List<Student> getStudentsByHomeroom(String homeroom) {
        return studentRepository.findByHomeroom(homeroom);
    }

    public List<Student> searchStudentsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllStudents();
        }
        return studentRepository.findByNameContainingIgnoreCase(name.trim());
    }

    public Student createStudent(Student student) {
        validateStudent(student);

        // Check if student ID already exists
        if (studentRepository.existsByStudentId(student.getStudentId())) {
            throw new IllegalArgumentException("Student with ID " + student.getStudentId() + " already exists.");
        }

        // Set student reference on each emergency contact
        if (student.getEmergencyContacts() != null) {
            for (EmergencyContact contact : student.getEmergencyContacts()) {
                contact.setStudent(student); // Set the bidirectional relationship

                // If isPrimary is null, set default to false
                if (contact.getIsPrimary() == null) {
                    contact.setIsPrimary(false);
                }
            }
        }

        // Set student reference on each allergy
        if (student.getAllergies() != null) {
            for (Allergy allergy : student.getAllergies()) {
                allergy.setStudent(student); // Set the bidirectional relationship
            }
        }

        // CascadeType.ALL should handle saving the related entities
        return studentRepository.save(student);
    }

    public Student updateStudent(Long id, Student studentDetails) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + id));

        validateStudent(studentDetails);

        // Check if student ID is being changed to an existing one
        if (!student.getStudentId().equals(studentDetails.getStudentId()) &&
                studentRepository.existsByStudentId(studentDetails.getStudentId())) {
            throw new IllegalArgumentException("Student with ID " + studentDetails.getStudentId() + " already exists.");
        }

        student.setStudentId(studentDetails.getStudentId());
        student.setFirstName(studentDetails.getFirstName());
        student.setLastName(studentDetails.getLastName());
        student.setGradeLevel(studentDetails.getGradeLevel());
        student.setHomeroom(studentDetails.getHomeroom());
        student.setDateOfBirth(studentDetails.getDateOfBirth());
        student.setGender(studentDetails.getGender());
        student.setSpecialNotes(studentDetails.getSpecialNotes());

        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + id));

        // Check if student has visits (optional business rule)
        // if (!student.getVisits().isEmpty()) {
        //     throw new IllegalStateException("Cannot delete student with existing visit records.");
        // }

        studentRepository.delete(student);
    }

    public boolean studentExists(String studentId) {
        return studentRepository.existsByStudentId(studentId);
    }

    private void validateStudent(Student student) {
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID is required.");
        }
        if (student.getFirstName() == null || student.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (student.getLastName() == null || student.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required.");
        }
    }


//    BulkUploadResponse processBulkUpload(MultipartFile file);


    public BulkUploadResponse processBulkUpload(MultipartFile file) {
        List<BulkUploadError> errors = new ArrayList<>();
        int successfulCount = 0;

        try {
            String filename = file.getOriginalFilename();

            if (filename == null) {
                throw new IllegalArgumentException("File name is null");
            }

            String lowerCaseFilename = filename.toLowerCase();

            if (lowerCaseFilename.endsWith(".csv")) {
                return processCSVFile(file, errors);
            } else if (lowerCaseFilename.endsWith(".xlsx") || lowerCaseFilename.endsWith(".xls")) {
                return processExcelFile(file, errors);
            } else {
                throw new IllegalArgumentException("Unsupported file format");
            }

        } catch (Exception e) {
            errors.add(new BulkUploadError(0, null, "File", "Error reading file: " + e.getMessage()));
            return new BulkUploadResponse("Failed to process file", successfulCount, errors.size(), errors);
        }
    }

    private BulkUploadResponse processCSVFile(MultipartFile file, List<BulkUploadError> errors) {
        int successfulCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            List<CSVRecord> records = csvParser.getRecords();

            for (int i = 0; i < records.size(); i++) {
                CSVRecord record = records.get(i);
                int rowNumber = i + 2; // +2 because header is row 1 and we're 0-indexed

                try {
                    Student student = createStudentFromCSVRecord(record);

                    // Validate student
                    validateStudent(student, rowNumber, errors);

                    // Check for duplicate student ID
                    if (studentRepository.existsByStudentId(student.getStudentId())) {
                        errors.add(new BulkUploadError(rowNumber, student.getStudentId(),
                                "studentId", "Student ID already exists"));
                        continue;
                    }

                    // Save the student
                    studentRepository.save(student);
                    successfulCount++;

                } catch (Exception e) {
                    errors.add(new BulkUploadError(rowNumber,
                            record.isSet("Student ID") ? record.get("Student ID") : "Unknown",
                            "General", e.getMessage()));
                }
            }

            String message = successfulCount > 0
                    ? String.format("Successfully imported %d students. %d failed.", successfulCount, errors.size())
                    : "No students were imported.";

            return new BulkUploadResponse(message, successfulCount, errors.size(), errors);

        } catch (Exception e) {
            throw new RuntimeException("Error processing CSV file", e);
        }
    }

    private BulkUploadResponse processExcelFile(MultipartFile file, List<BulkUploadError> errors) {
        int successfulCount = 0;

        try (Workbook workbook = getWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            int rowNumber = 2; // Starting from row 2 (row 1 is header)

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                try {
                    Student student = createStudentFromExcelRow(row);

                    // Validate student
                    validateStudent(student, rowNumber, errors);

                    // Check for duplicate student ID
                    if (studentRepository.existsByStudentId(student.getStudentId())) {
                        errors.add(new BulkUploadError(rowNumber, student.getStudentId(),
                                "studentId", "Student ID already exists"));
                        rowNumber++;
                        continue;
                    }

                    // Save the student
                    studentRepository.save(student);
                    successfulCount++;

                } catch (Exception e) {
                    errors.add(new BulkUploadError(rowNumber, "Unknown", "General", e.getMessage()));
                }

                rowNumber++;
            }

            String message = successfulCount > 0
                    ? String.format("Successfully imported %d students. %d failed.", successfulCount, errors.size())
                    : "No students were imported.";

            return new BulkUploadResponse(message, successfulCount, errors.size(), errors);

        } catch (Exception e) {
            throw new RuntimeException("Error processing Excel file", e);
        }
    }

    private Workbook getWorkbook(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException("File name is null");
        }

        if (filename.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(file.getInputStream());
        } else if (filename.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(file.getInputStream());
        } else {
            throw new IllegalArgumentException("Unsupported Excel file format");
        }
    }

    private Student createStudentFromCSVRecord(CSVRecord record) {
        Student student = new Student();

        student.setStudentId(getStringValue(record, "Student ID"));
        student.setFirstName(getStringValue(record, "First Name"));
        student.setLastName(getStringValue(record, "Last Name"));
        student.setGradeLevel(getStringValue(record, "Grade Level"));
        student.setHomeroom(getStringValue(record, "Class"));
        student.setDateOfBirth(parseDate(getStringValue(record, "Date of Birth")));
        student.setGender(getStringValue(record, "Gender"));
        student.setBoardingStatus(getStringValue(record, "Boarding Status"));

        // Set default values for optional fields
        if (student.getBoardingStatus() == null || student.getBoardingStatus().isEmpty()) {
            student.setBoardingStatus("DAY");
        }

        return student;
    }

    private Student createStudentFromExcelRow(Row row) {
        Student student = new Student();

        student.setStudentId(getCellStringValue(row.getCell(0))); // Student ID
        student.setFirstName(getCellStringValue(row.getCell(1))); // First Name
        student.setLastName(getCellStringValue(row.getCell(2))); // Last Name
        student.setGradeLevel(getCellStringValue(row.getCell(3))); // Grade Level
        student.setHomeroom(getCellStringValue(row.getCell(4))); // Class
        student.setDateOfBirth(parseDate(getCellStringValue(row.getCell(5)))); // Date of Birth
        student.setGender(getCellStringValue(row.getCell(6))); // Gender

        // Boarding Status (might be in column 7 or might not exist)
        if (row.getCell(7) != null) {
            student.setBoardingStatus(getCellStringValue(row.getCell(7)));
        } else {
            student.setBoardingStatus("DAY");
        }

        return student;
    }

    private String getStringValue(CSVRecord record, String header) {
        return record.isSet(header) ? record.get(header).trim() : null;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            // Try multiple date formats
            DateTimeFormatter[] formatters = {
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                    DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                    DateTimeFormatter.ISO_LOCAL_DATE
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDate.parse(dateStr.trim(), formatter);
                } catch (DateTimeParseException e) {
                    // Try next format
                }
            }

            throw new IllegalArgumentException("Invalid date format: " + dateStr);

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date: " + dateStr);
        }
    }

    private void validateStudent(Student student, int rowNumber, List<BulkUploadError> errors) {
        // Validate Student ID
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            errors.add(new BulkUploadError(rowNumber, null, "Student ID", "Student ID is required"));
        } else if (!student.getStudentId().matches("^MG0\\d{11}$")) {
            errors.add(new BulkUploadError(rowNumber, student.getStudentId(),
                    "Student ID", "Student ID must start with MG0 followed by 11 digits"));
        }

        // Validate First Name
        if (student.getFirstName() == null || student.getFirstName().trim().isEmpty()) {
            errors.add(new BulkUploadError(rowNumber, student.getStudentId(),
                    "First Name", "First name is required"));
        }

        // Validate Last Name
        if (student.getLastName() == null || student.getLastName().trim().isEmpty()) {
            errors.add(new BulkUploadError(rowNumber, student.getStudentId(),
                    "Last Name", "Last name is required"));
        }

        // Validate Grade Level
        if (student.getGradeLevel() == null || student.getGradeLevel().trim().isEmpty()) {
            errors.add(new BulkUploadError(rowNumber, student.getStudentId(),
                    "Grade Level", "Grade level is required"));
        }

        // Validate Class
        if (student.getHomeroom() == null || student.getHomeroom().trim().isEmpty()) {
            errors.add(new BulkUploadError(rowNumber, student.getStudentId(),
                    "Class", "Class is required"));
        }

        // Validate Date of Birth
        if (student.getDateOfBirth() == null) {
            errors.add(new BulkUploadError(rowNumber, student.getStudentId(),
                    "Date of Birth", "Date of birth is required"));
        } else if (student.getDateOfBirth().isAfter(LocalDate.now())) {
            errors.add(new BulkUploadError(rowNumber, student.getStudentId(),
                    "Date of Birth", "Date of birth cannot be in the future"));
        }

        // Validate Gender
        if (student.getGender() == null || student.getGender().trim().isEmpty()) {
            errors.add(new BulkUploadError(rowNumber, student.getStudentId(),
                    "Gender", "Gender is required"));
        } else {
            String gender = student.getGender().toUpperCase();
            if (!gender.equals("MALE") && !gender.equals("FEMALE") &&
                    !gender.equals("OTHER") && !gender.equals("PREFER NOT TO SAY")) {
                errors.add(new BulkUploadError(rowNumber, student.getStudentId(),
                        "Gender", "Gender must be: Male, Female, Other, or Prefer not to say"));
            }
        }

        // Validate Boarding Status
        if (student.getBoardingStatus() != null && !student.getBoardingStatus().trim().isEmpty()) {
            String status = student.getBoardingStatus().toUpperCase();
            if (!status.equals("DAY") && !status.equals("BOARDING")) {
                errors.add(new BulkUploadError(rowNumber, student.getStudentId(),
                        "Boarding Status", "Boarding status must be: DAY or BOARDING"));
            }
        }
    }
}
