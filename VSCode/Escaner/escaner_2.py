import tkinter as tk
from tkinter import ttk, scrolledtext, messagebox
import re


TS = {
    300: [10, 11, 12, 13, 14, 15],
    301: [4, 72],
    302: [4],
    303: [11, 50, 199],
    304: [4],
    305: [8, 11, 13, 14, 15, 50, 51, 53, 199],
    306: [4],
    307: [12, 50, 53, 199],
    308: [4],
    309: [4, 12, 50, 53, 199],
    310: [12, 53, 199],
    311: [4],
    312: [14, 15, 53, 199],
    313: [4],
    314: [8, 13],
    315: [8],
    316: [4, 54, 61],
    317: [14, 15],
    318: [62],
    319: [61]
}

def modulo_analisis(token_ingresado):
    reglas_asociadas = []
    for regla, tokens_validos in TS.items():
        if token_ingresado in tokens_validos:
            reglas_asociadas.append(str(regla))
    return reglas_asociadas


class EscanerSQLApp:
    def _init_(self, root):
        self.root = root
        self.root.title("Tabla Sintáctica")
        self.root.geometry("450x300")
        self.root.configure(padx=20, pady=20, bg="#f4f4f9")


        tk.Label(root, text="Módulo de Entrada", font=("Arial", 12, "bold"), bg="#f4f4f9").pack(anchor="w")
        tk.Label(root, text="Ingresa un Token (numérico entre 1 y 99):", font=("Arial", 10), bg="#f4f4f9").pack(
            anchor="w", pady=(10, 5))

        self.entrada_token = tk.Entry(root, font=("Arial", 12), justify="center")
        self.entrada_token.pack(fill="x", pady=5)


        self.btn_buscar = tk.Button(root, text="Analizar Token", bg="#2196F3", fg="white", font=("Arial", 11, "bold"),
                                    command=self.procesar)
        self.btn_buscar.pack(pady=15)


        tk.Label(root, text="Módulo de Resultados", font=("Arial", 12, "bold"), bg="#f4f4f9").pack(anchor="w",
                                                                                                   pady=(10, 0))
        tk.Label(root, text="Reglas Asociadas:", font=("Arial", 10), bg="#f4f4f9").pack(anchor="w")

        self.resultado_var = tk.StringVar()
        self.resultado_var.set("Esperando consulta...")
        self.lbl_resultado = tk.Label(root, textvariable=self.resultado_var, font=("Courier", 12, "bold"), fg="#D32F2F",
                                      wraplength=400, justify="left", bg="#f4f4f9")
        self.lbl_resultado.pack(anchor="w", pady=5)

    def procesar(self):
        entrada = self.entrada_token.get().strip()

        if not entrada.isdigit():
            messagebox.showerror("Error de Entrada", "Por favor, ingresa únicamente un valor numérico.")
            return

        token = int(entrada)

        if not (1 <= token <= 99):
            messagebox.showwarning("Advertencia", "El token debe estar en el rango de 1 a 99.")
            return


        reglas = modulo_analisis(token)


        if reglas:
            self.resultado_var.set(", ".join(reglas))
        else:
            self.resultado_var.set("Ninguna regla asociada (Celda vacía).")


if __name__ == "__main__":
    root = tk.Tk()
    app = EscanerSQLApp(root)
    root.mainloop()