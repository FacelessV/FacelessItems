# FacelessItems
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![Spigot API](https://img.shields.io/badge/Spigot--API-1.20+-red.svg)

Un framework avanzado para Spigot/Paper que permite la creación de ítems, armas, herramientas y armaduras personalizadas con un sistema modular de efectos, condiciones, cooldowns y bonificaciones de set, todo a través de archivos YAML.

---
## Tabla de Contenidos

* [Sobre el Proyecto](#-sobre-el-proyecto)
* [Características Principales](#-características-principales)
* [Instalación](#-instalación)
* [Configuración de un Ítem](#-configuración-de-un-ítem)
* [Configuración de Sets](#-configuración-de-sets)
* [Comandos y Permisos](#-comandos-y-permisos)
* [API para Desarrolladores](#-api-para-desarrolladores)
* [Hoja de Ruta](#-hoja-de-ruta)
* [Contribuciones](#-contribuciones)
* [Licencia](#-licencia)

---
## 📖 Sobre el Proyecto

**FacelessItems** nace de la necesidad de un sistema de ítems que vaya más allá de los encantamientos de vainilla. Permite a los administradores de servidores y diseñadores de juegos crear equipamiento con comportamientos únicos y complejos, desde espadas que lanzan rayos en cadena hasta sets de armadura que otorgan habilidades pasivas y reactivas, transformando la experiencia de juego en un verdadero RPG.

---
## ✨ Características Principales

* **Creación por YAML**: Define todos los aspectos de tus ítems en archivos `.yml` individuales y fáciles de gestionar.
* **Sistema de Efectos Avanzado**: Más de 20 efectos pre-programados, incluyendo `EXPLOSION`, `CHAIN_LIGHTNING`, `VEIN_MINE`, `MULTI_SHOT`, `DASH`, `PULL`, y efectos modificadores como `SMELT` y `REPLANT`.
* **Motor de Condiciones Poderoso**: Controla con precisión cuándo se activan los efectos. Usa listas blancas/negras para:
  * Tipos de Mobs, Causa del Daño, Tipos de Bloques, Razón de Aparición, Mundos, Hora del día y Probabilidad (`chance`).
* **Triggers Múltiples**: Activa efectos en respuesta a una gran variedad de eventos: `on_hit`, `on_use`, `on_damage_taken`, `on_kill`, `on_arrow_hit`, `on_bow_shoot`, `on_mine`, y más.
* **Sets de Armadura**: Define sets de armadura que otorgan bonificaciones pasivas (`passive_effects`) y habilidades por trigger (`triggered_effects`) al equipar múltiples piezas.
* **Efectos Pasivos**: Crea ítems que otorgan bonus constantes (ej: `PERMANENT_POTION`, `DAMAGE_MULTIPLIER`) solo por tenerlos equipados o en la mano.
* **Sistema de Cooldowns**: Balancea las habilidades con enfriamientos por jugador, ya sean individuales o compartidos (`cooldown_id`), con feedback visual en la Action Bar.
* **API para Desarrolladores**: Permite que otros plugins interactúen de forma segura con tu sistema de ítems.
* **GUI de Administrador**: Un menú interactivo (`/fi list`) para ver y obtener todos los ítems personalizados.
* **Totalmente Configurable**: Desde los mensajes (con placeholders) hasta las rarezas de los ítems.

---
## ⚙️ Instalación

1.  Descarga la última versión de `FacelessItems.jar` desde la [página de Releases](https://github.com/TU_USUARIO/TU_REPOSITORIO/releases).
2.  Coloca el archivo `.jar` en la carpeta `plugins/` de tu servidor de Spigot/Paper.
3.  Inicia el servidor. Se generarán las carpetas y archivos de configuración (`config.yml`, `messages.yml`, `sets.yml`, etc.).
4.  ¡Configura tus ítems y a jugar!

---
## 🔧 Configuración de un Ítem

La creación de ítems se realiza en la carpeta `plugins/FacelessItems/items/`.

#### Ejemplo: `pico_dragon.yml`
```yaml
key: pico_dragon
material: NETHERITE_PICKAXE
display-name: '&c&lPico de Aliento de Dragón'
lore:
  - '&7Forjado con una escama de dragón.'
  - ''
  - '&6Habilidad Pasiva: &lFuria Minera'
  - '&e▪ &7Rompe un área de &f3x3&7 de forma pasiva.'
  - '&e▪ &7Funde los minerales de &fHierro, Cobre y Oro&7.'
  - ''
  - '&6Habilidad Activa: &lAliento de Dragón &7(Clic Derecho)'
  - '&e▪ &7Perfora un túnel de &f3x3x5&7 a distancia.'
  - '&8(Enfriamiento: 20 segundos)'
rarity: MYTHIC
effects:
  on_mine:
    - type: BREAK_BLOCK
      radius: 1
      layers: 1
      mineable_blocks: [STONE, IRON_ORE, DEEPSLATE_IRON_ORE, GOLD_ORE, ...]
    - type: SMELT
      drop_experience: true
      conditions:
        blocks: [IRON_ORE, DEEPSLATE_IRON_ORE, GOLD_ORE, ...]
  on_use:
    - type: CHAIN
      delay: 0
      cooldown: 20
      target: BLOCK_IN_SIGHT
      effects:
        - type: BREAK_BLOCK
          radius: 1
          layers: 5
          range: 20
          mineable_blocks: [STONE, IRON_ORE, ...]
        - type: SOUND
          sound_effect: ENTITY_ENDER_DRAGON_GROWL
          range: 20