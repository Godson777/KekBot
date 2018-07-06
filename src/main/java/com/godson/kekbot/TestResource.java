package com.godson.kekbot;

import com.godson.kekbot.profile.Profile;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/* Root resource (exposed at "myresource" path) */
@Path("test")
public class TestResource {

    @GET
    @Produces({ "image/png", MediaType.TEXT_PLAIN, })
    public Object getIt(@QueryParam("id") long id) {
        try {
            return Profile.getProfile(KekBot.jda.getUserById(id)).drawCard();
        } catch (Exception e) {
            return "you fucked up";
        }
    }
}
