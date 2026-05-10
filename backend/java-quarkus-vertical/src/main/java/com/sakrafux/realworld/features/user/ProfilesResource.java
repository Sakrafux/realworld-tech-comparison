package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.features.user.dto.ProfileResponse;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Optional;

@Path("/profiles")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class ProfilesResource {

    private final UserService userService;
    private final JsonWebToken jwt;

    @GET
    @Path("/{username}")
    public ProfileResponse getProfile(@PathParam("username") String username) {
        Optional<String> currentEmail = Optional.ofNullable(jwt.getName());
        return userService.getProfile(username, currentEmail);
    }

    @POST
    @Path("/{username}/follow")
    @Authenticated
    public ProfileResponse followUser(@PathParam("username") String username) {
        String currentEmail = jwt.getName();
        return userService.followUser(username, currentEmail);
    }

    @DELETE
    @Path("/{username}/follow")
    @Authenticated
    public ProfileResponse unfollowUser(@PathParam("username") String username) {
        String currentEmail = jwt.getName();
        return userService.unfollowUser(username, currentEmail);
    }
}
