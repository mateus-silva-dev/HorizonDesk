package io.github.mateussilvadev.horizondesk.mapper;

import io.github.mateussilvadev.horizondesk.dto.request.UserRequestDTOs;
import io.github.mateussilvadev.horizondesk.dto.response.PageResponse;
import io.github.mateussilvadev.horizondesk.dto.response.UserResponseDTOs;
import io.github.mateussilvadev.horizondesk.model.domain.Department;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;

import java.util.Locale;

public class UserMapper {

    private static final ModelMapper MAPPER = new ModelMapper();
    static {
        MAPPER.createTypeMap(User.class, UserResponseDTOs.Response.class)
                .setConverter(context -> {
                    User source = context.getSource();
                    if (source == null) return null;

                    String departmentName = (source.getDepartment() != null)
                            ? source.getDepartment().getName()
                            : null;

                    return new UserResponseDTOs.Response(
                            source.getUuid(),
                            source.getName(),
                            source.getEmail().toLowerCase(Locale.ROOT),
                            source.getRole().getValue(),
                            departmentName,
                            source.getStatus().name()
                    );
                });
    }

    private UserMapper() { }

    public static User toUser(UserRequestDTOs.Create dto, Department department, String encryptedPassword) {
        return User.create(
                dto.name(),
                dto.email(),
                encryptedPassword,
                department
        );
    }

    public static UserResponseDTOs.Response toResponse(User user) {
        if (user == null) return null;
        return MAPPER.map(user, UserResponseDTOs.Response.class);
    }

    public static UserResponseDTOs.TechnicianOption toTechnicianOption(User user) {
        return new UserResponseDTOs.TechnicianOption(user.getUuid(), user.getName(), user.getStatus());
    }

    public static PageResponse<UserResponseDTOs.TechnicianOption> toTechnicianPageResponse(Page<User> page) {
        Page<UserResponseDTOs.TechnicianOption> mappedPage = page.map(UserMapper::toTechnicianOption);
        return new PageResponse<>(
                mappedPage.getContent(),
                mappedPage.getNumber(),
                mappedPage.getSize(),
                mappedPage.getTotalElements(),
                mappedPage.getTotalPages(),
                mappedPage.isLast());
    }

}
