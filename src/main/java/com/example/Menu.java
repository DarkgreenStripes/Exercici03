package com.example;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.jdom2.JDOMException;

public class Menu {
    private final String rutaXml;
    private final String rutaJson;
    private final Scanner scanner = new Scanner(System.in);

    public Menu(String rutaXml, String rutaJson) {
        this.rutaXml = rutaXml;
        this.rutaJson = rutaJson;
    }

    public void cargarMenu() {
        CatalogoPeliculas gestor = new CatalogoPeliculas(rutaXml, rutaJson);
        Integer opcion;
        boolean continuar = true;

        do {
            mostrarMenu();
            opcion = leerOpcion();
            if (opcion == null) {
                break;
            }

            switch (opcion) {
                case 1 -> continuar = convertirXmlAJson(gestor);
                case 2 -> mostrarTodas(gestor);
                case 3 -> buscarPorId(gestor);
                case 4 -> agregarPelicula(gestor);
                case 5 -> modificarPelicula(gestor);
                case 6 -> eliminarPelicula(gestor);
                case 0 -> System.out.println("Saliendo de la aplicación...");
                default -> System.out.println("Opción no válida. Vuelve a intentarlo.");
            }
        } while (continuar && opcion != 0);
    }

    private void mostrarMenu() {
        System.out.println("""
                \n========== CATÁLOGO DE PELÍCULAS ==========
                1. Convertir XML a JSON
                2. Mostrar todas las películas
                3. Buscar una película por ID
                4. Añadir una película
                5. Modificar una película por ID
                6. Eliminar una película por ID
                0. Salir
                ==========================================""");
    }

    private boolean convertirXmlAJson(CatalogoPeliculas gestor) {
        try {
            int cantidad = gestor.convertirXmlAJson();
            System.out.println("Conversión completada: " + cantidad
                    + " películas guardadas en " + rutaJson + ".");
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("No se pudo encontrar el XML de origen. La aplicación se cerrará: "
                    + e.getMessage());
            return false;
        } catch (IOException | JDOMException | IllegalArgumentException e) {
            System.err.println("No se pudo convertir el catálogo XML: " + e.getMessage());
            return true;
        }
    }

    private void mostrarTodas(CatalogoPeliculas gestor) {
        try {
            List<Pelicula> peliculas = gestor.obtenerTodas();
            if (peliculas.isEmpty()) {
                System.out.println("El catálogo está vacío.");
                return;
            }

            System.out.println("\n--- CATÁLOGO DE PELÍCULAS ---");
            peliculas.forEach(System.out::println);
        } catch (IOException e) {
            mostrarError(e);
        }
    }

    private void buscarPorId(CatalogoPeliculas gestor) {
        Integer id = leerEntero("Introduce el ID de la película: ");
        if (id == null) {
            return;
        }

        try {
            Pelicula pelicula = gestor.buscarPorId(id);
            if (pelicula == null) {
                System.out.println("No se encontró ninguna película con ID " + id + ".");
            } else {
                System.out.println("Película encontrada:\n" + pelicula);
            }
        } catch (IOException e) {
            mostrarError(e);
        }
    }

    private void agregarPelicula(CatalogoPeliculas gestor) {
        System.out.println("\n--- AÑADIR PELÍCULA ---");
        String titulo = leerTexto("Título: ");
        String director = leerTexto("Director: ");
        Integer año = leerEntero("Año: ");
        String genero = leerTexto("Género: ");
        if (titulo == null || director == null || año == null || genero == null) {
            return;
        }

        try {
            int id = gestor.agregarPelicula(titulo, director, año, genero);
            System.out.println("Película añadida correctamente con ID " + id + ".");
        } catch (IOException | IllegalArgumentException e) {
            mostrarError(e);
        }
    }

    private void modificarPelicula(CatalogoPeliculas gestor) {
        Integer id = leerEntero("Introduce el ID de la película que deseas modificar: ");
        if (id == null) {
            return;
        }

        try {
            Pelicula actual = gestor.buscarPorId(id);
            if (actual == null) {
                System.out.println("No existe ninguna película con ID " + id + ".");
                return;
            }

            System.out.println("Valores actuales: " + actual);
            System.out.println("Pulsa Enter para conservar cada valor.");
            String titulo = leerTexto("Nuevo título [" + actual.getTitulo() + "]: ");
            String director = leerTexto("Nuevo director [" + actual.getDirector() + "]: ");
            Integer nuevoAño = leerEnteroOpcional(
                    "Nuevo año [" + actual.getAño() + "]: ", actual.getAño());
            String genero = leerTexto("Nuevo género [" + actual.getGenero() + "]: ");
            if (titulo == null || director == null || nuevoAño == null || genero == null) {
                return;
            }

            if (gestor.modificarPelicula(
                    id,
                    titulo.isBlank() ? actual.getTitulo() : titulo,
                    director.isBlank() ? actual.getDirector() : director,
                    nuevoAño,
                    genero.isBlank() ? actual.getGenero() : genero)) {
                System.out.println("Película modificada correctamente.");
            }
        } catch (IOException | IllegalArgumentException e) {
            mostrarError(e);
        }
    }

    private void eliminarPelicula(CatalogoPeliculas gestor) {
        Integer id = leerEntero("Introduce el ID de la película que deseas eliminar: ");
        if (id == null) {
            return;
        }

        try {
            Pelicula pelicula = gestor.buscarPorId(id);
            if (pelicula == null) {
                System.out.println("No existe ninguna película con ID " + id + ".");
                return;
            }

            System.out.println("Se eliminará: " + pelicula);
            String confirmacion = leerTexto("¿Confirmas el borrado físico? (S/N): ");
            if (confirmacion == null || !confirmacion.equalsIgnoreCase("S")) {
                System.out.println("Operación cancelada.");
                return;
            }

            if (gestor.eliminarPelicula(id)) {
                System.out.println("Película eliminada del fichero JSON.");
            }
        } catch (IOException e) {
            mostrarError(e);
        }
    }

    private Integer leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            System.out.flush();
            if (!scanner.hasNextLine()) {
                return null;
            }
            String entrada = scanner.nextLine().trim();
            try {
                return Integer.valueOf(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Error: introduce un número entero válido.");
            }
        }
    }

    private Integer leerOpcion() {
        System.out.print("Selecciona una opción: ");
        System.out.flush();
        if (!scanner.hasNextLine()) {
            return null;
        }
        try {
            return Integer.valueOf(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        System.out.flush();
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine().trim();
    }

    private Integer leerEnteroOpcional(String mensaje, int valorActual) {
        while (true) {
            String entrada = leerTexto(mensaje);
            if (entrada == null) {
                return null;
            }
            if (entrada.isBlank()) {
                return valorActual;
            }
            try {
                return Integer.valueOf(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Error: introduce un año entero válido o pulsa Enter para conservarlo.");
            }
        }
    }

    private void mostrarError(Exception e) {
        System.err.println("No se pudo completar la operación: " + e.getMessage());
    }
}
