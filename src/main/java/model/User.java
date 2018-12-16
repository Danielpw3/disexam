package model;

import java.util.Date;

public class User {

  public int id;
  public String firstname;
  public String lastname;
  public String email;
  private String password;
  private int phoneNumber;
  private int salt;
  private Date createdTime; // skal initialiseres i konstruktør
  private String token; // bruges til delete og update User _D

  // Used when creating user
  public User(int id, String firstname, String lastname, String password, String email, int phoneNumber, int salt) {
    this.id = id;
    this.firstname = firstname;
    this.lastname = lastname;
    this.password = password;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.salt = salt;
    this.createdTime = new Date(System.currentTimeMillis()/1000L);
  }

  // Used when reading from database
  public User(int id, String firstname, String lastname, String password, String email, int phoneNumber, int salt, Date createdTime) {
    this.id = id;
    this.firstname = firstname;
    this.lastname = lastname;
    this.password = password;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.salt = salt;
    this.createdTime = createdTime;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(int phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public int getSalt() {
    return salt;
  }

  public void setSalt(int salt) {
    this.salt = salt;
  }

  public Date getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
  }

  // bruges til delete og update user
  public String getToken() {return token;}

  public void setToken(String token) {
    this.token = token;
  }
}
