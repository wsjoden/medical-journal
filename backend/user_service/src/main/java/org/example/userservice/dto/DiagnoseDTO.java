package org.example.userservice.dto;
import java.time.LocalDate;

public class DiagnoseDTO {
    private Long id;
    private String patientUserId;
    private String StaffUserId;
    private String diagnose;
    private String details;
    private LocalDate diagnosisDate;

    public DiagnoseDTO() {
    }

    public DiagnoseDTO(Long id, String patientUserId, String StaffUserId, String diagnose, String details, LocalDate diagnosisDate) {
        this.id = id;
        this.patientUserId = patientUserId;
        this.StaffUserId = StaffUserId;
        this.diagnose = diagnose;
        this.details = details;
        this.diagnosisDate = diagnosisDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientUserId() {
        return patientUserId;
    }

    public void setPatientUserId(String patientUserId) {
        this.patientUserId = patientUserId;
    }

    public String getStaffUserId() {
        return StaffUserId;
    }

    public void setStaffUserId(String staffUserId) {
        StaffUserId = staffUserId;
    }

    public String getDiagnose() {
        return diagnose;
    }

    public void setDiagnose(String diagnose) {
        this.diagnose = diagnose;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDate getDiagnosisDate() {
        return diagnosisDate;
    }

    public void setDiagnosisDate(LocalDate diagnosisDate) {
        this.diagnosisDate = diagnosisDate;
    }
}
