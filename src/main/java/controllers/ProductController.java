package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    for (Product product : productCache.getProducts(false)) {
      if (product.getId() == id) {
        // product found in cache
        return product;
      }
    }

    // Product not found in cache
    Product productFoundInDb = getProductFromDb(id);
    if (productFoundInDb != null) {
      // Add product found in database into cache
      productCache.addProduct(productFoundInDb);
    }
    return productFoundInDb;
  }

  public static Product getProductFromDb(int id) {
    // Build the SQL query for the DB
    String sql = "SELECT * FROM product where id=" + id;
    return getProduct(sql);
  }

  public static Product getProductBySku(String sku) {
    for (Product product : productCache.getProducts(false)) {
      if (product.getSku() == sku) {
        // product found in cache
        return product;
      }
    }

    // Product not found in cache
    Product productFoundInDb = getProductBySkuFromDb(sku);
    if (productFoundInDb != null) {
      // Add product found in database into cache
      productCache.addProduct(productFoundInDb);
    }
    return productFoundInDb;
  }

  public static Product getProductBySkuFromDb(String sku) {
    String sql = "SELECT * FROM product where sku='" + sku + "'";
    return getProduct(sql);
  }

  // method for checking connection
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
                rs.getString("product_name"), // change from "name" to "product_name"
                rs.getString("sku"),
                rs.getFloat("price"),
                rs.getString("description"),
                rs.getInt("stock"),
                rs.getTimestamp("created_at"));
      } else {
        // Changed to log
        Log.writeLog(ProductController.class.getName(), null,"No product found", 0);

      }
    } catch (SQLException ex) {
      // changed to log
      Log.writeLog(ProductController.class.getName(), null,"getProduct Message="+ ex.getMessage(), 1);
    }
    return null;
  }

  /**
   * Get all products using cache
   *
   * @return
   */
  public static ArrayList<Product> getProducts() {
    // TODO: Use caching layer. - Fixed
    return productCache.getProducts(false);
  }

  /**
   * Get all products in database
   *
   * @return
   */
  public static ArrayList<Product> getProductsFromDb() {
    checkConnection();
    String sql = "SELECT * FROM product";

    ResultSet rs = dbCon.query(sql);
    ArrayList<Product> products = new ArrayList<Product>();

    try {
      while (rs.next()) {
        Product product =
            new Product(
                rs.getInt("id"),
                rs.getString("product_name"),
                rs.getString("sku"),
                rs.getFloat("price"),
                rs.getString("description"),
                rs.getInt("stock"),
                rs.getTimestamp("created_at"));

        products.add(product);
      }
    } catch (SQLException ex) {
      Log.writeLog(ProductController.class.getName(), null,"getProducts Message="+ ex.getMessage(), 1);
    }

    return products;
  }

  public static String DateToTimeStamp(Date date) {
    Timestamp sq = new Timestamp(date.getTime());
    return sq.toString();
  }

  public static Product createProduct(Product product) {

    // Write in log that we've reach this step
    Log.writeLog(ProductController.class.getName(), product, "Actually creating a product in DB", 0);

    // Set creation time for product.
    //product.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    checkConnection();

    String sql = "INSERT INTO product(product_name, sku, price, description, stock, created_at) VALUES('"
            + product.getName()
            + "', '"
            + product.getSku()
            + "', "
            + product.getPrice()
            + ", '"
            + product.getDescription()
            + "', "
            + product.getStock()
            + ", '"
            + DateToTimeStamp(product.getCreatedTime())
            + "')";

    // Insert the product in the DB
    int productID = dbCon.insert(sql);

    if (productID != 0) {
      //Update the productid of the product before returning
      product.setId(productID);
      // add product to cache
      productCache.addProduct(product);
    } else{
      // Return null if product has not been inserted into database
      return null;
    }

    // Return product
    return product;
  }
}
