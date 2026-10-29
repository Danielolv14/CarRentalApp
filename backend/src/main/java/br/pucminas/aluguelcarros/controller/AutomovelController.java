package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.AutomovelRequest;
import br.pucminas.aluguelcarros.dto.AutomovelResponse;
import br.pucminas.aluguelcarros.service.AutomovelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Frota de automóveis. Qualquer usuário logado consulta; só o agente EMPRESA cadastra, edita e remove (SecurityConfig). */
@RestController
@RequestMapping("/automoveis")
public class AutomovelController {

    private final AutomovelService automoveis;

    public AutomovelController(AutomovelService automoveis) {
        this.automoveis = automoveis;
    }

    @GetMapping
    public List<AutomovelResponse> listar() {
        return automoveis.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AutomovelResponse cadastrar(@Valid @RequestBody AutomovelRequest dados) {
        return automoveis.cadastrar(dados);
    }

    @PutMapping("/{id}")
    public AutomovelResponse atualizar(@PathVariable Long id, @Valid @RequestBody AutomovelRequest dados) {
        return automoveis.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        automoveis.remover(id);
    }
}
