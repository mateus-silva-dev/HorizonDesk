package io.github.mateussilvadev.horizondesk.controller;

import io.github.docflowlib.docflow.annotations.ApiDocController;
import io.github.docflowlib.docflow.annotations.ApiDocGet;
import io.github.docflowlib.docflow.annotations.ApiDocPatch;
import io.github.docflowlib.docflow.annotations.ApiDocPost;
import io.github.mateussilvadev.horizondesk.dto.request.UserRequestDTOs;
import io.github.mateussilvadev.horizondesk.dto.response.PageResponse;
import io.github.mateussilvadev.horizondesk.dto.response.UserResponseDTOs;
import io.github.mateussilvadev.horizondesk.mapper.UserMapper;
import io.github.mateussilvadev.horizondesk.model.domain.User;
import io.github.mateussilvadev.horizondesk.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@ApiDocController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ApiDocPost
    public ResponseEntity<UserResponseDTOs.Response> createUser(@Valid @RequestBody UserRequestDTOs.Create dto) {
        User savedUser = userService.create(dto);
        UserResponseDTOs.Response response = UserMapper.toResponse(savedUser);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(response.uuid())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/{uuid}")
    @ApiDocGet
    public ResponseEntity<UserResponseDTOs.Response> getByUUID(@PathVariable UUID uuid) {
        User user = userService.findByUuid(uuid);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }


    @PatchMapping("/{uuid}")
    @ApiDocPatch
    public ResponseEntity<UserResponseDTOs.Response> updateUser(@PathVariable UUID uuid, @Valid @RequestBody UserRequestDTOs.Update dto) {
        User user = userService.updateUser(uuid, dto);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    @PatchMapping("/{uuid}/password")
    @ApiDocPatch
    public ResponseEntity<Void> updatePassword(@PathVariable UUID uuid, @Valid @RequestBody UserRequestDTOs.UpdatePassword dto) {
        userService.changePassword(uuid, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/exclusion-request")
    @ApiDocPatch
    public ResponseEntity<Void> exclusionRequest(@PathVariable UUID uuid) {
        userService.requestAccountExclusion(uuid);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{uuid}/role")
    @ApiDocPatch
    public ResponseEntity<Void> changeRole(@PathVariable UUID uuid, @Valid @RequestBody UserRequestDTOs.ChangeRole dto) {
        userService.changeRole(uuid, dto.role());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/department")
    @ApiDocPatch
    public ResponseEntity<Void> changeDepartment(@PathVariable UUID uuid, @Valid @RequestBody UserRequestDTOs.ChangeDepartment dto) {
        userService.changeDepartment(uuid, dto.departmentUuid());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/activate")
    @ApiDocPatch
    public ResponseEntity<Void> activateUser(@PathVariable UUID uuid) {
        userService.activate(uuid);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/deactivate")
    @ApiDocPatch
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID uuid) {
        userService.deactivate(uuid);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/technicians")
    @ApiDocGet
    public ResponseEntity<PageResponse<UserResponseDTOs.TechnicianOption>> getActiveTechnicians(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<User> page =
                userService.findAllActiveTechnicians(pageable);
        return ResponseEntity.ok(UserMapper.toTechnicianPageResponse(page));
    }

}
