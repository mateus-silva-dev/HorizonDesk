package io.github.mateussilvadev.horizondesk.controller;

import io.github.docflowlib.docflow.annotations.ApiDocController;
import io.github.docflowlib.docflow.annotations.ApiDocGet;
import io.github.docflowlib.docflow.annotations.ApiDocPost;
import io.github.mateussilvadev.horizondesk.dto.request.DepartmentRequestDTOs;
import io.github.mateussilvadev.horizondesk.dto.response.DepartmentResponseDTOs;
import io.github.mateussilvadev.horizondesk.dto.response.PageResponse;
import io.github.mateussilvadev.horizondesk.mapper.DepartmentMapper;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.service.DepartmentService;
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
@ApiDocController
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @PostMapping
    @ApiDocPost
    public ResponseEntity<DepartmentResponseDTOs.Response> create(@RequestBody DepartmentRequestDTOs.Create dto) {
        Department savedDepartment = service.create(dto);
        DepartmentResponseDTOs.Response response = DepartmentMapper.toResponse(savedDepartment);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(response.uuid())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/{uuid}")
    @ApiDocGet
    public ResponseEntity<DepartmentResponseDTOs.Response> getByUUID(@PathVariable UUID uuid) {
        Department department = service.findByUuid(uuid);
        return ResponseEntity.ok(DepartmentMapper.toResponse(department));
    }

    @GetMapping
    @ApiDocGet
    public ResponseEntity<PageResponse<DepartmentResponseDTOs.Response>> getAll(
            @PageableDefault(size = 20, sort = "name") Pageable page) {
        Page<Department> pagedDepartments = service.findAll(page);
        return ResponseEntity.ok(DepartmentMapper.toPageResponse(pagedDepartments));
    }

    @GetMapping("/options")
    @ApiDocGet
    public ResponseEntity<List<DepartmentResponseDTOs.Options>> getActiveOptions() {
        List<Department> departments = service.findAllActiveOptions();
        return ResponseEntity.ok(
                departments.stream()
                .map(DepartmentMapper::toOption)
                .toList());
    }


}
