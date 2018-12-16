package model;

import java.util.Date;

public class Product {

  public int id;
  public String name;
  public String sku;
  public float price;
  private String description;
  private int stock;
  private Date createdTime; // skal laves om til Date

  // Used when creating product
  public Product(int id, String name, String sku, float price, String description, int stock) {
    this.id = id;
    this.name = name;
    this.sku = sku;
    this.price = price;
    this.description = description;
    this.stock = stock;
    this.createdTime = new Date(System.currentTimeMillis()/1000L);
  }

  // Used when reading from database
  public Product(int id, String name, String sku, float price, String description, int stock, Date createdTime) {
    this.id = id;
    this.name = name;
    this.sku = sku;
    this.price = price;
    this.description = description;
    this.stock = stock;
    this.createdTime = createdTime;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public float getPrice() {
    return price;
  }

  public void setPrice(float price) {
    this.price = price;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int stock) {
    this.stock = stock;
  }
}
