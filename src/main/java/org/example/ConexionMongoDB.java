package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class ConexionMongoDB {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    public void setCollection(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public ConexionMongoDB() {
    }

    public ConexionMongoDB(String database, String collection) throws Exception{
        try{
            this.mongoClient = MongoClients.create();
            this.database = this.getMongoClient().getDatabase(database);
            this.collection = this.database.getCollection(collection);
        }catch (Exception e) {
            System.out.println("Error al conectar con MongoDB\n" + e.getMessage().toString());
            e.printStackTrace();
        }
    }
}
