package org.example.userservice.dto;

import java.util.List;

public class PatientProfileDetailsDTO {
    private String userId;
    private String firstName;
    private String lastName;
    private String role;
    private List<DiagnoseDTO> diagnoseList;
    private String currentDiagnose;
    private List<EncounterDTO> encounterList;
    private List<ObservationDTO> observationList;

    public PatientProfileDetailsDTO() {
    }

    public PatientProfileDetailsDTO(String userId, String firstName, String lastName, List<DiagnoseDTO> diagnoseList, List<EncounterDTO> encounterList, List<ObservationDTO> observationList) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = "Patient";
        this.diagnoseList = diagnoseList;
        this.currentDiagnose = diagnoseList.isEmpty() ? null : diagnoseList.get(diagnoseList.size() - 1).getDiagnose();
        this.encounterList = encounterList;
        this.observationList = observationList;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<DiagnoseDTO> getDiagnoseList() {
        return diagnoseList;
    }

    public void setDiagnoseList(List<DiagnoseDTO> diagnoseList) {
        this.diagnoseList = diagnoseList;
    }

    public String getCurrentDiagnose() {
        return currentDiagnose;
    }

    public void setCurrentDiagnose(String currentDiagnose) {
        this.currentDiagnose = currentDiagnose;
    }

    public List<EncounterDTO> getEncounterList() {
        return encounterList;
    }

    public void setEncounterList(List<EncounterDTO> encounterList) {
        this.encounterList = encounterList;
    }

    public List<ObservationDTO> getObservationList() {
        return observationList;
    }

    public void setObservationList(List<ObservationDTO> observationList) {
        this.observationList = observationList;
    }
}