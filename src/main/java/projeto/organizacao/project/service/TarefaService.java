package projeto.organizacao.project.service;

import org.aspectj.util.Reflection;
import org.springframework.http.HttpStatus;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.server.ResponseStatusException;
import projeto.organizacao.project.dto.TarefaDTO;
import projeto.organizacao.project.exceptions.ResourceNotFoundException;
import projeto.organizacao.project.model.Status;
import projeto.organizacao.project.model.Tarefa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projeto.organizacao.project.repository.TarefaRepository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class TarefaService {
    @Autowired
    TarefaRepository tarefaRepository;

    public Tarefa cadastrarTarefa(Tarefa tarefa){
        return tarefaRepository.save(tarefa);
    }

    public List<Tarefa> pegarTarefas() {
        return tarefaRepository.findAll().stream().sorted(Comparator.comparing(Tarefa::getStatus)).toList();
    }

    public List<Tarefa> filtrarPorData(LocalDateTime inicio, LocalDateTime fim){
        return tarefaRepository.findByDataHoraBetween(inicio, fim);
    }

    public Optional<Tarefa> deletarTarefa(Long id) {
        Optional<Tarefa> tarefa = tarefaRepository.findById(id);
        tarefa.ifPresent(t -> tarefaRepository.deleteById(id));
        return tarefa;
    }



    public Optional<Tarefa> alterarCompletamente(Long id, TarefaDTO dto) {
        return tarefaRepository.findById(id).map(exist -> {
            exist.setMateria(dto.materia());
            exist.setTopico(dto.topico());
            exist.setStatus(dto.status());
            exist.setDataHora(dto.dataHora());

            return tarefaRepository.save(exist);
        });
    }

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Allowlist: só estes campos podem ser alterados via PATCH.
    // "ID" propositalmente NÃO está aqui — nunca deve ser sobrescrevível.
    private static final Set<String> CAMPOS_PERMITIDOS =
            Set.of("materia", "topico", "dataHora", "status");

    /**
     * Atualiza parcialmente uma Tarefa a partir de um mapa de campos (JSON do PATCH).
     * Substitui o uso de ReflectionUtils por um switch explícito, com:
     *  - allowlist (bloqueia alteração de ID ou campos inexistentes)
     *  - conversão de tipo correta para enum Status e LocalDateTime
     *  - erros claros (400) em vez de exceções genéricas de reflexão
     */
    public Tarefa alterarParcialmente(Long id, Map<String, Object> campos) {
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tarefa com id " + id + " não encontrada!"));

        for (Map.Entry<String, Object> entry : campos.entrySet()) {
            String campo = entry.getKey();
            Object valor = entry.getValue();

            if (!CAMPOS_PERMITIDOS.contains(campo)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Campo '" + campo + "' não pode ser alterado via PATCH."
                );
            }

            switch (campo) {
                case "materia" -> tarefa.setMateria(exigirString(campo, valor));
                case "topico" -> tarefa.setTopico(exigirString(campo, valor));
                case "status" -> tarefa.setStatus(converterStatus(valor));
                case "dataHora" -> tarefa.setDataHora(converterDataHora(valor));
            }
        }

        return tarefaRepository.save(tarefa);
    }

    private String exigirString(String campo, Object valor) {
        if (!(valor instanceof String texto) || texto.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Campo '" + campo + "' deve ser um texto não vazio."
            );
        }
        return texto;
    }

    private Status converterStatus(Object valor) {
        if (!(valor instanceof String texto)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido.");
        }
        try {
            return Status.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status inválido: '" + texto + "'. Valores aceitos: PENDENTE, CONCLUIDO, CANCELADO."
            );
        }
    }

    private LocalDateTime converterDataHora(Object valor) {
        if (!(valor instanceof String texto)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataHora inválida.");
        }
        try {
            return LocalDateTime.parse(texto.trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de dataHora inválido. Use dd/MM/yyyy HH:mm:ss."
            );
        }
    }

}
