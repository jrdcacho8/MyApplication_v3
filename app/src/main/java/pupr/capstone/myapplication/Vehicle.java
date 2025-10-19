package pupr.capstone.myapplication;

import android.graphics.Bitmap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Year;

public class Vehicle {
    //Atributos para los datos del vehiculo.

    private String brand, color, license_plate, model;
    private int mileage, year;
    private Bitmap imageBitmap;


    //Constructores para los datos del vehiculo.

    public Vehicle(String brand, String model, String license_plate, int year, Bitmap imageBitmap, int mileage) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.license_plate = license_plate;
        this.imageBitmap = imageBitmap;
        this.mileage = mileage;
    }

    public Vehicle(String brand, String license_plate, String model) {
        this.brand = brand;
        this.model = model;
        this.license_plate = license_plate;
    }

    protected Vehicle(String marca, String modelo, String license_plate, int mileage) {
        this.brand = marca;
        this.model = modelo;
        this.license_plate = license_plate;
        this.mileage = mileage;

    }

    //Getters
    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getColor() {
        return color;
    }

    public String getLicense_plate() {
        return license_plate;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setLicense_plate(String plate) {
        this.license_plate = plate;
    }

    public int getYear() {
        return year;
    }

    public int getMileage() {
        return mileage;
    }

    /*public int getAverageMileagePerMonth() {
        int average_per_year, average_per_month;

     //   average_per_year = this.mileage / (Year.now().getValue() - this.year);

       // average_per_month = average_per_year / 12;

      //  return average_per_month;
    }*/

    //cargar aqui los objetos de mantenimientos con las tablas. Cada obejto aqui se cargara con su tipo de mantenimiento, fecfa de dua date (hacer calculo

}




