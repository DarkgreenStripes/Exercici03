# Gestor CRUD del catálogo de películas

Proyecto Java para practicar Acceso a Datos con persistencia en XML y JSON. La aplicación carga un catálogo inicial desde un fichero XML, lo convierte a JSON y permite gestionar las películas mediante operaciones CRUD desde una interfaz por consola.

## Objetivo del proyecto

Este ejercicio combina varias tareas típicas de acceso a datos:

- Lectura de datos desde XML con JDOM2.
- Transformación y almacenamiento en JSON con Jackson.
- Gestión de un catálogo de películas por consola.
- Validación de datos antes de persistir cambios.
- Escritura segura en fichero temporal para evitar corrupción del JSON.

## Tecnologías utilizadas

- Java
- Maven
- JDOM2 para procesar XML
- Jackson (core + databind) para leer y escribir JSON

## Estructura del proyecto

```text
Exercici03/
├── pom.xml
├── README.md
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/
│       │       ├── Main.java
│       │       ├── Menu.java
│       │       ├── CatalogoPeliculas.java
│       │       └── Pelicula.java
│       └── resources/
│           ├── peliculas.xml
│           └── peliculas.json
└── target/
```

### Descripción de las clases

#### `Main`
Clase principal que inicia la aplicación.

```java
public static void main(String[] args) {
    Menu menu = new Menu(
            "src/main/resources/peliculas.xml",
            "src/main/resources/peliculas.json");
    menu.cargarMenu();
}
```

#### `Menu`
Se encarga de la interacción con el usuario mediante consola. Muestra el menú, lee las opciones introducidas y delega cada operación en la clase `CatalogoPeliculas`.

Incluye validaciones para:

- entradas numéricas inválidas,
- valores vacíos,
- confirmación antes de borrar,
- año opcional al modificar con conservación del valor actual si se pulsa Enter.

#### `Pelicula`
Modelo de datos que representa cada película con estos atributos:

- `id`
- `titulo`
- `director`
- `año`
- `genero`

La clase incluye getters, setters y `toString()` para mostrar la información en consola.

#### `CatalogoPeliculas`
Es la capa principal de gestión del catálogo. Contiene la lógica para:

- convertir XML a JSON,
- cargar el JSON,
- listar todas las películas,
- buscar por ID,
- insertar una nueva película,
- modificar una existente,
- eliminar una película,
- validar IDs únicos y campos obligatorios.

También gestiona la escritura segura del JSON usando un fichero temporal y un movimiento atómico o, si no es posible, una sustitución directa.

## Ficheros de datos

### `src/main/resources/peliculas.xml`
Es el origen inicial del catálogo. Tiene esta estructura:

```xml
<peliculas>
  <pelicula>
    <id>1</id>
    <titulo>Interstellar</titulo>
    <director>Christopher Nolan</director>
    <anio>2014</anio>
    <genero>Ciencia ficción</genero>
  </pelicula>
</peliculas>
```

### `src/main/resources/peliculas.json`
Es el fichero de trabajo del programa. Una vez convertido, guarda la información en formato JSON con esta estructura:

```json
{
  "peliculas": [
    {
      "id": 1,
      "titulo": "Interstellar",
      "director": "Christopher Nolan",
      "año": 2014,
      "genero": "Ciencia ficción"
    }
  ]
}
```

## Funcionalidades de la aplicación

El menú ofrece estas opciones:

1. Convertir XML a JSON
2. Mostrar todas las películas
3. Buscar una película por ID
4. Añadir una película
5. Modificar una película por ID
6. Eliminar una película por ID
0. Salir

### Flujo recomendado

1. Ejecutar la aplicación.
2. Seleccionar la opción 1 para convertir el XML inicial a JSON.
3. Usar el resto de opciones sobre el fichero JSON.

## Comportamiento y validaciones importantes

- Si el fichero XML no existe, la conversión falla y la aplicación informa del error.
- Si el fichero JSON no existe, las operaciones CRUD avisan de que primero debe realizarse la conversión.
- Los cambios se escriben primero en un archivo temporal y solo se reemplaza el JSON cuando la escritura termina correctamente.
- Si el XML contiene un ID o un año no numérico, se lanza una excepción y se avisa al usuario.
- El programa valida que los IDs no se dupliquen y que los campos obligatorios no estén vacíos.
- Al modificar una película, si el año introducido no es válido, se vuelve a pedir.
- Al eliminar, la aplicación pide confirmación antes de borrar físicamente la película.

## Ejecución

Desde la raíz del proyecto:

```bash
mvn compile
mvn exec:java -Dexec.mainClass=com.example.Main
```

## ¿Utiliza la metodología POJO?

Sí, pero con una distinción importante:

- La clase `Pelicula` es un claro ejemplo de POJO.
- Las otras clases principales (`CatalogoPeliculas` y `Menu`) no son POJOs estrictos porque incluyen lógica de acceso a datos y de interacción con el usuario.

Un POJO en Java es una clase simple con atributos privados, constructor vacío, getters/setters y sin depender de frameworks ni de herencias especiales. En este proyecto, `Pelicula` cumple exactamente esa definición:

```java
public class Pelicula {
    private int id;
    private String titulo;
    private String director;
    private int año;
    private String genero;

    public Pelicula() {
    }

    public Pelicula(int id, String titulo, String director, int año, String genero) {
        this.id = id;
        this.titulo = titulo;
        this.director = director;
        this.año = año;
        this.genero = genero;
    }
}
```

En cambio, `CatalogoPeliculas` sí tiene responsabilidades de negocio y persistencia:

- leer XML,
- convertirlo a JSON,
- validar datos,
- guardar y cargar archivos,
- gestionar la colección completa de películas.

Esto hace que sea más una clase de gestión/servicio que un POJO puro.

### ¿Se podría hacer todo el proyecto POJO?

Sí, técnicamente se podría, pero no sería la mejor opción para este caso. Un diseño totalmente POJO se podría lograr si todas las clases fueran simples Java sin lógica de infraestructura ni patrones más complejos, pero entonces:

- se perdería claridad,
- la separación de responsabilidades sería peor,
- el código sería menos mantenible,
- y no aportaría un beneficio real para una práctica de acceso a datos.

Por tanto, la respuesta más precisa es:

- la entidad `Pelicula` sí es POJO,
- el proyecto completo combina POJO con clases de gestión y de interfaz de usuario.

## Resumen

Este proyecto es una práctica completa de acceso a datos en Java en la que se trabaja con dos formatos distintos:

- XML como origen de datos inicial.
- JSON como formato de almacenamiento final.

La app permite gestionar un catálogo de películas de forma segura y con validaciones, simulando un pequeño sistema CRUD de consola.
