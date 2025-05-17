# Weka Cancer Prediction API

## Descripción

Este repositorio contiene una API Spring Boot que utiliza el framework Weka para predecir el riesgo de cáncer de mama (benigno/maligno) basado en características de células tumorales. La API también integra con OpenRouter para obtener recomendaciones médicas generadas por IA.

## Características principales

- **Predicción de riesgo de cáncer**: Clasificación de tumores como benignos o malignos usando un modelo pre-entrenado de Weka
- **Recomendaciones de IA**: Integración con OpenRouter para generar consejos médicos personalizados
- **Historial de predicciones**: Almacenamiento en memoria de todas las predicciones realizadas
- **API RESTful**: Endpoints accesibles para integración con otros sistemas

## Tecnologías utilizadas

- Java 17
- Spring Boot 3.x
- Weka 3.8
- OkHttp (para llamadas a API externas)
- Maven (gestión de dependencias)

## Configuración

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/B3RU/weka_.git
   ```

2. Configurar las variables de entorno:
   Crear un archivo `application.properties` en `src/main/resources` con:
   ```properties
   openrouter.api.key=tu_api_key_aqui
   openrouter.api.url=https://openrouter.ai/api/v1/chat/completions
   openrouter.api.model=gpt-3.5-turbo
   ```

3. Ejecutar la aplicación:
   ```bash
   mvn spring-boot:run
   ```

## Endpoints disponibles

- `POST /api/predict` - Realiza una predicción
  ```json
  {
    "clumpThickness": 5,
    "uniformityOfCellSize": 3,
    "uniformityOfCellShape": 3,
    "marginalAdhesion": 2,
    "singleEpithelialCellSize": 4,
    "bareNuclei": 3,
    "blandChromatin": 2,
    "normalNucleoli": 1,
    "mitoses": 1
  }
  ```

- `GET /api/predictions` - Obtiene el historial de todas las predicciones

## Modelo de datos

El modelo utiliza 9 características celulares (todas en escala 1-10):

1. Grosor del tumor (Clump Thickness)
2. Uniformidad del tamaño celular
3. Uniformidad de la forma celular
4. Adhesión marginal
5. Tamaño de células epiteliales individuales
6. Núcleos desnudos
7. Cromatina blanda
8. Nucléolos normales
9. Mitosis

## Estructura del proyecto

```
src/
├── main/
│   ├── java/com/example/weka/
│   │   ├── controller/ - Controladores REST
│   │   ├── entities/ - Entidades de datos
│   │   ├── services/ - Lógica de negocio
│   │   └── WekaApplication.java - Clase principal
│   └── resources/
│       ├── models/ - Modelo pre-entrenado de Weka
│       ├── DataSet/ - Dataset de entrenamiento
│       └── application.properties - Configuración
└── test/ - Pruebas unitarias
```

## Requisitos del sistema

- JDK 17+
- Maven 3.6+
- Conexión a Internet (para llamadas a OpenRouter API)

## Licencia

Este proyecto está bajo la licencia MIT. Ver el archivo [LICENSE](LICENSE) para más detalles.

## Contribuciones

Las contribuciones son bienvenidas. Por favor abre un issue o pull request para cualquier mejora.
