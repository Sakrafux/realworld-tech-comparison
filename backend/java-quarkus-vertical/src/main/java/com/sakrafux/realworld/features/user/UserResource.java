package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.features.user.dto.UpdateUserRequest;
import com.sakrafux.realworld.features.user.dto.UserResponse;
import io.quarkus.security.Authenticated;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@RequiredArgsConstructor
public class UserResource {

    private final UserService userService;
    private final JsonWebToken jwt;

    @GET
    public UserResponse getCurrentUser() {
        String email = jwt.getName();
        return userService.getCurrentUser(email);
    }

    @PUT
    public UserResponse updateUser(@Valid UpdateUserRequest request) {
        String currentEmail = jwt.getName();
        return userService.updateUser(currentEmail, request);
    }
}
