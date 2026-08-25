package projeto.organizacao.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import projeto.organizacao.project.model.Status;

import java.time.LocalDateTime;

public record TarefaDTO(
    @NotBlank(message = "Matéria não pode ser vazio")
    String materia,
    @NotBlank(message = "Tópico não pode ser vazio")
    String topico,

    @NotNull(message = "A data de renovação é obrigatoria")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Future(message = "A data da tarefa deve ser futura")
    LocalDateTime dataHora,

    @NotNull(message = "É obrigatorio passar o status")
    Status status
){}
