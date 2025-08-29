package org.example.medicaldataservice.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Diagnose {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_user_id")
    private String patientUserId;

    @Column(name = "staff_user_id")
    private String staffUserId;

    private String diagnose;
    private String details;

    private LocalDate diagnosisDate;

    public Diagnose() {
    }

    public Diagnose(String patientUserId, String staffUserId, String diagnose, String details, LocalDate diagnosisDate) {
        this.patientUserId = patientUserId;
        this.staffUserId = staffUserId;
        this.diagnose = diagnose;
        this.details = details;
        this.diagnosisDate = diagnosisDate;
    }

    public Diagnose(String patientUserId, String staffUserId, String diagnose, String details) {
        this.patientUserId = patientUserId;
        this.staffUserId = staffUserId;
        this.diagnose = diagnose;
        this.details = details;
        this.diagnosisDate = LocalDate.now();
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
        return staffUserId;
    }

    public void setStaffUserId(String staffUserId) {
        this.staffUserId = staffUserId;
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
