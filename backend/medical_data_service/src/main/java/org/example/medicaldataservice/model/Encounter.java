package org.example.medicaldataservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Encounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_user_id")
    private String patientUserId;

    @Column(name = "staff_user_id")
    private String staffUserId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate encounterDate;
    private String notes;

    public Encounter() {}

    public Encounter(String patientUserId, String staffUserId, LocalDate encounterDate, String notes) {
        this.patientUserId = patientUserId;
        this.staffUserId = staffUserId;
        this.encounterDate = encounterDate;
        this.notes = notes;
    }

    public Encounter(String patientUserId, String staffUserId, String notes) {
        this.patientUserId = patientUserId;
        this.staffUserId = staffUserId;
        this.encounterDate = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
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

    public LocalDate getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(LocalDate encounterDate) {
        this.encounterDate = encounterDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Encounter{" +
                "id=" + id +
                ", patientUserId=" + patientUserId +
                ", staffUserId=" + staffUserId +
                ", encounterDate=" + encounterDate +
                ", notes='" + notes + '\'' +
                '}';
    }
}
