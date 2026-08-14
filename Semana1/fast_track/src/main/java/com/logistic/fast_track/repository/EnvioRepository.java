package com.logistic.fast_track.repository;

import com.logistic.fast_track.core.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {
    Optional<Envio> findByIdRastreo(String idRastreo);
}
