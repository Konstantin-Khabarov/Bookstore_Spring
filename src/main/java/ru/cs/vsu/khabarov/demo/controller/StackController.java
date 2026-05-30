package ru.cs.vsu.khabarov.demo.controller;

import ru.cs.vsu.khabarov.demo.dto.StackDto;
import ru.cs.vsu.khabarov.demo.model.Stack;
import ru.cs.vsu.khabarov.demo.repository.StackRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stacks")
@Tag(name = "Stacks", description = "Управление стеллажами и поиск по расположению")
public class StackController {

    @Autowired
    private StackRepository stackRepository;

    @PostMapping
    @Operation(summary = "Создать стеллаж")
    public ResponseEntity<Stack> create(@Valid @RequestBody StackDto dto) {
        Stack stack = new Stack();
        stack.setX(dto.getX());
        stack.setY(dto.getY());
        stack.setShelfNumber(dto.getShelfNumber());
        stack.setDescription(dto.getDescription());
        return new ResponseEntity<>(stackRepository.save(stack), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Получить все стеллажи")
    public List<Stack> getAll() {
        return stackRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить стеллаж по ID")
    public ResponseEntity<Stack> getById(@PathVariable Long id) {
        Optional<Stack> stack = stackRepository.findById(id);
        return stack.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/near")
    @Operation(summary = "Поиск стеллажей рядом с координатами (x, y)")
    public List<Stack> findNear(@RequestParam double x, @RequestParam double y,
                                @RequestParam(defaultValue = "100") double maxDistance) {
        return stackRepository.findStacksNear(x, y, maxDistance);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить стеллаж")
    public ResponseEntity<Stack> update(@PathVariable Long id, @Valid @RequestBody StackDto dto) {
        Optional<Stack> existing = stackRepository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        Stack stack = existing.get();
        stack.setX(dto.getX());
        stack.setY(dto.getY());
        stack.setShelfNumber(dto.getShelfNumber());
        stack.setDescription(dto.getDescription());
        stack.updateTimestamp();
        return ResponseEntity.ok(stackRepository.save(stack));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить стеллаж")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!stackRepository.existsById(id)) return ResponseEntity.notFound().build();
        stackRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
