package pupr.capstone.myapplication;
import android.graphics.Bitmap;
import android.os.Build;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

//mport static java.time.LocalDate.now;

//this class will create an object with each type of maintenance. For example: Motor Oil change, tire rotation
public class Maintenance {


    private String maintenance_type;

    private String note;
    private int mileage_rate;
    private int mileage_rate_other;
    private int time_rate;
    private int image_key;//la imagen de mantenimiento se cargara desde la base de dato


    public Maintenance(String atype, int mileage_rate, int mileage_rate_other, int time_rate, String note, int image_key) {

        this.maintenance_type = atype;
        this.time_rate = time_rate;
        this.mileage_rate = mileage_rate;
        this.mileage_rate_other = mileage_rate_other;
        this.note= note;
        this.image_key= image_key;
    }

    public Maintenance(Maintenance maintenance) {

        this(maintenance.maintenance_type,maintenance.time_rate, maintenance.mileage_rate,
                maintenance.mileage_rate_other, maintenance.note,maintenance.image_key);
    }

    public Maintenance(String atype, int mileage_rate) {
        this.maintenance_type = atype;
        this.mileage_rate = mileage_rate;
        this.mileage_rate_other = 0;     // default
        this.time_rate = 0;              // default
        this.note = "";                  // default para evitar null
        this.image_key = 0;              // default
    }


    public void setTypeOfMaintenance(String atype){  this.maintenance_type = atype;   }


   // public void setImageBitmap(Bitmap image){  this.imageBitmap=image;}

    public String getMaintenance(){return this.maintenance_type;  }

    public int getImage() { return image_key; }

    public String getNote(){return this.note;  }

    // ---- GETTERS NUEVOS (necesarios para el Intent) ----
    public int getMileageRate() { return mileage_rate; }
    public int getMileageRateOther() { return mileage_rate_other; }
    public int getTimeRate() { return time_rate; }

 }
