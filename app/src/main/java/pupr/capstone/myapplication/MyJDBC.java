package pupr.capstone.myapplication;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@SuppressLint("NewApi")
public class MyJDBC {

    Connection con;
    String uname, pass, ip, port, database;

    public Connection obtenerConexion() {


        ip = "192.168.0.29";
        port = "3306";
        database = "Test";
        uname = "root";
        pass = "Eecf#0819";

/*
        ip = "10.0.0.64";
        port = "3306";
        database = "Test";
        uname = "root";
        pass = "C@pstoneAJE3";
*/
        //php -Que es un api (intermediario entre applicación)
        //firebase sera un api?
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL;

        try {
            Class.forName("com.mysql.jdbc.Driver"); // Cambiado
            ConnectionURL = "jdbc:mysql://" + ip + ":" + port + "/" + database +
                    "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";// Cambiado

            Log.d("MyJDBC", "Intentando conectar a: " + ConnectionURL);
            connection = DriverManager.getConnection(ConnectionURL, uname, pass);
            Log.d("MyJDBC", "Conexión exitosa");

        } catch (SQLException ex) {
            Log.e("MyJDBC", "SQLException: " + ex.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("MyJDBC", "Driver no encontrado: " + e.getMessage());
        }

        return connection;
    }
}
