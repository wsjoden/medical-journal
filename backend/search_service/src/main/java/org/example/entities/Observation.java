package org.example.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "observation")
public class Observation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_user_id")
    private String patientUserId;

    @Column(name = "staff_user_id")
    private String staffUserId;

    private String observation;

    @Column(name="observation_date")
    private LocalDate observationDate;

    public Observation() {}

    public Observation(String patientUserId, String staffUserId, String observation, LocalDate observationDate) {
        this.patientUserId = patientUserId;
        this.staffUserId = staffUserId;
        this.observation = observation;
        this.observationDate = observationDate;
    }

    public Observation(String patientUserId, String staffUserId, String observation) {
        this.patientUserId = patientUserId;
        this.staffUserId = staffUserId;
        this.observation = observation;
        this.observationDate = LocalDate.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
