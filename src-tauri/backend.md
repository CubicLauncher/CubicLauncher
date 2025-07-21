# Backend del proyecto CubicLauncher

Este markdown trae explicaciones de como estructure el backend del launcher en caso de colaboraciones.

## Estructura de archivos.

> [!CAUTION]
>
> Cubic no utiliza una estructura de archivos como el resto de launchers.
>
> Debido a que Cubic es un launcher hecho principalmente para instancias organiza todo en un directorio principal de obviamente instancias y otro de recursos compartidos como versiones de juego.
>
> Eso para tener eficiencia y evitar redescargar versiones del juego lo que tomaria muchos recursos.

```
|
|----Settings/
|     |-config.json (Puede variar si cambio a msgpack)/
|
|
|----Instances/
|    |-Foo/
|       |-(Archivos variados como instance.json)
|       |-Minecraft/
|           |-(Aca se guardan solo las cosas de la instancia como mods, resouce packs y eso.)
|
|----Shared/
|     |-(Natives/Assets/Versions)
|
```


> Ultima modificacion [21/07/25] por Santiagolxx (falta terminar esto.)
