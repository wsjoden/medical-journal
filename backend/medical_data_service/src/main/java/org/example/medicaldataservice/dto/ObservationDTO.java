package org.example.medicaldataservice.dto;

import java.time.LocalDate;

public class ObservationDTO {
    private Long id;
    private String patientUserId;
    private String staffUserId;
    private String observation;
    private LocalDate observationDate;

    public ObservationDTO() {
    }

    public ObservationDTO(Long id, String patientUserId, String staffUserId, String observation, LocalDate observationDate) {
        this.id = id;
        this.patientUserId = patientUserId;
        this.staffUserId = staffUserId;
        this.observation = observation;
        this.observationDate = observationDate;
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

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public LocalDate getObservationDate() {
        return observationDate;
    }

    public void setObservationDate(LocalDate observationDate) {
        this.observationDate = observationDate;
    }
}
