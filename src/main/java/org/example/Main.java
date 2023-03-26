package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;


public class Main {
    public static JSONArray conexionHTTPURL(String urlString) throws Exception{
        URL url = new URL(urlString);
        JSONArray jsonArray = null;
        try{
            HttpURLConnection cx = (HttpURLConnection) url.openConnection();
            cx.setRequestMethod("GET");
            try (InputStream strn = cx.getInputStream()){
                byte[] arrStream = strn.readAllBytes();

                String cntJson = "";

                for(byte tmp: arrStream){
                    cntJson += (char)tmp;
                }

                jsonArray = new JSONArray(cntJson);
                cx.disconnect();
            }catch (IOException e){
                System.out.println("Sin datos");
            }

        }catch (Exception e){
            System.err.println("Error en metodo conexionHTTPURL()\n"+e);
        }
        return jsonArray;
    }

    public static Pais creacionPais(JSONArray jsonArray){
        Pais pais = new Pais();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jObject = (JSONObject)jsonArray.get(i);
                pais.setCodigo(Integer.parseInt((jObject.get("ccn3")).toString()));
                pais.setNombre(new JSONObject(jObject.get("name").toString()).get("common").toString());
                JSONArray capitales = jObject.getJSONArray("capital");
                pais.setCapital(capitales.get(0).toString());
                if(pais.getCodigo()==148){
                    pais.setCapital((pais.getCapital().replace("'","")));
                }
                pais.setRegion((jObject.get("region")).toString());
                pais.setPoblacion(Long.parseLong(jObject.get("population").toString()));
                JSONArray latLong = (JSONArray)jObject.get("latlng");
                pais.setLatitud(Double.parseDouble(latLong.get(0).toString()));
                pais.setLongitud(Double.parseDouble(latLong.get(1).toString()));
            } catch (Exception e) {
                System.err.println("Error en metodo creacionPais()\n"+e);
            }
        }
        return pais;
    }

    public static void conexionDB(String url, String nombreDB, String userDB, String passwordDB, Pais pais) throws SQLException{
        ConexionDB db = null;
        try{
            db = new ConexionDB(url, nombreDB, userDB, passwordDB);
            String queryBuscar = "SELECT codigoPais FROM pais WHERE codigoPais = ?";
            PreparedStatement preparedStatement = db.getConn().prepareStatement(queryBuscar);
            preparedStatement.setInt(1, pais.getCodigo());
            db.setResultSet(preparedStatement.executeQuery());
            if (db.getResultSet().next()){
                String query = "UPDATE pais SET nombrePais = "+pais.getNombre()+
                        ", capitalPais = "+pais.getCapital()+
                        ", región = "+pais.getRegion()+
                        ", población = "+pais.getPoblacion()+
                        ", latitud = "+pais.getLatitud()+
                        ", longitud  = "+pais.getLongitud()+
                        " WHERE codigoPais = "+pais.getCodigo();
                db.getStmt().executeUpdate(query);
            }else {
                String query = "INSERT INTO pais(codigoPais, nombrePais, capitalPais, región, población, latitud, longitud) " +
                        "VALUES ('"+pais.getCodigo()+"', '"+pais.getNombre()+"', '"+pais.getCapital()+"', '"+pais.getRegion()+"', " +
                        "'"+pais.getPoblacion()+"', '"+pais.getLatitud()+"', '"+pais.getLongitud()+"')";
                db.getStmt().executeUpdate(query);
            }
        }catch (SQLException e){
            System.err.println("Error en metodo conexionDB()\n"+e);
        }finally {
            db.CerrarConexion();
        }

    }

    public static void main(String[] args) throws Exception {
        String url = "https://restcountries.com/v3.1/alpha/";
        int num = 1;
        while(num < 301){
            System.out.print("Numero "+num+": ");
            JSONArray jsonArray = conexionHTTPURL(url+num);
            if(!(jsonArray==null)){
                Pais pais = creacionPais(jsonArray);
                conexionDB("jdbc:mysql://localhost:3306/", "paises_tp2", "root", "", pais);
                System.out.println("Pais guardado");
            }
            num++;
        }
    }
}