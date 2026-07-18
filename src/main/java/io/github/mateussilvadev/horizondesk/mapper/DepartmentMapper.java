package io.github.mateussilvadev.horizondesk.mapper;

import io.github.mateussilvadev.horizondesk.dto.request.DepartmentRequestDTOs;
import io.github.mateussilvadev.horizondesk.dto.response.DepartmentResponseDTOs;
import io.github.mateussilvadev.horizondesk.dto.response.PageResponse;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;

public class DepartmentMapper {

    private static final ModelMapper MAPPER = new ModelMapper();
    static {
        MAPPER.createTypeMap(Department.class, DepartmentResponseDTOs.Response.class)
                .setConverter(context -> {
                    Department source = context.getSource();
                    if (source == null) return null;

                    return new DepartmentResponseDTOs.Response(
                            source.getUuid(),
                            source.getName(),
                            source.isActive()
                    );
                });
    }

    private DepartmentMapper() { }

    public static Department toDepartment(DepartmentRequestDTOs.Create dto) {
        return Department.create(dto.name());
    }

    public static DepartmentResponseDTOs.Response toResponse(Department department) {
        if (department == null) return null;
        return MAPPER.map(department, DepartmentResponseDTOs.Response.class);
    }

    public static DepartmentResponseDTOs.Options toOption(Department department) {
        return new DepartmentResponseDTOs.Options(department.getUuid(), department.getName());
    }

    public static PageResponse<DepartmentResponseDTOs.Response> toPageResponse(Page<Department> page) {
        Page<DepartmentResponseDTOs.Response> mappedPage = page.map(DepartmentMapper::toResponse);
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
