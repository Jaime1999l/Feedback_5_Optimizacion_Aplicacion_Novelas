- Link al repositorio: 
https://github.com/Jaime1999l/Feedback_5_Optimizacion_Aplicacion_Novelas.git

### Jaime López Díaz

# Feedback 5 - Optimización Aplicación de Novelas

## Descripción del Proyecto

Esta aplicación, desarrollada en Android Studio, permite a los usuarios gestionar novelas, agregarlas como favoritas, añadir reseñas y optimizar recursos como memoria, batería y uso de red. La aplicación está desarrollada en Java y utiliza diversas tecnologías de bases de datos tanto externas como internas, tales como Firebase y SQLite.

---

## Funcionalidades Principales

- Listado y detalle de novelas.
- Gestión de novelas favoritas.
- Añadir y visualizar reseñas de usuarios.
- Personalización de temas (modo oscuro).
- Widget de escritorio para mostrar novelas favoritas.

---

## Optimización Implementada

### **1. Optimización del Uso de la Memoria**
#### Acciones:
- Uso eficiente de `Bitmap` y reciclaje de vistas en `NovelAdapter`.
  - **Archivo:** `NovelAdapter.java`
  - **Código relevante:**
    ```java
    holder.itemView.setOnClickListener(v -> onNovelClickListener.onNovelClick(currentNovel));
    ```
- Reutilización de vistas en `RecyclerView` para evitar fugas de memoria.

- Identificación de problemas de memoria con **Memory Profiler** y optimización de cargas innecesarias.

---

### **2. Mejora del Rendimiento de la Red**
#### Acciones:
- Uso de **Network Profiler** para optimizar solicitudes de red.
- Implementación de:
  - **Compresión de datos** al recuperar información desde Firebase.
    - **Archivo:** `NovelViewModel.java`
    - **Código relevante:**
      ```java
      db.collection("novelas")
        .orderBy("title")
        .limit(20)
        .get();
      ```
  - Sincronización periódica de datos con intervalos controlados:
    - **Frecuencia normal:** 15 minutos.
    - **Frecuencia en batería baja:** 30 minutos.
    - **Archivo:** `NovelViewModel.java`, `ReviewViewModel.java`, `FavoritesActivity.java`.

---

### **3. Optimización del Uso de la Batería**
#### Acciones:
- Ajuste dinámico del brillo de pantalla según el nivel de batería:
  - **Archivo:** `PantallaPrincipalActivity.java`, `ReviewActivity.java`, `AddNovelActivity.java`.
  - **Código relevante:**
    ```java
    layoutParams.screenBrightness = isLowBattery ? 0.7f : WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
    getWindow().setAttributes(layoutParams);
    ```
- Monitoreo del estado de batería con **Battery Historian**:
  - Uso de `BroadcastReceiver` para cambiar comportamiento de la app en tiempo real.

- Reducción de actualizaciones en segundo plano durante batería baja:
  - **Archivo:** `ReviewViewModel.java`, `FavoritesActivity.java`.

---

## Clases y Su Función

### **1. PantallaPrincipalActivity**
Gestión del menú principal y funcionalidad de las vistas principales:
- Configuración de navegación lateral.
- Listado y filtrado de novelas.
- Monitoreo de batería y ajuste de brillo dinámico.

### **2. NovelViewModel**
Gestión de datos de novelas:
- Conexión con Firebase para recuperar y actualizar novelas.
- Implementación de sincronización periódica con frecuencia ajustable.

### **3. ReviewViewModel**
Gestión de datos de reseñas:
- Recuperación en tiempo real desde Firebase.
- Almacenamiento en SQLite para acceso offline.
- Sincronización optimizada según estado de batería.

### **4. NovelAdapter**
Adaptador para la visualización eficiente de novelas en `RecyclerView`:
- Manejo de eventos de clic en elementos.
- Optimización de memoria al reciclar vistas.

### **5. SQLiteHelper**
Gestión de base de datos SQLite:
- Operaciones CRUD para novelas y reseñas.
- Sincronización con Firebase.

### **6. NovelDetailFragment**
Fragmento para mostrar el detalle de una novela seleccionada:
- Gestión de estado de favorito.
- Navegación a añadir reseñas.

### **7. FavoritesActivity**
Visualización de novelas marcadas como favoritas:
- Sincronización periódica y dinámica con Firebase.
- Ajustes en tiempo real según batería.

### **8. SettingsActivity**
Gestión de preferencias de usuario:
- Activación/desactivación del modo oscuro.

### **9. NovelWidgetProvider**
Widget para mostrar las novelas favoritas:
- Actualización dinámica del contenido.

---

## Tecnologías Utilizadas

- **Lenguaje:** Java
- **Base de datos:** Firebase y SQLite

---

## Créditos

**Desarrollado por:** [Jaime López Díaz]  

