package io.github.mateussilvadev.horizondesk.service;

import io.github.mateussilvadev.horizondesk.dto.request.DepartmentCreateDTO;
import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import io.github.mateussilvadev.horizondesk.exception.EntityNotFoundException;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.repository.DepartmentRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    /**
     * Métodos de negócios.
     */
    @Transactional
    public Department create(DepartmentCreateDTO dto) {
        checkDepartmentExists(dto.name());
        Department department = Department.create(dto.name());
        return departmentRepository.save(department);
    }

    @Transactional(readOnly = true)
    public Department findByUuid(UUID uuid) {
        return departmentRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("department_service.error.department_not_found"));
    }

    /**
     * Métodos auxiliares.
     */
    private void checkDepartmentExists(String name) {
        if (departmentRepository.existsByName(name))
            throw new BusinessException(Code.DEPARTMENT_ALREADY_REGISTERED, "department_service.error.department_already_registered");
    }
}
