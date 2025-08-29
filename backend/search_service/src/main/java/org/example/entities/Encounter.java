package org.example.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "encounter")
public class Encounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_user_id")
    private String patientUserId;

    @Column(name = "staff_user_id")
    private String staffUserId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column (name = "encounter_date")
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

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getpatientUserId() {
        return patientUserId;
    }

    public void setpatientUserId(String patientUserId) {
        this.patientUserId = patientUserId;
    }

    public String getstaffUserId() {
        return staffUserId;
    }

    public void setstaffUserId(String staffUserId) {
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

}