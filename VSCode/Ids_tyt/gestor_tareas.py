"""
Nombre del programa: Gestor de Tareas
Descripción: Este programa permite al usuario agregar, mostrar y eliminar tareas pendientes.
Autor: Pablo Hernández.
Fecha: 2024-06-01
"""
"""Aqui se define la clase GestorTareas con sus métodos para agregar, mostrar y eliminar tareas"""
class GestorTareas:
    def __init__(self):
        self.tareas = []
    
    def agregar_tarea(self, tarea):
        self.tareas.append(tarea)
        print("Tarea agregada")
    
    def mostrar_tareas(self):
        print("Tareas pendientes:")
        if not self.tareas:
            print("No hay tareas pendientes.")
        else:
            for i, tarea in enumerate(self.tareas, 1):
                print(f"{i}. {tarea}")
    
    def eliminar_tarea(self, indice):
        if 1 <= indice <= len(self.tareas):
            self.tareas.pop(indice - 1)
            print("Tarea eliminada")
        else:
            print("Índice inválido")
            
""" Aquí se define la lógica para agregar, mostrar y eliminar tareas."""

def main():
    gestor = GestorTareas()
    
    print("Bienvenido al gestor de tareas")
    
    while True:
        print("\n1. Agregar tarea")
        print("2. Mostrar tareas")
        print("3. Eliminar tarea")
        print("4. Salir")
        
        try:
            opcion = int(input("Seleccione una opción: "))
        except ValueError:
            print("Por favor, ingrese un número válido")
            continue
        
        match opcion:
            case 1:
                tarea = input("Ingrese la tarea: ")
                gestor.agregar_tarea(tarea)
            case 2:
                gestor.mostrar_tareas()
            case 3:
                try:
                    indice = int(input("Ingrese el número de la tarea a eliminar: "))
                    gestor.eliminar_tarea(indice)
                except ValueError:
                    print("Por favor, ingrese un número válido")
            case 4:
                print("Saliendo del gestor de tareas...")
                break
            case _:
                print("Opción inválida, por favor intente nuevamente.")


if __name__ == "__main__":
    main()