package org.example;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Sorts;
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
import java.util.ArrayList;
import java.util.List;


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

    public static ArrayList<Pais> creacionPais(JSONArray jsonArray){
        ArrayList<Pais> paisArrayList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Pais pais = new Pais();
                JSONObject jObject = (JSONObject)jsonArray.get(i);
                pais.setCodigo(Integer.parseInt((jObject.get("numericCode")).toString()));
                pais.setNombre(jObject.get("name").toString());
                try{
                    pais.setCapital(jObject.get("capital").toString());
                }catch (org.json.JSONException jsonException){
                    pais.setCapital("Undefined");
                }
                pais.setRegion((jObject.get("region")).toString());
                pais.setPoblacion(Long.parseLong(jObject.get("population").toString()));
                try{
                    JSONArray latLong = (JSONArray)jObject.get("latlng");
                    pais.setLatitud(Double.parseDouble(latLong.get(0).toString()));
                    pais.setLongitud(Double.parseDouble(latLong.get(1).toString()));
                }catch (org.json.JSONException jsonException){
                    pais.setLatitud(0.0);
                    pais.setLongitud(0.0);
                }
                paisArrayList.add(pais);
            } catch (Exception e) {
                System.err.println("Error en metodo creacionPais()\n"+e);
                e.printStackTrace();
            }
        }
        //verificacion
                /*
                System.out.println("---------------------------------");
                System.out.println("nombre: "+pais.getNombre());
                System.out.println(pais.getCapital());
                System.out.println(pais.getRegion());
                System.out.println(pais.getPoblacion());
                System.out.println(pais.getCodigo());
                System.out.println(pais.getLatitud());
                System.out.println(pais.getLongitud());
                System.out.println("---------------------------------");
                */
        return paisArrayList;
    }

    public static String reemplazar(String string){
        string = string.replace("'", "");
        if(string.length()>50){
            string = string.substring(0, 50);
        }
        return string;
    }

    public static void buscarSQL(ConexionDBSQL db, Pais pais) throws SQLException{
        pais.setNombre(reemplazar(pais.getNombre()));
        pais.setCapital(reemplazar(pais.getCapital()));
        pais.setRegion(reemplazar(pais.getRegion()));
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
                System.out.println("      Calling code: "+pais.getCodigo() +" Registro actualizado");
            }else {
                String query = "INSERT INTO pais(codigoPais, nombrePais, capitalPais, región, población, latitud, longitud) " +
                        "VALUES ('"+pais.getCodigo()+"', '"+pais.getNombre()+"', '"+pais.getCapital()+"', '"+pais.getRegion()+"', " +
                        "'"+pais.getPoblacion()+"', '"+pais.getLatitud()+"', '"+pais.getLongitud()+"')";
                db.getStmt().executeUpdate(query);
                System.out.println("      Calling code: "+pais.getCodigo() +" Registro guardado");
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
                System.out.println("      Calling code: "+pais.getCodigo() +" Registro actualizado");
            } else {
                collection.insertOne(pais.toDocument());
                System.out.println("      Calling code: "+pais.getCodigo() +" Registro guardado");
            }
        }catch (Exception e){
            System.err.println("Error en metodo buscarMongo()\n"+e);
            e.printStackTrace();
        }
    }

    public static void preguntasFinales(){
        ConexionMongoDB dbMongo = null;
        try{
            dbMongo = new ConexionMongoDB("paises_db","paises");
            System.out.println("Conexión exitosa a la base de datos MongoDB " + dbMongo.getDatabase().getName());
        }catch (Exception e){
            System.out.println("Fallo en conexion con DB mongo\n"+e);
            e.printStackTrace();
        }

        try{
            Document filter = new Document("region","Americas");
            MongoCursor<Document> cursor = dbMongo.getCollection().find(filter).iterator();
            System.out.println("Pregunta 5.1: ");
            while(cursor.hasNext()){
                System.out.print(cursor.next().getString("nombre") +", ");
            }
            System.out.println("\n--------------------------------------------------------------------------");
        }catch (Exception e){
            System.out.println("Fallo en pregunta 5.1\n"+e);
            e.printStackTrace();
        }

        try{
            Document filter = new Document("region", "Americas").append("poblacion", new Document("$gt", 100000000));
            MongoCursor<Document> cursor = dbMongo.getCollection().find(filter).iterator();
            System.out.println("Pregunta 5.2: ");
            while(cursor.hasNext()){
                System.out.print(cursor.next().getString("nombre") +", ");
            }
            System.out.println("\n--------------------------------------------------------------------------");
        }catch (Exception e){
            System.out.println("Fallo en pregunta 5.2\n"+e);
            e.printStackTrace();
        }

        try{
            Document filter = new Document("region", new Document("$ne", "Africa"));
            MongoCursor<Document> cursor = dbMongo.getCollection().find(filter).iterator();
            System.out.println("Pregunta 5.3: ");
            while(cursor.hasNext()){
                System.out.print(cursor.next().getString("nombre") +", ");
            }
            System.out.println("\n--------------------------------------------------------------------------");
        }catch (Exception e){
            System.out.println("Fallo en pregunta 5.3\n"+e);
            e.printStackTrace();
        }

        try{
            Document filter = new Document("nombre", "Egypt");
            Document update = new Document("$set", new Document("nombre", "Egipto").append("poblacion", 95000000));
            dbMongo.getCollection().updateOne(filter, update);
            System.out.println("Pregunta 5.4: ");
            MongoCursor<Document> cursor = dbMongo.getCollection().find(new Document("nombre", "Egipto")).iterator();
            while(cursor.hasNext()){
                System.out.print(cursor.next().getString("nombre") +", ");
            }
            System.out.println("\n--------------------------------------------------------------------------");
        }catch (Exception e){
            System.out.println("Fallo en pregunta 5.4\n"+e);
            e.printStackTrace();
        }

        try{
            Document filter = new Document("codigo", 258);
            dbMongo.getCollection().deleteOne(filter);

            System.out.println("Pregunta 5.5: ");
            MongoCursor<Document> cursor = dbMongo.getCollection().find(filter).iterator();
            if (cursor.hasNext()){
                System.out.println("Todavia existe el registro");
            }else {
                System.out.println("Borrado correctamente");
            }
            System.out.println("\n--------------------------------------------------------------------------");
        }catch (Exception e){
            System.out.println("Fallo en pregunta 5.5\n"+e);
            e.printStackTrace();
        }

        try{
            List<Document> filters = new ArrayList<>();
            filters.add(new Document("poblacion", new Document("$gt", 50000000)));
            filters.add(new Document("poblacion", new Document("$lt", 150000000)));
            Document filter = new Document("$and", filters);
            MongoCursor<Document> results = dbMongo.getCollection().find(filter).iterator();
            System.out.println("Pregunta 5.7: ");
            while(results.hasNext()){
                System.out.print(results.next().get("nombre") +", ");
            }
            System.out.println("\n--------------------------------------------------------------------------");
        }catch (Exception e){
            System.out.println("Fallo en pregunta 5.7\n"+e);
            e.printStackTrace();
        }

        try{
            MongoCursor<Document> results = dbMongo.getCollection().find().sort(Sorts.ascending("nombre")).iterator();
            System.out.println("Pregunta 5.8: ");
            while(results.hasNext()){
                System.out.print(results.next().get("nombre").toString()+", ");
            }

            System.out.println("\n--------------------------------------------------------------------------");
        }catch (Exception e){
            System.out.println("Fallo en pregunta 5.8\n"+e);
            e.printStackTrace();
        }

        try{
            MongoCursor<Document> results = dbMongo.getCollection().find().skip(10).iterator();
            System.out.println("Pregunta 5.9: ");
            while(results.hasNext()){
                System.out.print(results.next().get("nombre").toString()+", ");
            }

            System.out.println("\n--------------------------------------------------------------------------");
        }catch (Exception e){
            System.out.println("Fallo en pregunta 5.9\n"+e);
            e.printStackTrace();
        }

        try{
            var regex = "mex";
            var query = Filters.regex("nombre", regex, "i");
            MongoCursor<Document> cursor = dbMongo.getCollection().find(query).iterator();
            System.out.println("Pregunta 5.10: ");
            while (cursor.hasNext()) {
                System.out.print(cursor.next().get("nombre").toString()+", ");
            }
            System.out.println("\n--------------------------------------------------------------------------");
        }catch (Exception e){
            System.out.println("Fallo en pregunta 5.10\n"+e);
            e.printStackTrace();
        }

        try{
            IndexOptions options = new IndexOptions().unique(true);
            dbMongo.getCollection().createIndex(new Document("codigo", 1), options);
            System.out.println("Pregunta 5.11: ");
            System.out.println("Indice creado");


            System.out.println("\n--------------------------------------------------------------------------");
        }catch (Exception e){
            System.out.println("Fallo en pregunta 5.11\n"+e);
            e.printStackTrace();
        }

        dbMongo.getMongoClient().close();
    }


    public static void ejecutar(int seleccion) throws Exception {
        ConexionMongoDB dbMongo = null;
        ConexionDBSQL db = null;
        //String url = "https://restcountries.com/v3.1/alpha/";
        String url = "https://restcountries.com/v2/callingcode/";
        int num = 1;

        try{
            if(seleccion==0){
                db = new ConexionDBSQL("jdbc:mysql://localhost:3306/", "paises_db", "root", "");
                System.out.println("Conexión exitosa a la base de datos SQL " + db.getNombreDB());
            } else {
                dbMongo = new ConexionMongoDB("paises_db","paises");
                System.out.println("Conexión exitosa a la base de datos MongoDB " + dbMongo.getDatabase().getName());
            }
            while(num < 301){
                JSONArray jsonArray = conexionHTTPURL(url+num);
                if(!(jsonArray==null)){
                    System.out.println("Numero "+num+":");
                    ArrayList<Pais> paisArrayList = creacionPais(jsonArray);
                    for (Pais pais : paisArrayList) {
                        if(seleccion == 0){
                            buscarSQL(db, pais);
                        } else {
                            buscarMongo(dbMongo.getCollection(), pais);
                        }
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
            if(seleccion==1){
                preguntasFinales();
            }
        } else {
            System.exit(0);
        }

    }
}