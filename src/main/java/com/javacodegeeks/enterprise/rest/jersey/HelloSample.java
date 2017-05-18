package com.javacodegeeks.enterprise.rest.jersey;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/HelloWorldService")
public class HelloSample {

	@GET
	@Path("/{param}")
	public Response getMsg(@PathParam("param") String msg) {
	//public Response getMsg() {

		String output = "Jersey say Hello " + msg;
		return Response.status(200).entity(output).build();

	}

}
