package edu.northeastern.stutrade.Models;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Product implements Serializable {
    private String productPrice;

    public Product(String name, String description, String price, String imageUrl) {
        this.productName = name;
        this.productDescription=description;
        this.productPrice = price;
        this.imageUrl = imageUrl;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    private String productName;
    private String datePosted;

    private String location;
    private String productDescription;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String userId;

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    private String sellerName;
    private List<String> imageUrls;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private String imageUrl;


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


    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
