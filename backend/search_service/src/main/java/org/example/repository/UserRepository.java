package org.example.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.example.entities.User;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public List<User> searchPatients(
            String genericSearch,
            String firstName,
            String lastName,
            String diagnose,
            String encounterDate,
            String observation,
            String staffFirstName,
            String staffLastName
    ) {
        StringBuilder query = new StringBuilder("SELECT u FROM User u WHERE u.role = 'patient' ");
        Map<String, Object> params = new HashMap<>();

        if (genericSearch != null && !genericSearch.isEmpty()) {
            query.append("AND (u.firstName LIKE :genericSearch OR u.lastName LIKE :genericSearch) ");
            params.put("genericSearch", "%" + genericSearch + "%");
        }

        if (firstName != null && !firstName.isEmpty()) {
            query.append("AND u.firstName LIKE :firstName ");
            params.put("firstName", "%" + firstName + "%");
        }

        if (lastName != null && !lastName.isEmpty()) {
            query.append("AND u.lastName LIKE :lastName ");
            params.put("lastName", "%" + lastName + "%");
        }

        if (diagnose != null && !diagnose.isEmpty()) {
            query.append("AND u.id IN (SELECT d.patientUserId FROM Diagnose d WHERE d.diagnose LIKE :diagnose) ");
            params.put("diagnose", "%" + diagnose + "%");
        }

        if (encounterDate != null && !encounterDate.isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(encounterDate);
                query.append("AND u.id IN (SELECT e.patientUserId FROM Encounter e WHERE e.encounterDate = :encounterDate) ");
                params.put("encounterDate", date);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format for encounterDate: " + encounterDate);
            }
        }

        if (observation != null && !observation.isEmpty()) {
            query.append("AND u.id IN (SELECT o.patientUserId FROM Observation o WHERE o.observation LIKE :observation) ");
            params.put("observation", "%" + observation + "%");
        }

        if (staffFirstName != null && !staffFirstName.isEmpty()) {
            query.append("AND u.id IN (SELECT e.patientUserId FROM Encounter e JOIN User s ON e.staffUserId = s.id WHERE s.firstName LIKE :staffFirstName ");
            query.append("OR u.id IN (SELECT o.patientUserId FROM Observation o JOIN User s ON o.staffUserId = s.id WHERE s.firstName LIKE :staffFirstName) ");
            query.append("OR u.id IN (SELECT d.patientUserId FROM Diagnose d JOIN User s ON d.staffUserId = s.id WHERE s.firstName LIKE :staffFirstName)) ");
            params.put("staffFirstName", "%" + staffFirstName + "%");
        }

        if (staffLastName != null && !staffLastName.isEmpty()) {
            query.append("AND u.id IN (SELECT e.patientUserId FROM Encounter e JOIN User s ON e.staffUserId = s.id WHERE s.lastName LIKE :staffLastName ");
            query.append("OR u.id IN (SELECT o.patientUserId FROM Observation o JOIN User s ON o.staffUserId = s.id WHERE s.lastName LIKE :staffLastName) ");
            query.append("OR u.id IN (SELECT d.patientUserId FROM Diagnose d JOIN User s ON d.staffUserId = s.id WHERE s.lastName LIKE :staffLastName)) ");
            params.put("staffLastName", "%" + staffLastName + "%");
        }

        TypedQuery<User> typedQuery = getEntityManager().createQuery(query.toString(), User.class);
        params.forEach(typedQuery::setParameter);

        return typedQuery.getResultList();
    }
}
