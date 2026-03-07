import tkinter as tk
from tkinter import messagebox

class AnalizadorSintacticoDML:
    def __init__(self, root):
        self.root = root
        self.root.title("Analizador de Tabla Sintáctica DML")
        self.root.geometry("700x750")

        self.tabla = {
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

        self.crear_interfaz()

    def crear_interfaz(self):
        tk.Label(self.root, text="Módulo de Entrada", font=('Arial', 12, 'bold')).pack(pady=10)
        tk.Label(self.root, text="Ingrese el valor del Token (1-99):").pack()
        
        self.entry_token = tk.Entry(self.root, justify='center')
        self.entry_token.pack(pady=5)
        
        self.btn_analizar = tk.Button(self.root, text="Analizar Token", command=self.modulo_analisis, bg="#4CAF50", fg="white")
        self.btn_analizar.pack(pady=10)

        tk.Label(self.root, text="Módulo de Resultados", font=('Arial', 12, 'bold')).pack(pady=10)
        self.txt_resultados = tk.Text(self.root, height=10, width=40, state='disabled', bg="#f0f0f0")
        self.txt_resultados.pack(padx=20, pady=5)

    def modulo_analisis(self):
        valor = self.entry_token.get()
        
        try:
            token_buscado = int(valor)
            reglas_encontradas = []

            for regla, columnas in self.tabla.items():
                if token_buscado in columnas:
                    reglas_encontradas.append(str(regla))
            
            self.mostrar_resultados(token_buscado, reglas_encontradas)

        except ValueError:
            messagebox.showerror("Error", "Por favor, ingrese un número entero válido.")

    def mostrar_resultados(self, token, lista_reglas):
        self.txt_resultados.config(state='normal')
        self.txt_resultados.delete('1.0', tk.END)
        
        res_texto = f"Token analizado: {token}\n"
        res_texto += "\n"
        
        if lista_reglas:
            res_texto += "Reglas asociadas:\n"
            res_texto += ", ".join(lista_reglas)
        else:
            res_texto += "No tiene reglas asociadas."
            
        self.txt_resultados.insert(tk.END, res_texto)
        self.txt_resultados.config(state='disabled')

if __name__ == "__main__":
    root = tk.Tk()
    app = AnalizadorSintacticoDML(root)
    root.mainloop()