"""Programa interactivo con un número ingresado por el usuario"""
"""Autor: Pablo Hernández."""
"""Fecha: 2024-06-01"""

"""Opción 1 del Menú"""
def tabla_multiplicar(numero):
    
    print(f"\n--- Tabla de multiplicar del {numero} ---")
    
    for i in range(1, 11):
        resultado = numero * i
        print(f"{numero} x {i} = {resultado}")

"""Opción 2 del Menú"""
def es_par_o_impar(numero):
    if numero % 2 == 0:
        print(f"\nEl número {numero} es PAR")
    else:
        print(f"\nEl número {numero} es IMPAR")

"""Opción 3 del Menú"""
def fibonacci(numero):
    
    print(f"\n--- Secuencia de Fibonacci hasta {numero} ---")
    
    if numero < 0:
        print("Por favor ingresa un número positivo")
        return
    
    a, b = 0, 1
    
    if numero == 0:
        print(0)
        return
    
    print("Secuencia:", end=" ")
    while a <= numero:
        print(a, end=" ")
        a, b = b, a + b
    print()

def main():
    """Función principal del programa"""
    try:
        numero = int(input("Ingresa un número entero: "))
        
        while True:
            print("\n--- MENÚ DE OPCIONES ---")
            print("1. Ver tabla de multiplicar")
            print("2. Verificar si es par o impar")
            print("3. Ver secuencia de Fibonacci")
            print("4. Todas las opciones")
            print("5. Salir")
            
            opcion = input("Selecciona una opción (1-5): ")
            
            if opcion == "1":
                tabla_multiplicar(numero)
            elif opcion == "2":
                es_par_o_impar(numero)
            elif opcion == "3":
                fibonacci(numero)
            elif opcion == "4":
                tabla_multiplicar(numero)
                es_par_o_impar(numero)
                fibonacci(numero)
            elif opcion == "5":
                print("¡Hasta luego!")
                break
            else:
                print("Opción no válida. Por favor selecciona 1-5")
                
    except ValueError:
        print("Error: Debes ingresar un número entero válido")

# Ejecutar el programa
if __name__ == "__main__":
    main()