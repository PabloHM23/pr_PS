def programa_analisis_sintactico():
    # Representación de la Tabla Sintáctica (R4. Tabla Sintáctica del DML)
    # Formato: { regla: { token: contenido, ... }, ... }
    tabla_sintactica = {
        300: {10: "10 301 11 306 310"},
        301: {4: "302", 72: "72"},
        302: {4: "304 303"},
        303: {11: "99", 50: "50 302", 199: "99"},
        304: {4: "4 305"},
        305: {8: "99", 11: "99", 13: "99", 14: "99", 15: "99", 50: "99", 51: "51 4", 53: "99", 199: "99"},
        306: {4: "308 307"},
        307: {12: "99", 50: "50 306", 53: "99", 199: "99"},
        308: {4: "4 309"},
        309: {4: "4", 12: "99", 50: "99", 53: "99", 199: "99"},
        310: {12: "12 311", 53: "99", 199: "99"},
        311: {4: "313 312"},
        312: {14: "317 311", 15: "317 311", 53: "99", 199: "99"},
        313: {4: "304 314"},
        314: {8: "315 316", 13: "13 52 300 53"},
        315: {8: "8"},
        316: {4: "304", 54: "54 318 54", 61: "319"},
        317: {14: "14", 15: "15"},
        318: {62: "62"},
        319: {61: "61"}
    }

    print("--- Administrador de Tabla Sintáctica DML ---")
    
    # Módulo de Entrada
    try:
        entrada = input("Ingrese el número de token (1-99): ")
        token_buscado = int(entrada)
        
        if not (1 <= token_buscado <= 99):
            print("Aviso: El token debe estar entre 1 y 99.")
            # Continuamos de todos modos para validar la lógica
    except ValueError:
        print("Error: Por favor, ingrese un valor numérico válido.")
        return

    # Módulo de Análisis
    reglas_asociadas = []
    
    # Recorremos la tabla para ver en qué reglas (filas) aparece el token (columna)
    for regla, tokens_dict in tabla_sintactica.items():
        if token_buscado in tokens_dict:
            reglas_asociadas.append(str(regla))

    # Módulo de Resultados
    print("\n--- Resultados ---")
    print(f"Token: {token_buscado}")
    
    if reglas_asociadas:
        print(f"Reglas asociadas: {', '.join(reglas_asociadas)}")
    else:
        print("No tiene reglas asociadas.")

# Ejecución del programa
if __name__ == "__main__":
    programa_analisis_sintactico()