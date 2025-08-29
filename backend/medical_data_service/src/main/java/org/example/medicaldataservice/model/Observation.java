package org.example.medicaldataservice.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Observation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_user_id")
    private String patientUserId;

    @Column(name = "staff_user_id")
    private String staffUserId;

    private String observation;
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

    @Override
    public String toString() {
        return "Observation{" +
                "id=" + id +
                ", patientUserId=" + patientUserId +
                ", staffUserId=" + staffUserId +
                ", observation='" + observation + '\'' +
                ", observationDate=" + observationDate +
                '}';
    }
}
