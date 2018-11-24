package model;

public class Address {

  private int id;
  private User customer;
  private String streetAddress;
  private String city;
  private int zipCode;

  public Address(){}

  public Address(int id, User customer, String streetAddress, String city, int zipCode){
    this.id = id;
    this.customer = customer;
    this.streetAddress = streetAddress;
    this.city = city;
    this.zipCode = zipCode;
  }

  public Address(User customer, String streetAddress, String city, int zipCode){
    this.customer = customer;
    this.streetAddress = streetAddress;
    this.city = city;
    this.zipCode = zipCode;
  }

  public User getCustomer() {
    return customer;
  }

  public void setCustomer(User customer) {
    this.customer = customer;
  }

  public String getStreetAddress() {
    return streetAddress;
  }

  public void setStreetAddress(String streetAddress) {
    this.streetAddress = streetAddress;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public int getZipCode() {
    return zipCode;
  }

  public void setZipCode(int zipCode) {
    this.zipCode = zipCode;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
