package org.example.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.example.entities.Encounter;

public class EncounterRepository implements PanacheRepository<Encounter> {
}
