package controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


import cache.OrderCache;
import model.Address;
import model.LineItem;
import model.Order;
import model.User;
import utils.Log;

public class OrderController {

  private static DatabaseController dbCon;
  private static OrderCache orderCache;

  public OrderController() {
    dbCon = new DatabaseController();
    orderCache = new OrderCache();
  }

  public static OrderCache getOrderCache() {
    if (orderCache == null) {
      orderCache = new OrderCache();
    }
    return orderCache;
  }
  public static Order getOrder (int id) {
    for (Order order : getOrderCache().getOrders(false)) {
      if (order.getId() == id) {
        // order found in cache
        return order;
      }
    }

    // Order not found in cache
    Order orderFoundInDb = getOrderFromDb(id);
    if (orderFoundInDb != null) {
      // Add order found in database into cache
      getOrderCache().addOrder(orderFoundInDb);
    }
    return orderFoundInDb;
  }


  public static Order getOrderFromDb (int id) {

    // check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL string to query
    String sql = "SELECT * FROM orders where id=" + id;

    // Do the query in the database and create an empty object for the results
    ResultSet rs = dbCon.query(sql);
    Order order = null;

    try {
      if (rs.next()) {


        // Perhaps we could optimize things a bit here and get rid of nested queries.
        User user = UserController.getUser(rs.getInt("user_id"));
        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));
        Address billingAddress = AddressController.getAddress(rs.getInt("billing_address_id"));
        Address shippingAddress = AddressController.getAddress(rs.getInt("shipping_address_id"));

        // Create an object instance of order from the database dataa
        order =
            new Order(
                rs.getInt("id"),
                user,
                lineItems,
                billingAddress,
                shippingAddress,
                rs.getFloat("order_total"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"));

        // Returns the build order
        return order;
      } else {
        Log.writeLog(OrderController.class.getName(), null,"No order found", 0);
      }
    } catch (SQLException ex) {
      Log.writeLog(OrderController.class.getName(), null,"getOrder Message="+ ex.getMessage(), 1);
    }

    // Returns null
    return order;
  }

  public static ArrayList<Order> getOrders() {
    return getOrderCache().getOrders(false);
  }

  /**
   * Get all orders in database
   *
   * @return
   */
  public static ArrayList<Order> getOrdersFromdb() {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    String sql = "SELECT * FROM orders"; // change from "order" to "orders"

    ResultSet rs = dbCon.query(sql);
    ArrayList<Order> orders = new ArrayList<Order>();

    try {
      while(rs.next()) {

        // Perhaps we could optimize things a bit here and get rid of nested queries.
        User user = UserController.getUser(rs.getInt("user_id"));
        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));
        Address billingAddress = AddressController.getAddress(rs.getInt("billing_address_id"));
        Address shippingAddress = AddressController.getAddress(rs.getInt("shipping_address_id"));

        // Create an order from the database data
        Order order =
            new Order(
                rs.getInt("id"),
                user,
                lineItems,
                billingAddress,
                shippingAddress,
                rs.getFloat("order_total"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"));

        // Add order to our list
        orders.add(order);

      }
    } catch (SQLException ex) {
      Log.writeLog(OrderController.class.getName(), null,"getOrders Message="+ ex.getMessage(), 1);
    }

    // return the orders
    return orders;
  }

  public static Order createOrder(Order order) {

    // Write in log that we've reach this step
    Log.writeLog(OrderController.class.getName(), order, "Actually creating a order in DB", 0);

    //Laver en forbindelse til databasen
    Connection conection = DatabaseController.getConnection();

    try {

      // sets autocommit to false, så we can decide when we commit
      conection.setAutoCommit(false);
      conection.setAutoCommit(false);

      // Set creation and updated time for order.
      order.setCreatedAt(System.currentTimeMillis() / 1000L);
      order.setUpdatedAt(System.currentTimeMillis() / 1000L);

      // Check for DB Connection
      if (dbCon == null) {
        dbCon = new DatabaseController();
      }

      // Save addresses to database and save them back to initial order instance
      order.setBillingAddress(AddressController.createAddress(order.getBillingAddress()));
      order.setShippingAddress(AddressController.createAddress(order.getShippingAddress()));

      // Save the user to the database and save them back to initial order instance
      order.setCustomer(UserController.createUser(order.getCustomer()));

      // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts. - Fixed

      // Insert the product in the DB
      int orderID = dbCon.insert(
              "INSERT INTO orders(user_id, billing_address_id, shipping_address_id, order_total, created_at, updated_at) VALUES("
                      + order.getCustomer().getId()
                      + ", "
                      + order.getBillingAddress().getId()
                      + ", "
                      + order.getShippingAddress().getId()
                      + ", "
                      + order.calculateOrderTotal()
                      + ", "
                      + order.getCreatedAt()
                      + ", "
                      + order.getUpdatedAt()
                      + ")");

      if (orderID != 0) {
        //Update the productid of the product before returning
        order.setId(orderID);
        getOrderCache().addOrder(order);
      }

      // Create an empty list in order to go trough items and then save them back with ID
      ArrayList<LineItem> items = new ArrayList<LineItem>();

      // Save line items to database
      for(LineItem item : order.getLineItems()){
        item = LineItemController.createLineItem(item, order.getId());
        items.add(item);

        // Insert the line item in the DB
        dbCon.insert(
                "INSERT INTO line_item(id, product_id, order_id, price, quantity) VALUES("
                        + item.getId()
                        + ", "
                        + item.getProduct().getId()
                        + ", "
                        + order.getId()
                        + ", "
                        + item.getProduct().getPrice()
                        + ", "
                        + item.getQuantity()
                        + ")");
      }
      order.setLineItems(items);

      // we now have all information and we can commit
      conection.commit();

    } catch (SQLException e) {

      //Writes the error
      Log.writeLog(OrderController.class.getName(), null,"createOrder Message="+ e.getMessage(), 1);
      if (conection != null) {
        try {
          System.err.print("Transaction is being rolled back");
          // remove order from cache because exception happend
          getOrderCache().removeOrder(order);
          conection.rollback();
        } catch (SQLException excep) {
          Log.writeLog(OrderController.class.getName(), null,"createOrder (rollback) Message="+ excep.getMessage(), 1);
        }
      }
    } finally{
      try {
        conection.setAutoCommit(true);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    // Return order
    return order;
  }
}