# FacelessItems
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![Spigot](https://img.shields.io/badge/Spigot--API-1.20+-red.svg)

Un plugin avanzado de Spigot/Paper para crear ítems personalizados con un sistema modular de efectos, condiciones y cooldowns a través de archivos YAML.

---
## Tabla de Contenidos

* [Sobre el Proyecto](#-sobre-el-proyecto)
* [Características](#-características)
* [Instalación](#-instalación)
* [Uso y Configuración](#-uso-y-configuración)
* [Comandos y Permisos](#-comandos-y-permisos)
* [Hoja de Ruta](#-hoja-de-ruta)
* [Contribuciones](#-contribuciones)
* [Licencia](#-licencia)

---
## 📖 Sobre el Proyecto

**FacelessItems** nace de la necesidad de un sistema de ítems personalizados que sea a la vez fácil de usar para los administradores de servidores y extremadamente potente para los desarrolladores. En lugar de estar limitado a encantamientos básicos, este plugin permite crear habilidades complejas que reaccionan al mundo del juego de maneras dinámicas e inteligentes.

---
## ✨ Características

* **Creación Totalmente por YAML**: Define ítems en archivos `.yml` individuales y fáciles de gestionar.
* **Sistema de Efectos Avanzado**: Asigna múltiples efectos, desde `DAMAGE` y `HEAL` hasta `EXPLOSION`, `CHAIN_LIGHTNING` o `SHADOW_CLONE`.
* **Motor de Condiciones Poderoso**: Controla con precisión cuándo se activan los efectos usando listas blancas/negras para:
    * Tipos de Mobs (`target_mobs`)
    * Causa del Daño (`damage_cause`)
    * Tipos de Bloques (`blocks`)
    * Razón de Aparición (`spawn_reason`)
* **Sistema de Cooldowns**: Balancea las habilidades con enfriamientos por jugador, ya sean individuales o compartidos (`cooldown_id`).
* **Efectos en Cadena (`CHAIN`)**: Crea secuencias de efectos con retrasos para habilidades cinemáticas.
* **Rarezas Personalizables**: Define tus propias rarezas con colores y etiquetas de lore.
* **Integración con AuraSkills**: Añade estadísticas de AuraSkills directamente a tus ítems.

---
## ⚙️ Instalación

1.  Descarga la última versión de `FacelessItems.jar` desde la [página de Releases](https://github.com/TU_USUARIO/TU_REPOSITORIO/releases).
2.  Coloca el archivo `.jar` en la carpeta `plugins/` de tu servidor de Spigot/Paper (versión 1.20+ recomendada).
3.  Inicia el servidor. Se generarán las carpetas y archivos de configuración por defecto.
4.  ¡Configura tus ítems y a jugar!

---
## 🔧 Uso y Configuración

La creación de ítems se realiza en la carpeta `plugins/FacelessItems/items/`. Cada archivo `.yml` representa un nuevo ítem.

#### Ejemplo: `coraza_volcanica.yml`
Este ejemplo muestra una armadura que reacciona con una secuencia de efectos al ser golpeado por ciertos mobs.

```yaml
# Identificador único del ítem
key: coraza_volcanica

# Material de Minecraft
material: NETHERITE_CHESTPLATE

# Nombre del ítem
display-name: '&cCoraza Volcánica de Represalia'

# Descripción
lore:
  - '&7Forjada en el corazón de un volcán.'
  - '&7Devuelve el dolor con furia ígnea.'

# Rareza del ítem
rarity: LEGENDARY

# Propiedades especiales
properties:
  unbreakable: true

# Efectos del ítem
effects:
  # Trigger que se activa al recibir daño
  on_damage_taken:
    - type: CHAIN
      delay: 10 # 10 ticks (0.5s) de retraso entre cada paso
      cooldown: 5 # La secuencia completa tiene un cooldown de 5 segundos
      conditions:
        target_mobs: [ZOMBIE, SKELETON, HUSK, STRAY]
        damage_cause: [ENTITY_ATTACK, PROJECTILE]
      
      # Lista de efectos a ejecutar en secuencia
      effects:
        - { type: SOUND, sound_effect: BLOCK_FURNACE_FIRE_CRACKLE, target: PLAYER }
        - { type: MESSAGE, text: "&6La coraza crepita con furia..." }
        - { type: POTION, potion_type: GLOWING, duration: 30, target: ENTITY }
        - { type: EXPLOSION, power: 2.5, set_fire: true, break_blocks: false, target: PLAYER }