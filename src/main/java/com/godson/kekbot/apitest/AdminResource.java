package com.godson.kekbot.apitest;

import com.godson.kekbot.ExitCode;
import com.godson.kekbot.KekBot;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("admin")
public class AdminResource {

    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public Response kill(@HeaderParam("Authorization") String auth) {
        if (auth == null || !auth.equals("pineapple")) {
            return Response.status(Response.Status.FORBIDDEN).entity("reee you don't have a pineapple u fag").build();
        } else {
            KekBot.shutdown("fuk u");
            KekBot.shutdownListener.setExitCode(ExitCode.STOP);
            return Response.ok().entity("time for me to die").build();
        }
    }


}
