package projeto.organizacao.project.repository;

import projeto.organizacao.project.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    List<Tarefa> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Tarefa> findByDataHoraAfter(LocalDateTime hora);
}
