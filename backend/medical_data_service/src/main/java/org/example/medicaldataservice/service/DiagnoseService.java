package org.example.medicaldataservice.service;

import org.example.medicaldataservice.model.Diagnose;
import org.example.medicaldataservice.repository.IDiagnoseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiagnoseService {

    private final IDiagnoseRepository diagnoseRepository;

    public DiagnoseService(IDiagnoseRepository diagnoseRepository) {
        this.diagnoseRepository = diagnoseRepository;
    }

    public void registerDiagnose(Diagnose diagnose) {
        diagnoseRepository.save(diagnose);
    }

    public List<Diagnose> findDiagnosesByPatientUserId(String patientUserId) {
        return diagnoseRepository.findByPatientUserId(patientUserId);
    }
}
