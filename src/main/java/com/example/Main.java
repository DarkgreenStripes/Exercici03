package com.example;
public class Main {

    public static void main(String[] args) {
        Menu menu = new Menu(
                "src/main/resources/peliculas.xml",
                "src/main/resources/peliculas.json");
        menu.cargarMenu();
    }
}