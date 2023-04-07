# TP_2_Vargas

Respuestas de las consultas finales

5.1)
  Document filter = new Document("region","Americas");
  MongoCursor<Document> cursor = dbMongo.getCollection().find(filter).iterator();

5.2)
  Document filter = new Document("region", "Americas").append("poblacion", new Document("$gt", 100000000));
  MongoCursor<Document> cursor = dbMongo.getCollection().find(filter).iterator();
  
5.3)
  Document filter = new Document("region", new Document("$ne", "Africa"));
  MongoCursor<Document> cursor = dbMongo.getCollection().find(filter).iterator();
  
5.4)
  Document filter = new Document("nombre", "Egypt");
  Document update = new Document("$set", new Document("nombre", "Egipto").append("poblacion", 95000000));
  dbMongo.getCollection().updateOne(filter, update);

5.5)
  Document filter = new Document("codigo", 258);
  dbMongo.getCollection().deleteOne(filter);
  
5.6)
  El método drop() se utiliza para eliminar completamente una base de datos o una colección de MongoDB.
  Si se ejecuta drop() en una base de datos, se eliminarán todas las colecciones que pertenecen a esa base de datos. 
  Esto significa que se perderán todos los datos y las configuraciones asociadas con esas colecciones. 
  Por otro lado, si se ejecuta drop() en una colección, solo se eliminarán los documentos que pertenecen a esa colección. 
  La colección en sí misma seguirá existiendo con las mismas configuraciones y estructura de índice. 
  En otras palabras, se puede pensar en drop() en una colección como una operación de "borrado total" de los datos de la colección, 
  pero no de la colección en sí.
  
5.7)
  List<Document> filters = new ArrayList<>();
  filters.add(new Document("poblacion", new Document("$gt", 50000000)));
  filters.add(new Document("poblacion", new Document("$lt", 150000000)));
  Document filter = new Document("$and", filters);
  MongoCursor<Document> results = dbMongo.getCollection().find(filter).iterator();
  
5.8)
  MongoCursor<Document> results = dbMongo.getCollection().find().sort(Sorts.ascending("nombre")).iterator();

5.9)
  La función skip() en MongoDB se utiliza en combinación con la función de consulta find() para especificar cuántos documentos 
  debe saltar la consulta antes de comenzar a devolver resultados.
  MongoCursor<Document> results = dbMongo.getCollection().find().skip(10).iterator();
  En este caso de ejemplo le pedimos que nos devuelva todos los registros de la coleccion saltando los primeros 10
  
5.10)
  Las expresiones regulares en MongoDB son una combinación de caracteres que definen un patrón de búsqueda.
  Suponiendo que queremos buscar todos los países en nuestra base de datos que contengan la cadena "mex" en el nombre, 
  independientemente de si está escrito en mayúsculas o minúsculas, podríamos hacerlo usando una expresión regular en MongoDB de la siguiente manera en Java:
  var regex = "mex";
  var query = Filters.regex("nombre", regex, "i");
  MongoCursor<Document> cursor = dbMongo.getCollection().find(query).iterator();
  
  o en Javascript de la siguiente manera: 
  db.paises.find({nombre: /mex/i})
  
  Por otro lado, en SQL, la cláusula LIKE se utiliza para buscar patrones en una cadena. 
  Por ejemplo, podríamos usar la misma busqueda en una consulta SQL:
  SELECT * FROM paises WHERE nombre LIKE '%mex%';
  
  Como podemos ver, ambos enfoques se utilizan para buscar patrones en una cadena, pero la sintaxis y la forma de uso son diferentes. 
  Mientras que SQL utiliza la cláusula LIKE para buscar patrones en una cadena, MongoDB utiliza expresiones regulares para lograr el mismo resultado. 
  En general, el uso de expresiones regulares en MongoDB puede proporcionar una mayor flexibilidad y un mayor control sobre las búsquedas avanzadas, 
  y puede ser una alternativa eficaz al uso de la cláusula LIKE de SQL.
  
5.11)
  IndexOptions options = new IndexOptions().unique(true);
  dbMongo.getCollection().createIndex(new Document("codigo", 1), options);
  
  Como añadido se crea el indice con un ordenamiento Ascendente y con valores unicos
  
5.12)
  Para realizar un backup de la base de datos "países_db" en MongoDB, se pueden seguir los siguientes pasos:

  Acceder a la consola de MongoDB o utilizar una herramienta de administración como Compass o Robo 3T.
  Seleccionar la base de datos "países_db".
  Ejecutar el siguiente comando para crear un archivo de backup en la carpeta "backup" del directorio actual:

  mongodump --db países_db --out backup
  
  Este comando utiliza la herramienta "mongodump" de MongoDB para crear una copia de la base de datos "países_db" en la carpeta "backup". 
  La opción "--db" indica la base de datos que se va a respaldar, y la opción "--out" especifica la carpeta donde se guardará el archivo de backup.

  El archivo de backup creado contendrá todos los documentos de la base de datos, así como la información de índices y otros metadatos. 
  Para restaurar la base de datos desde este archivo de backup, se puede utilizar la herramienta "mongorestore" de MongoDB.
