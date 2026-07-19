package io.github.mateussilvadev.horizondesk.mapper;

import io.github.mateussilvadev.horizondesk.dto.request.DepartmentRequestDTOs;
import io.github.mateussilvadev.horizondesk.dto.response.DepartmentResponseDTOs;
import io.github.mateussilvadev.horizondesk.dto.response.PageResponse;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;

public class DepartmentMapper {

    private static final ModelMapper MAPPER = new ModelMapper();
    static {
        MAPPER.createTypeMap(Department.class, DepartmentResponseDTOs.DepartmentResponse.class)
                .setConverter(context -> {
                    Department source = context.getSource();
                    if (source == null) return null;

                    return new DepartmentResponseDTOs.DepartmentResponse(
                            source.getUuid(),
                            source.getName(),
                            source.isActive()
                    );
                });
    }

    private DepartmentMapper() { }

    public static Department toDepartment(DepartmentRequestDTOs.DepartmentCreate dto) {
        return Department.create(dto.name());
    }

    public static DepartmentResponseDTOs.DepartmentResponse toResponse(Department department) {
        if (department == null) return null;
        return MAPPER.map(department, DepartmentResponseDTOs.DepartmentResponse.class);
    }

    public static DepartmentResponseDTOs.DepartmentOptions toOption(Department department) {
        return new DepartmentResponseDTOs.DepartmentOptions(department.getUuid(), department.getName());
    }

    public static PageResponse<DepartmentResponseDTOs.DepartmentResponse> toPageResponse(Page<Department> page) {
        Page<DepartmentResponseDTOs.DepartmentResponse> mappedPage = page.map(DepartmentMapper::toResponse);
        return new PageResponse<>(
                mappedPage.getContent(),
                mappedPage.getNumber(),
                mappedPage.getSize(),
                mappedPage.getTotalElements(),
                mappedPage.getTotalPages(),
                mappedPage.isLast()
        );
    }
}
