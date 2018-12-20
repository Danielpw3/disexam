package model;

public class Address {

  private int id;
  private String customer;
  private String streetAddress;
  private String city;
  private String zipCode;

  public Address(){}

  public Address(int id, String customer, String streetAddress, String city, String zipCode){
    this.id = id;
    this.customer = customer;
    this.streetAddress = streetAddress;
    this.city = city;
    this.zipCode = zipCode;
  }

  public Address(String customer, String streetAddress, String city, String zipCode){
    this.customer = customer;
    this.streetAddress = streetAddress;
    this.city = city;
    this.zipCode = zipCode;
  }

  public String getCustomer() {
    return customer;
  }

  public void setCustomer(String customer) {
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

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
