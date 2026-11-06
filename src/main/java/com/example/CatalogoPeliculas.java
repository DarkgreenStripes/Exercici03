package com.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class CatalogoPeliculas {
    private List<Pelicula> peliculas;
    private transient String rutaXml;
    private transient String rutaJson;
    private static final ObjectMapper MAPPER =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public CatalogoPeliculas() {
        peliculas = new ArrayList<>();
    }

    public CatalogoPeliculas(List<Pelicula> peliculas) {
        this.peliculas = peliculas == null ? new ArrayList<>() : peliculas;
    }

    public CatalogoPeliculas(String rutaXml, String rutaJson) {
        this();
        this.rutaXml = rutaXml;
        this.rutaJson = rutaJson;
    }

    public List<Pelicula> getPeliculas() {
        return peliculas;
    }

    public void setPeliculas(List<Pelicula> peliculas) {
        this.peliculas = peliculas == null ? new ArrayList<>() : peliculas;
    }

    public int convertirXmlAJson() throws IOException, JDOMException {
        File archivoXml = new File(rutaXml);
        if (!archivoXml.isFile()) {
            throw new FileNotFoundException("No se encuentra el fichero XML: " + rutaXml);
        }

        Document documento = new SAXBuilder().build(archivoXml);
        Element raiz = documento.getRootElement();
        List<Pelicula> peliculasConvertidas = new ArrayList<>();

        for (Element elemento : raiz.getChildren("pelicula")) {
            String anio = obtenerTexto(elemento, "anio", "año");
            try {
                peliculasConvertidas.add(new Pelicula(
                        Integer.parseInt(obtenerTexto(elemento, "id")),
                        obtenerTexto(elemento, "titulo"),
                        obtenerTexto(elemento, "director"),
                        Integer.parseInt(anio),
                        obtenerTexto(elemento, "genero")));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Hay un ID o año no numérico en el XML de origen.", e);
            }
        }

        this.peliculas = peliculasConvertidas;
        guardarCatalogo(this);
        return peliculasConvertidas.size();
    }

    public List<Pelicula> obtenerTodas() throws IOException {
        return cargarCatalogo().getPeliculas();
    }

    public Pelicula buscarPorId(int id) throws IOException {
        for (Pelicula pelicula : cargarCatalogo().getPeliculas()) {
            if (pelicula.getId() == id) {
                return pelicula;
            }
        }
        return null;
    }

    public int agregarPelicula(String titulo, String director, int año, String genero)
            throws IOException {
        validarTexto(titulo, "El título");
        validarTexto(director, "El director");
        validarTexto(genero, "El género");

        CatalogoPeliculas catalogo = cargarCatalogo();
        int maxId = 0;
        for (Pelicula pelicula : catalogo.getPeliculas()) {
            maxId = Math.max(maxId, pelicula.getId());
        }

        int nuevoId;
        try {
            nuevoId = Math.addExact(maxId, 1);
        } catch (ArithmeticException e) {
            throw new IOException("No se puede generar un ID nuevo: se alcanzó el límite.", e);
        }

        catalogo.getPeliculas().add(new Pelicula(nuevoId, titulo, director, año, genero));
        guardarCatalogo(catalogo);
        return nuevoId;
    }

    public boolean modificarPelicula(int id, String titulo, String director, int año, String genero)
            throws IOException {
        validarTexto(titulo, "El título");
        validarTexto(director, "El director");
        validarTexto(genero, "El género");

        CatalogoPeliculas catalogo = cargarCatalogo();
        for (Pelicula pelicula : catalogo.getPeliculas()) {
            if (pelicula.getId() == id) {
                pelicula.setTitulo(titulo);
                pelicula.setDirector(director);
                pelicula.setAño(año);
                pelicula.setGenero(genero);
                guardarCatalogo(catalogo);
                return true;
            }
        }
        return false;
    }

    public boolean eliminarPelicula(int id) throws IOException {
        CatalogoPeliculas catalogo = cargarCatalogo();
        boolean eliminada = catalogo.getPeliculas().removeIf(pelicula -> pelicula.getId() == id);
        if (eliminada) {
            guardarCatalogo(catalogo);
        }
        return eliminada;
    }

    private CatalogoPeliculas cargarCatalogo() throws IOException {
        File archivo = new File(rutaJson);
        if (!archivo.isFile()) {
            throw new IOException("No existe " + rutaJson
                    + ". Ejecuta primero la opción de conversión XML a JSON.");
        }

        CatalogoPeliculas catalogo = MAPPER.readValue(archivo, CatalogoPeliculas.class);
        if (catalogo == null || catalogo.getPeliculas() == null) {
            throw new IOException("El catálogo del fichero " + rutaJson + " no es válido.");
        }
        validarIdsUnicos(catalogo.getPeliculas());
        return catalogo;
    }

    private void guardarCatalogo(CatalogoPeliculas catalogo) throws IOException {
        Path archivo = Path.of(rutaJson).toAbsolutePath();
        Path directorio = archivo.getParent();
        Files.createDirectories(directorio);
        Path temporal = Files.createTempFile(directorio, "peliculas-", ".json.tmp");
        IOException errorEscritura = null;

        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(temporal.toFile(), catalogo);
            try {
                Files.move(temporal, archivo,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            errorEscritura = e;
            throw e;
        } finally {
            try {
                Files.deleteIfExists(temporal);
            } catch (IOException e) {
                if (errorEscritura != null) {
                    errorEscritura.addSuppressed(e);
                } else {
                    throw e;
                }
            }
        }
    }

    private static String obtenerTexto(Element elemento, String... nombres) {
        for (String nombre : nombres) {
            String texto = elemento.getChildTextTrim(nombre);
            if (texto != null && !texto.isEmpty()) {
                return texto;
            }
        }
        throw new IllegalArgumentException(
                "Falta el elemento <" + String.join("> o <", nombres) + "> en el XML.");
    }

    private static void validarTexto(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombreCampo + " no puede estar vacío.");
        }
    }

    private static void validarIdsUnicos(List<Pelicula> peliculas) throws IOException {
        Set<Integer> ids = new HashSet<>();
        for (Pelicula pelicula : peliculas) {
            if (pelicula == null
                    || pelicula.getId() <= 0
                    || !ids.add(pelicula.getId())
                    || pelicula.getTitulo() == null || pelicula.getTitulo().isBlank()
                    || pelicula.getDirector() == null || pelicula.getDirector().isBlank()
                    || pelicula.getGenero() == null || pelicula.getGenero().isBlank()) {
                throw new IOException(
                        "El catálogo contiene una película inválida o un ID duplicado.");
            }
        }
    }
}
