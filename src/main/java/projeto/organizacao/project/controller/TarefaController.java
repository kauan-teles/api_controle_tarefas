package projeto.organizacao.project.controller;

import lombok.Data;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.format.annotation.DateTimeFormat;
import projeto.organizacao.project.dto.TarefaDTO;
import projeto.organizacao.project.exceptions.ResourceNotFoundException;
import projeto.organizacao.project.model.Tarefa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.organizacao.project.service.TarefaService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tarefas")
public class TarefaController {
    private static final DateTimeFormatter FORMATER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    @Autowired
    TarefaService tarefaService;

    @PostMapping("/cadastro")
    public ResponseEntity<Tarefa> cadastrarTarefa(@RequestBody Tarefa tarefa) {
        Tarefa t = tarefaService.cadastrarTarefa(tarefa);
        return ResponseEntity.status(HttpStatus.CREATED).body(t);
    }

    @GetMapping
    public List<Tarefa> pegarTudo() {
        return tarefaService.pegarTarefas();
    }

    @GetMapping("/dataHora")
    public List<Tarefa> filtrarPorData(@RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") String inicio,
                                       @RequestParam @DateTimeFormat(pattern = "dd/MM/YYYY HH:mm:ss") String fim) {
        LocalDateTime i = LocalDateTime.parse(inicio.trim(), FORMATER);
        LocalDateTime f = LocalDateTime.parse(fim.trim(), FORMATER);
        return tarefaService.filtrarPorData(i, f);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Tarefa> deletarTarefa(@PathVariable Long id){
        return tarefaService.deletarTarefa(id).map(tarefaDeletada -> ResponseEntity.ok(tarefaDeletada)).orElse(ResponseEntity.notFound().build());

    }

    @PatchMapping("/{id}")
    public ResponseEntity<Tarefa> alterarParcialmente(@PathVariable Long id, @RequestBody Map<String, Object> fields){
        Tarefa tarefa = tarefaService.alterarParcialmente(id, fields);

        return ResponseEntity.ok(tarefa);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarefa> alterarCompletamente(@PathVariable Long id, @RequestBody TarefaDTO dto){
        return tarefaService.alterarCompletamente(id, dto).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }



}
