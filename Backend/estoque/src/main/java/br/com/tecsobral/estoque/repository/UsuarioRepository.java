package br.com.tecsobral.estoque.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.tecsobral.estoque.entity.Usuario;
import br.com.tecsobral.estoque.model.enums.Perfil;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    // Autenticação e verificação de login único (RN01)
    Optional<Usuario> findByLogin(String login);

    Optional<Usuario> findByLoginIgnoreCase(String login);
    
    boolean existsByLogin(String login);

    boolean existsByLoginIgnoreCase(String login);

    long countByPerfilAndAtivoTrue(Perfil perfil);

    java.util.List<Usuario> findAllByOrderByNomeAsc();
}
