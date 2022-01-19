package br.trilhas.ufpa.repository;

import br.trilhas.ufpa.domain.Fotografias;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Fotografias entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FotografiasRepository extends JpaRepository<Fotografias, Long> {}