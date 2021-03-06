package com.cbsexam;

import com.google.gson.Gson;
import controllers.OrderController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.Order;
import utils.Encryption;

@Path("order")
public class OrderEndpoints {

  private static OrderController orderController;

  public OrderEndpoints () {
    orderController = new OrderController();
  }

  /**
   * @param idOrder
   * @return Responses
   */
  @GET
  @Path("/{idOrder}")
  public Response getOrder(@PathParam("idOrder") int idOrder) {

    // Call our controller-layer in order to get the order from the DB
    Order order = OrderController.getOrder(idOrder);

    // TODO: Add Encryption to JSON - FIXED
    // We convert the java object to json with GSON library imported in Maven
    String json = new Gson().toJson(order);
    json = Encryption.encryptDecryptXOR(json); //add encryption to JSON -D

    // Return a response with status 200 and JSON as type
    return Response.status(order != null ? Response.Status.OK : Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getOrders() {

    // Call our controller-layer in order to get the order from the DB
    ArrayList<Order> orders = OrderController.getOrders();

    // TODO: Add Encryption to JSON - FIXED
    // We convert the java object to json with GSON library imported in Maven
    String json = new Gson().toJson(orders);
    json = Encryption.encryptDecryptXOR(json); //add encryption to JSON -D

    // Return a response with status 200 and JSON as type
    return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
  }

  @POST
  @Path("/createOrder")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createOrder(String body) {

    // Read the json from body and transfer it to a order class
    Order newOrder = new Gson().fromJson(body, Order.class);

    // Use the controller to add the user
    Order createdOrder = orderController.createOrder(newOrder);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createdOrder);

    // Return the data to the user
    if (createdOrder != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {

      // Return a response with status 400 and a message in text
      return Response.status(Response.Status.BAD_REQUEST).entity("Could not create order").build();
    }
  }
}