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

    Maintenance services[] = new Maintenance[15];
    private Bitmap imageBitmap;


    //Constructores para los datos del vehiculo.

    public Vehicle(String brand, String license_plate, Bitmap imageBitmap) {
        this.brand = brand;
        //this.model = modelo;
        //this.year = year;
        // this.color = color;
        this.license_plate = license_plate;
        //this.mileage = mileage;
        this.imageBitmap = imageBitmap;
    }

    public Vehicle(String brand, String license_plate, String model) {
        this.brand = brand;
        this.model = model;
        this.license_plate = license_plate;
    }

    protected Vehicle(String marca, String modelo, int year, String color, String license_plate, int milleage) {
        this.brand = marca;
        this.model = modelo;
        this.year = year;
        this.color = color;
        this.license_plate = license_plate;
        this.mileage = mileage;
        this.imageBitmap = imageBitmap;

        //Regular Maintenances
/*
        services[0] = new Maintenance("Aceite de Motor");//Preguntar que tipo de aceite convencional o síntetico
        services[1] = new Maintenance("Líquido refrigerante/anticongelante");
        services[2] = new Maintenance("Presión de aire y grosor de las gomas");
        services[3] = new Maintenance("Líquido de Limpiaparabrisas (Wiper)");
        services[4] = new Maintenance("Escobillas de Limpiaparabrisas (Wiper)");

        //Periodical Maintenances
        services[5] = new Maintenance("Filtro de Aire (Motor)");
        services[6] = new Maintenance("Líquido de Transmisión");
        services[7] = new Maintenance("Líquido de dirección hidráulica (Power Steering)");
        services[8] = new Maintenance("Correas / Mangas");
        services[9] = new Maintenance("Batería");
        services[10] = new Maintenance("Rotación de Gomas");

        //Annual Maintenances
        services[11] = new Maintenance("Frenos");
        services[12] = new Maintenance("Filtro de Aire (Cabina)");
        services[13] = new Maintenance("Bujías (Spark Plugs)");//Preguntar que tipo de bujia (cobre o iridio)
        services[14] = new Maintenance("Alineación de Gomas");

        //The object Vehicle will initialize with all his maintenances: regular, periodic and annuals
        /*
        A. Regular Checks (Monthly or Every Few Thousand Miles):
                1) Engine Oil
                2) Coolant/Antifreeze
                3) Tire Pressure and Tread Depth
                4) Windshield Wiper Fluid
                5) Wiper Blades
         B. Periodical Checks (Every 3-6 Months or 3,000-6,000 Miles):
                1) Air Filter (Engine)
                2) Automatic Transmission Fluid
                3) Power Steering Fluid
                4) Belts and Hoses
                5) Battery
                6) Tire Rotation
         C. Annual or Less Frequent Checks:
                1) Brakes
                2) Cabin Air Filter
                3) Exhaust System
                4) Spark Plugs
                5) Wheel Alignment
         */

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
    public void downloadMaintenances(String marca) {
        try {
            MyJDBC myJDBC = new MyJDBC();
            Connection connection = myJDBC.obtenerConexion();

            if (connection != null) {
                String query = "SELECT TYPE_OF_MAINTENANCE, MILEAGE_RATE,MILEAGE_RATE_OTHER, TIME_RATE, NOTE, IMAGEN FROM " + marca;
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet rs = statement.executeQuery();
                int count = 0;
                while (rs.next()) {

                    String type = rs.getString("TYPE_OF_MAINTENANCE");
                    int mileage_rate = Integer.parseInt(rs.getString("MILEAGE_RATE"));
                    int mileage_rate_other = Integer.parseInt(rs.getString("MILEAGE_RATE_OTHER"));
                    int time_rate = Integer.parseInt(rs.getString("TIME_RATE"));
                    String note = rs.getString("NOTE");

                }

                statement.close();
                connection.close();
                // adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




