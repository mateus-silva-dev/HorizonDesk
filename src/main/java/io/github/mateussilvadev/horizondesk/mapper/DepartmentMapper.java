package io.github.mateussilvadev.horizondesk.mapper;

import io.github.mateussilvadev.horizondesk.dto.request.DepartmentCreateDTO;
import io.github.mateussilvadev.horizondesk.dto.response.DepartmentResponseDTO;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import org.modelmapper.ModelMapper;

public class DepartmentMapper {

    private static final ModelMapper MAPPER = new ModelMapper();

    static {
        MAPPER.createTypeMap(Department.class, DepartmentResponseDTO.class)
                .setConverter(context -> {
                    Department source = context.getSource();
                    if (source == null) return null;

                    return new DepartmentResponseDTO(
                            source.getUuid(),
                            source.getName(),
                            source.isActive()
                    );
                });
    }

    public static Department toEntity(DepartmentCreateDTO dto) {
        return MAPPER.map(dto, Department.class);
    }

    public static DepartmentResponseDTO toResponse(Department department) {
        return MAPPER.map(department, DepartmentResponseDTO.class);
    }
}
