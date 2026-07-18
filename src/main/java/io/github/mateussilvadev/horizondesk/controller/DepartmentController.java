package io.github.mateussilvadev.horizondesk.controller;

import io.github.docflowlib.docflow.annotations.ApiDocController;
import io.github.docflowlib.docflow.annotations.ApiDocGet;
import io.github.docflowlib.docflow.annotations.ApiDocPost;
import io.github.mateussilvadev.horizondesk.dto.request.DepartmentCreateDTO;
import io.github.mateussilvadev.horizondesk.dto.response.DepartmentResponseDTO;
import io.github.mateussilvadev.horizondesk.mapper.DepartmentMapper;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.service.DepartmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/departments")
@ApiDocController
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @ApiDocPost
    public ResponseEntity<DepartmentResponseDTO> create(@RequestBody DepartmentCreateDTO dto) {
        Department savedDepartment = departmentService.create(dto);
        DepartmentResponseDTO response = DepartmentMapper.toResponse(savedDepartment);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(response.uuid())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/{uuid}")
    @ApiDocGet
    public ResponseEntity<DepartmentResponseDTO> getByUUID(@PathVariable UUID uuid) {
        Department department = departmentService.findByUuid(uuid);
        return ResponseEntity.ok(DepartmentMapper.toResponse(department));
    }


}
