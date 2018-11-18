package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import cache.ProductCache;
import model.Product;
import utils.Log;

public class ProductController {

  private static DatabaseController dbCon;
  private static ProductCache productCache;

  public ProductController() {
    dbCon = new DatabaseController();
    productCache = new ProductCache();
  }

  public static Product getProduct(int id) {
    // Build the SQL query for the DB
    String sql = "SELECT * FROM product where id=" + id;
    return getProduct(sql);
  }

  public static Product getProductBySku(String sku) {
    String sql = "SELECT * FROM product where sku='" + sku + "'";
    return getProduct(sql);
  }

  private static void checkConnection() {
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }
  }

  private static Product getProduct(String sql) {

    checkConnection();

    ResultSet rs = dbCon.query(sql);

    try {
      if (rs.next()) {
        return new Product(
                rs.getInt("id"),
                rs.getString("product_name"),
                rs.getString("sku"),
                rs.getFloat("price"),
                rs.getString("description"),
                rs.getInt("stock"));
      } else {
        // TODO Daniel change to log
        System.out.println("No product found");
      }
    } catch (SQLException ex) {
      // TODO Daniel change to log
      System.out.println(ex.getMessage());
    }
    return null;
  }

  /**
   * Get all products in database
   *
   * @return
   */
  public static ArrayList<Product> getProducts() {

    checkConnection();

    // TODO: Use caching layer.
    //return productCache.getProducts(true);

    String sql = "SELECT * FROM product";

    ResultSet rs = dbCon.query(sql);
    ArrayList<Product> products = new ArrayList<Product>();

    try {
      while (rs.next()) {
        Product product =
            new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("sku"),
                rs.getFloat("price"),
                rs.getString("description"),
                rs.getInt("stock"));

        products.add(product);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    return products;
  }

  public static Product createProduct(Product product) {

    // Write in log that we've reach this step
    Log.writeLog(ProductController.class.getName(), product, "Actually creating a product in DB", 0);

    // Set creation time for product.
    product.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    checkConnection();

    // Insert the product in the DB
    int productID = dbCon.insert(
        "INSERT INTO product(product_name, sku, price, description, stock, created_at) VALUES('"
            + product.getName()
            + "', '"
            + product.getSku()
            + "', '"
            + product.getPrice()
            + "', '"
            + product.getDescription()
            + "', "
            + product.getStock()
            + "', "
            + product.getCreatedTime()
            + ")");

    if (productID != 0) {
      //Update the productid of the product before returning
      product.setId(productID);
    } else{
      // Return null if product has not been inserted into database
      return null;
    }

    // Return product
    return product;
  }
}
