package edu.northeastern.stutrade.Models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Product {
    private String productPrice;
    private String productName;
    private String datePosted;

    private String location;
    private String productDescription;

    public Product(){}
    public Product(String productPrice, String productName, String datePosted, String location, String productDescription) {
        this.productPrice = productPrice;
        this.productName = productName;
        this.datePosted = datePosted;
        this.location = location;
        this.productDescription = productDescription;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductName() {
        return productName;
    }

    public void setName(String productName) {
        this.productName = productName;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(String datePosted) {
        this.datePosted = datePosted;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public Double getPriceAsDouble() {
        try {
            return Double.parseDouble(productPrice);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public Date getDatePostedAsDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            return sdf.parse(datePosted);
        } catch (ParseException e) {
            return new Date();
        }
    }
}
