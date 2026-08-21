package com.luv2code.springboot.cruddemo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pokemon")
public class Pokemon {

    // define fields
    //
    // El id es String, no int: MongoDB genera un ObjectId (hexadecimal de 24
    // caracteres). No lleva @GeneratedValue — el driver lo asigna al insertar.
    @Id
    private String id;

    // Sin @Column: MongoDB no tiene esquema, así que el campo del documento se
    // llama igual que la propiedad Java. No hay nada que mapear.
    private String name;

    private String type;

    private String level;


    // define constructors
    public Pokemon() {

    }

    public Pokemon(String name, String type, String level) {
        this.name = name;
        this.type = type;
        this.level = level;
    }

    // define getter/setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    // define toString
    @Override
    public String toString() {
        return "Pokemon{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", level='" + level + '\'' +
                '}';
    }
}
