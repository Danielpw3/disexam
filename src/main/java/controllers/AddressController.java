package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import model.Address;
import model.User;
import utils.Log;

public class AddressController {

  private static DatabaseController dbCon;

  public AddressController() {
    dbCon = new DatabaseController();
  }

  public static Address getAddress(int id) {

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Our SQL string
    String sql = "SELECT * FROM address where id=" + id;

    // Do the query and set the initial value to null
    ResultSet rs = dbCon.query(sql);
    Address address = null;



    try {
      // Get the first row and build an address object
      if (rs.next()) {

        User user = UserController.getUser(rs.getInt("user_id"));

        address =
            new Address(
                rs.getInt("id"),
                user,
                rs.getString("street_address"),
                rs.getString("city"),
                rs.getInt("zipcode")
                );

        // Return our newly added object
        return address;
      } else {
        System.out.println("No address found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Returns null if we can't find anything.
    return address;
  }

  public static Address createAddress(Address address) {

    if (address.getId() != 0) {
      // address already created
      return address;
    }

    // Write in log that we've reach this step
    Log.writeLog(ProductController.class.getName(), address, "Actually creating a line item in DB", 0);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the product in the DB
    int addressID = dbCon.insert(
        "INSERT INTO address(user_id, city, zipcode, street_address) VALUES("
            + address.getCustomer()
            + ", '"
            + address.getCity()
            + "', "
            + address.getZipCode()
            + ", '"
            + address.getStreetAddress()
            + "')");

    if (addressID != 0) {
      //Update the productid of the product before returning
      address.setId(addressID);
    } else{
      // Return null if product has not been inserted into database
      return null;
    }

    // Return product, will be null at this point
    return address;
  }
  
}
