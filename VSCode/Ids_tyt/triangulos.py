def clasificar_triangulo(a, b, c):
    if a <= 0 or b <= 0 or c <= 0:
        return "No es triángulo"

    if a + b <= c or a + c <= b or b + c <= a:
        return "No es triángulo"

    if a == b == c:
        return "Equilátero"
    if a == b or a == c or b == c:
        return "Isósceles"
    return "Escaleno"


if __name__ == "__main__":
    try:
        lados = input("Introduce 3 lados separados por espacios: ").strip().split()
        #if len(lados) != 3:
        #    raise ValueError("Se requieren exactamente 3 números")

        a, b, c = map(float, lados)
        tipo = clasificar_triangulo(a, b, c)
        print(f"Tipo de triángulo: {tipo}")
    except ValueError as e:
        print(f"Entrada no válida: {e}")
