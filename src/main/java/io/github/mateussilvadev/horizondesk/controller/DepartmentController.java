package io.github.mateussilvadev.horizondesk.controller;

import io.github.mateussilvadev.horizondesk.dto.request.DepartmentRequestDTOs;
import io.github.mateussilvadev.horizondesk.dto.response.DepartmentResponseDTOs;
import io.github.mateussilvadev.horizondesk.dto.response.PageResponse;
import io.github.mateussilvadev.horizondesk.mapper.DepartmentMapper;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DepartmentResponseDTOs.DepartmentResponse> create(@RequestBody @Valid DepartmentRequestDTOs.DepartmentCreate dto) {
        Department savedDepartment = service.create(dto);
        DepartmentResponseDTOs.DepartmentResponse departmentResponse = DepartmentMapper.toResponse(savedDepartment);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(departmentResponse.uuid())
                .toUri();
        return ResponseEntity.created(uri).body(departmentResponse);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<DepartmentResponseDTOs.DepartmentResponse> getByUUID(@PathVariable UUID uuid) {
        Department department = service.findByUuid(uuid);
        return ResponseEntity.ok(DepartmentMapper.toResponse(department));
    }


    @PatchMapping("/{uuid}")
    public ResponseEntity<DepartmentResponseDTOs.DepartmentResponse> update(@PathVariable UUID uuid, @Valid @RequestBody DepartmentRequestDTOs.DepartmentUpdate dto) {
        Department department = service.update(uuid, dto.name());
        return ResponseEntity.ok(DepartmentMapper.toResponse(department));
    }

    @PatchMapping("/{uuid}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID uuid) {
        service.activate(uuid);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID uuid) {
        service.deactivate(uuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PageResponse<DepartmentResponseDTOs.DepartmentResponse>> getAll(
            @PageableDefault(size = 20, sort = "name") Pageable page) {
        Page<Department> pagedDepartments = service.findAll(page);
        return ResponseEntity.ok(DepartmentMapper.toPageResponse(pagedDepartments));
    }

    @GetMapping("/options")
    public ResponseEntity<List<DepartmentResponseDTOs.DepartmentOptions>> getActiveOptions() {
        List<Department> departments = service.findAllActiveOptions();
        return ResponseEntity.ok(
                departments.stream()
                .map(DepartmentMapper::toOption)
                .toList());
    }


}
