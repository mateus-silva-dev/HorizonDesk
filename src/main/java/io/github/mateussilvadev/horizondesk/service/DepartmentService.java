package io.github.mateussilvadev.horizondesk.service;

import io.github.mateussilvadev.horizondesk.dto.request.DepartmentRequestDTOs;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import io.github.mateussilvadev.horizondesk.exception.EntityNotFoundException;
import io.github.mateussilvadev.horizondesk.mapper.DepartmentMapper;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.repository.DepartmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Department create(DepartmentRequestDTOs.Create dto) {
        checkDepartmentExists(dto.name());
        Department department = DepartmentMapper.toDepartment(dto);
        return repository.save(department);
    }

    @Transactional(readOnly = true)
    public Department findByUuid(UUID uuid) {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("department_service.error.department_not_found"));
    }

    @Transactional
    public Department update(UUID uuid, String newName) {
        Department department = findByUuid(uuid);
        checkDepartmentExists(newName);
        department.changeDepartmentName(newName);
        return department;
    }

    @Transactional
    public void activate(UUID uuid) {
        Department department = findByUuid(uuid);
        department.activate();
    }

    @Transactional
    public void deactivate(UUID uuid) {
        Department department = findByUuid(uuid);
        department.deactivate();
    }

    @Transactional(readOnly = true)
    public List<Department> findAllActiveOptions() {
        return repository.findAllByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Page<Department> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }


    private void checkDepartmentExists(String name) {
        if (repository.existsByName(name))
            throw new BusinessException(Code.DEPARTMENT_ALREADY_REGISTERED, "department_service.error.department_already_registered");
    }
}
