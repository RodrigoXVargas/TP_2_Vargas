package org.example;


import org.bson.Document;

public class Pais {

    private int codigo;
    private String nombre;
    private String capital;
    private String region;
    private Long poblacion;
    private Double latitud;
    private Double longitud;

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Long getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(Long poblacion) {
        this.poblacion = poblacion;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Pais() {
    }

    public Pais(int codigo, String nombre, String capital, String region, Long poblacion, Double latitud, Double longitud) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.capital = capital;
        this.region = region;
        this.poblacion = poblacion;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Document toDocument(){
        Document document = new Document();
        document.append("codigo", this.codigo);
        document.append("nombre", this.nombre);
        document.append("capital", this.capital);
        document.append("region", this.region);
        document.append("poblacion", this.poblacion);
        document.append("latitud", this.latitud);
        document.append("longitud", this.longitud);
        return document;
    }
}
