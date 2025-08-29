package org.example.medicaldataservice.dto;

import java.time.LocalDate;

public class EncounterDTO {
    private Long id;
    private String patientUserId;
    private String staffUserId;
    private String notes;
    private LocalDate encounterDate;

    public EncounterDTO() {
    }

    public EncounterDTO(Long id, String patientUserId, String staffUserId, String notes, LocalDate encounterDate) {
        this.id = id;
        this.patientUserId = patientUserId;
        this.staffUserId = staffUserId;
        this.notes = notes;
        this.encounterDate = encounterDate;
    }

    public Long getId() {
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(LocalDate encounterDate) {
        this.encounterDate = encounterDate;
    }
}
