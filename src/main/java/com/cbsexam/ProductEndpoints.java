package com.cbsexam;

import com.google.gson.Gson;
import controllers.ProductController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.Product;
import utils.Encryption;

@Path("product")
public class ProductEndpoints {

  private static ProductController productController;

  public ProductEndpoints () {
    productController = new ProductController();
  }


  /**
   * @param idProduct
   * @return Responses
   */
  @GET
  @Path("/{idProduct}")
  public Response getProduct(@PathParam("idProduct") int idProduct) {

    // Call our controller-layer in order to get the order from the DB
    Product product = productController.getProduct(idProduct);

    // TODO: Add Encryption to JSON - FIXED
    // We convert the java object to json with GSON library imported in Maven
    String json = product != null ? new Gson().toJson(product) : "Product with id="+idProduct+" was not found";
    json = Encryption.encryptDecryptXOR(json); //add encryption to JSON -D

    // Return a response with status 200 and JSON as type
    return Response.status(product != null ? Response.Status.OK : Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getProducts() {

    // Call our controller-layer in order to get the order from the DB
    ArrayList<Product> products = productController.getProducts();

    // TODO: Add Encryption to JSON - FIXED
    // We convert the java object to json with GSON library imported in Maven
    String json = new Gson().toJson(products);
    json = Encryption.encryptDecryptXOR(json); //add encryption to JSON -D

    // Return a response with status 200 and JSON as type
    return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
  }

  @POST
  @Path("/createProduct")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createProduct(String body) {

    // Read the json from body and transfer it to a product class
    Product newProduct = new Gson().fromJson(body, Product.class);

    // Use the controller to add the user
    Product createdProduct = productController.createProduct(newProduct);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createdProduct);

    // Return the data to the user
    if (createdProduct != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(Response.Status.BAD_REQUEST).entity("Could not create product").build();
    }
  }
}
