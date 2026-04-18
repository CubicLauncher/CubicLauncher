# TODO / Notas

## General
- [x] Hacer settings  
- [x] Quitar consola  
- [x] Que las instances se manejen con un actor que sincroniza el estado en la memoria RAM con el FS  
- [x] Instances mejor estructuradas  
- [x] Sincronización cada 30 segundos  

## Backend
- [ ] Posible *yield* en comandos  
  - Puede que se esté bloqueando el backend y no reciba más llamadas por mala gestión de threads  
- [ ] Remover cosas innecesarias en el backend (Mutex, Locks, etc.)  

## UI
- [x] Poner banner  

## Importante
- [ ] Investigar Proton  
  - Debido a que actualizaron a Vulkan cierta librería, ya no funciona  
  - Chequear `log1.log` que está en este mismo directorio  