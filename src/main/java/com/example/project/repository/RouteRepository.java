package com.example.project.repository;

import com.example.project.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByFromLocation_IdAndToLocation_Id(Long fromLocationId, Long toLocationId);
}
