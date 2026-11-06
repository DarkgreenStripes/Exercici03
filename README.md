# Gestor CRUD del catálogo de películas

Ejercicio de Acceso a Datos: importa el catálogo inicial desde XML usando JDOM2 y
realiza las operaciones CRUD sobre `peliculas.json` con Jackson.

## Ejecución

Desde la raíz del proyecto:

```sh
mvn compile
mvn exec:java -Dexec.mainClass=com.example.Main
```

El catálogo JSON está en `src/main/resources/peliculas.json`. La opción
**1. Convertir XML a JSON** lo genera de nuevo a partir de
`src/main/resources/peliculas.xml`. Las opciones de consulta, alta,
modificación y borrado trabajan solo con el fichero JSON.

Si no se encuentra el XML al intentar convertirlo, se informa del error y la
aplicación termina. Si falta el JSON, las operaciones CRUD indican que primero
hay que convertir el XML y devuelven al menú. Los cambios se escriben en un
fichero temporal y sustituyen el JSON solo cuando la escritura termina
correctamente; al modificar, un año no numérico se vuelve a solicitar.
