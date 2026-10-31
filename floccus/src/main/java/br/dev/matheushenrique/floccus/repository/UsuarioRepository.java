package br.dev.matheushenrique.floccus.repository;

import br.dev.matheushenrique.floccus.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmailAndIdNot(String email, String id);
}
