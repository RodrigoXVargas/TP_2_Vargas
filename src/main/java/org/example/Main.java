package org.example;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
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

            }

        }catch (Exception e){
            System.err.println("Error en metodo conexionHTTPURL()\n"+e);
            e.printStackTrace();
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
                e.printStackTrace();
            }
        }
        return pais;
    }

    public static void buscarSQL(ConexionDBSQL db, Pais pais) throws SQLException{
        try{
            String queryBuscar = "SELECT codigoPais FROM pais WHERE codigoPais = ?";
            PreparedStatement preparedStatement = db.getConn().prepareStatement(queryBuscar);
            preparedStatement.setInt(1, pais.getCodigo());
            db.setResultSet(preparedStatement.executeQuery());
            if (db.getResultSet().next()){
                String query = "UPDATE pais SET nombrePais = '"+pais.getNombre()+
                        "', capitalPais = '"+pais.getCapital()+
                        "', región = '"+pais.getRegion()+
                        "', población = "+pais.getPoblacion()+
                        ", latitud = "+pais.getLatitud()+
                        ", longitud  = "+pais.getLongitud()+
                        " WHERE codigoPais = "+pais.getCodigo();
                db.getStmt().executeUpdate(query);
                System.out.println("Numero "+pais.getCodigo()+": Registro actualizado");
            }else {
                String query = "INSERT INTO pais(codigoPais, nombrePais, capitalPais, región, población, latitud, longitud) " +
                        "VALUES ('"+pais.getCodigo()+"', '"+pais.getNombre()+"', '"+pais.getCapital()+"', '"+pais.getRegion()+"', " +
                        "'"+pais.getPoblacion()+"', '"+pais.getLatitud()+"', '"+pais.getLongitud()+"')";
                db.getStmt().executeUpdate(query);
                System.out.println("Numero "+pais.getCodigo()+": Registro guardado");
            }
        }catch (SQLException e){
            System.err.println("Error en metodo buscarSQL()\n"+e );
            e.printStackTrace();
        }

    }

    public static void buscarMongo(MongoCollection<Document> collection, Pais pais){
        try{

            Document query = new Document("codigo", pais.getCodigo());
            Document result = collection.find(query).first();

            if (result != null) {
                Document filter = new Document("_id", new ObjectId(result.get("_id").toString()));
                Document update = new Document("$set",
                        new Document("id", pais.getCodigo())
                                .append("nombre", pais.getNombre())
                                .append("capital", pais.getCapital())
                                .append("region", pais.getRegion())
                                .append("poblacion", pais.getPoblacion())
                                .append("latitud", pais.getLatitud())
                                .append("longitud", pais.getLongitud()));

                collection.updateOne(filter, update);
                System.out.println("Numero "+pais.getCodigo()+": Registro actualizado");
            } else {
                collection.insertOne(pais.toDocument());
                System.out.println("Numero "+pais.getCodigo()+": Registro guardado");
            }
        }catch (Exception e){
            System.err.println("Error en metodo buscarMongo()\n"+e);
            e.printStackTrace();
        }
    }



    public static void ejecutar(int seleccion) throws Exception {
        ConexionMongoDB dbMongo = null;
        ConexionDBSQL db = null;
        String url = "https://restcountries.com/v3.1/alpha/";
        int num = 1;
        try{
            if(seleccion==0){
                db = new ConexionDBSQL("jdbc:mysql://localhost:3306/", "paises_tp2", "root", "");
                System.out.println("Conexión exitosa a la base de datos SQL " + db.getNombreDB());
            } else {
                dbMongo = new ConexionMongoDB("paises_db","paises");
                System.out.println("Conexión exitosa a la base de datos MongoDB " + dbMongo.getDatabase().getName());
            }
            while(num < 301){
                JSONArray jsonArray = conexionHTTPURL(url+num);
                if(!(jsonArray==null)){
                    Pais pais = creacionPais(jsonArray);
                    if(seleccion == 0){
                        buscarSQL(db, pais);
                    } else {
                        buscarMongo(dbMongo.getCollection(), pais);
                    }
                }else{
                    System.out.println("Numero "+num+": Sin datos");
                }
                num++;
            }
        }catch (Exception e){
            System.err.println("Error en metodo ejecutar()\n"+e);
            e.printStackTrace();
        }finally {
            if(seleccion == 0){
                db.CerrarConexion();
            } else {
                dbMongo.getMongoClient().close();
            }
        }

    }

    public static void main(String[] args) throws Exception {
        String[] options = {"SQL", "MongoDB", "Salir"};
        int seleccion = JOptionPane.showOptionDialog(null, "¿Con qué base de datos quiere ejecutar?",
                "Seleccionar base de datos", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if(seleccion == 0 || seleccion == 1){
            ejecutar(seleccion);
        } else {
            System.exit(0);
        }
    }
}