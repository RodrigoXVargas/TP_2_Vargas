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
    /**
     * Funcion que recibe un String con la url a consultar, hace la conexion tipo GET y hace la conversion
     * de la informacion retornada de la url a un JSONArray que se devolverá al final de la funcion
     * @param urlString
     * @return JSONArray
     * @throws Exception
     */
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

    /**
     * Funcion que recibe un JSONArray con los datos a obtener para luego guardarlos en un Objeto Pais e
     * insertarlo en un ArrayList de Paises y retornarlo en la funcion
     * Se coloca un try catch para la obtencion de la capital, la latitud y longitud ya que se verifica
     * que algunos paises tienen problemas en esos datos.
     * @param jsonArray
     * @return ArrayList<Pais>
     */
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
        return paisArrayList;
    }

    /**
     * Funcion que recibe un String para borrarle las comillas simples y asegurarse que solo tengan 50
     * caracteres, borrando los sobrantes.
     * @param string
     * @return String
     */
    public static String reemplazar(String string){
        string = string.replace("'", "");
        if(string.length()>50){
            string = string.substring(0, 50);
        }
        return string;
    }

    /**
     * Metodo para persistir un pais en la BD de SQL
     * Recibe la conexion con la base de datos SQL y el pais a tratar
     * En primera instancia se llama a la funcion "reemplazar(String string)" pasandole como parametro
     * los atributos del pais que sean tipo string para modificarlos y que no interfieran con la llamada SQL
     * Se busca el pais en la base de datos segun el codigo del pais, si no obtiene resultados lo
     * guarda en la coleccion, por el contrario, si obtiene resultados, se actualiza el registro
     * con los datos del pais pasado por parametro
     * @param db
     * @param pais
     * @throws SQLException
     */
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

    /**
     * Metodo para persistir un pais en la BD de MongoDB
     * Recibe la coleccion de la base de datos y el pais a tratar
     * Busca el pais en la base de datos segun el codigo del pais, si no obtiene resultados lo
     * guarda en la coleccion, por el contrario, si obtiene resultados, se actualiza el registro
     * con los datos del pais pasado por parametro
     * @param collection
     * @param pais
     */
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

    /**
     * Metodo que ejecuta todas las respuestas a las preguntas finales del TP sobre MongoDB
     * Se conecta a la DB de MongoDB
     * Cada respuesta tiene su propio Try-Catch porque se fue probando una por una
     * y finalmente se cierra la conexion
     */
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

    /**
     * Este es el metodo principal del objetivo del programa, segun el parametro "seleccion" se procedera a
     * conectar la base de datos correspondiente y luego a hacer la consulta a la url definida. Una vez se
     * el json de la url, si no es nulo, se llama a la funcion "creacionPais(jsonArray)", obteniendo un Array
     * de paises. Segun la seleccion, se pasa por parametro el Array al metodo correspondiente para guardarlos en
     * persistencia elegida. Si el Json es nulo, se muestra por pantalla y finalmente se cierra la conexion
     * con la base seleccionada
     * @param seleccion
     * @throws Exception
     */
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

    /**
     * En el Main se ejecuta un JOptionPane para que el usuario elija el tipo de persistencia del programa,
     * luego de la seleccion, se procede al metodo "ejecutar(seleccion)" pasandole el int de la variable
     * seleccion. Al usuario tambien se le da la opcion de salir desde el mismo JOptionPane
     * @param args
     * @throws Exception
     */
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