package org.example.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.example.entities.Observation;

public class ObservationRepository implements PanacheRepository<Observation> {
}
