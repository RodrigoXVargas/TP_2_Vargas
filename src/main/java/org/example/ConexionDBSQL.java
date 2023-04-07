package org.example;

import java.sql.*;

public class ConexionDBSQL {
    private Connection conn;
    private Statement stmt;
    private ResultSet resultSet;
    private String urlDB = "jdbc:mysql://localhost:3306/";
    private String nombreDB;
    private String user;
    private String password;

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public Statement getStmt() {
        return stmt;
    }

    public void setStmt(Statement stmt) {
        this.stmt = stmt;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public String getUrlDB() {
        return urlDB;
    }

    public void setUrlDB(String urlDB) {
        this.urlDB = urlDB;
    }

    public String getNombreDB() {
        return nombreDB;
    }

    public void setNombreDB(String nombreDB) {
        this.nombreDB = nombreDB;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ConexionDBSQL() {
    }

    /**
     * Se crea un contructor con logica para crear la conexion con la DB de SQL, recibiendo la url de la
     * la conexion, el nombre de la DB, el usuario y password.
     * @param urlDB
     * @param nombreDB
     * @param user
     * @param password
     * @throws SQLException
     */
    public ConexionDBSQL(String urlDB, String nombreDB, String user, String password) throws SQLException {
        this.urlDB = urlDB;
        this.nombreDB = nombreDB;
        this.user = user;
        this.password = password;
        try{
            this.conn = DriverManager.getConnection(urlDB+nombreDB, user, password);
            this.stmt = conn.createStatement();
        }catch (SQLException e) {
            System.err.println("Error al conectar la base de datos\n" + e);
        }
    }

    public void CerrarConexion(){
        try {
            this.conn.close();
            this.stmt.close();
        }catch (SQLException e){
            System.err.println("Error al cerrar conexion de base de datos\n" + e );
        }
    }
}
