import tkinter as tk
from tkinter import ttk, scrolledtext, messagebox
import re


class EscanerSQLApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Escáner SQL y Analizador Sintáctico")
        self.root.geometry("950x650")
        style = ttk.Style()
        style.configure("Treeview.Heading", font=('Helvetica', 10, 'bold'))
        style.configure("Treeview", font=('Helvetica', 10), rowheight=25)

        self.palabras_reservadas = {
            "SELECT", "FROM", "WHERE", "AND", "OR", "NOT", "INSERT", 
            "DELETE", "UPDATE", "AS", "INNER", "JOIN", "ON"
        }
        
        self.ts = {
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
        for regla, tokens_validos in self.ts.items():
            if token_ingresado in tokens_validos:
                reglas_asociadas.append(str(regla))
        return reglas_asociadas

        self.notebook = ttk.Notebook(self.root)
        self.notebook.pack(fill="both", expand=True, padx=5, pady=5)

        self.tab_escaner = ttk.Frame(self.notebook)
        self.tab_sintactico = ttk.Frame(self.notebook)

        #self.notebook.add(self.tab_escaner, text="Escáner Léxico")
        self.notebook.add(self.tab_sintactico, text="Tabla Sintáctica")

        self.crear_modulo_escaner()
        self.crear_modulo_sintactico()

    def crear_modulo_escaner(self):
        frame_entrada = tk.LabelFrame(self.tab_escaner, text="Módulo de Entrada (Sentencia SQL)", padx=10, pady=10)
        frame_entrada.pack(fill="x", padx=10, pady=5)

        self.txt_entrada = scrolledtext.ScrolledText(frame_entrada, width=100, height=8, font=("Consolas", 11))
        self.txt_entrada.pack(fill="both", expand=True)

        sql_default = (
            "SELECT ANOMBRE, CALIFICACION, TURNO\n"
            "FROM ALUMNOS, INSCRITOS, MATERIAS, CARRERAS\n"
            "WHERE MNOMBRE='PROGSIST' AND TURNO = 'TV'\n"
            "AND CNOMBRE='IDS' AND SEMESTRE='EJ2026' AND CALIFICACION >= 60"
        )
        self.txt_entrada.insert(tk.END, sql_default)

        btn_analizar = tk.Button(frame_entrada, text="Analizar Consulta", command=self.modulo_analisis, 
                                 bg="#4CAF50", fg="white", font=("Arial", 12, "bold"))
        btn_analizar.pack(pady=5)

        frame_resultados = tk.Frame(self.tab_escaner)
        frame_resultados.pack(fill="both", expand=True, padx=10, pady=5)

        frame_id = tk.LabelFrame(frame_resultados, text="Tabla de Identificadores (400+)")
        frame_id.pack(side="left", fill="both", expand=True, padx=5)

        cols = ("Token", "Valor (ID)", "Línea")
        self.tree_id = ttk.Treeview(frame_id, columns=cols, show='headings')
        for col in cols:
            self.tree_id.heading(col, text=col)
            self.tree_id.column(col, width=100, anchor="center")
        
        scroll_id = ttk.Scrollbar(frame_id, orient="vertical", command=self.tree_id.yview)
        self.tree_id.configure(yscroll=scroll_id.set)
        self.tree_id.pack(side="left", fill="both", expand=True)
        scroll_id.pack(side="right", fill="y")

        frame_const = tk.LabelFrame(frame_resultados, text="Tabla de Constantes (600+)")
        frame_const.pack(side="right", fill="both", expand=True, padx=5)

        self.tree_const = ttk.Treeview(frame_const, columns=cols, show='headings')
        for col in cols:
            self.tree_const.heading(col, text=col)
            self.tree_const.column(col, width=100, anchor="center")

        scroll_const = ttk.Scrollbar(frame_const, orient="vertical", command=self.tree_const.yview)
        self.tree_const.configure(yscroll=scroll_const.set)
        self.tree_const.pack(side="left", fill="both", expand=True)
        scroll_const.pack(side="right", fill="y")

    def modulo_analisis(self):
        for item in self.tree_id.get_children():
            self.tree_id.delete(item)
        for item in self.tree_const.get_children():
            self.tree_const.delete(item)

        entrada = self.txt_entrada.get("1.0", tk.END).strip()
        if not entrada:
            messagebox.showwarning("Advertencia", "El campo de entrada está vacío.")
            return

        entrada = entrada.replace("’", "'").replace("‘", "'")
        lineas = entrada.split("\n")

        datos_identificadores = {}
        datos_constantes = {}
        contador_id = 401
        contador_const = 600

        patron = re.compile(r"('[^']*')|(\b\d+\b)|([a-zA-Z_][a-zA-Z0-9_]*)")

        for num_linea, contenido_linea in enumerate(lineas, start=1):
            iterador = patron.finditer(contenido_linea)
            for match in iterador:
                token = match.group()
                es_cadena = match.group(1) is not None
                es_numero = match.group(2) is not None
                es_palabra = match.group(3) is not None

                if es_cadena:
                    valor_limpio = token.replace("'", "")
                    if valor_limpio not in datos_constantes:
                        datos_constantes[valor_limpio] = {'id': contador_const, 'lineas': {num_linea}}
                        contador_const += 1
                    else:
                        datos_constantes[valor_limpio]['lineas'].add(num_linea)

                elif es_numero:
                    if token not in datos_constantes:
                        datos_constantes[token] = {'id': contador_const, 'lineas': {num_linea}}
                        contador_const += 1
                    else:
                        datos_constantes[token]['lineas'].add(num_linea)

                elif es_palabra:
                    if token.upper() not in self.palabras_reservadas:
                        if token not in datos_identificadores:
                            datos_identificadores[token] = {'id': contador_id, 'lineas': {num_linea}}
                            contador_id += 1
                        else:
                            datos_identificadores[token]['lineas'].add(num_linea)

        self.llenar_tabla_visual(self.tree_id, datos_identificadores)
        self.llenar_tabla_visual(self.tree_const, datos_constantes)

    def llenar_tabla_visual(self, tree, datos):
        lista_ordenada = sorted(datos.items(), key=lambda x: x[1]['id'])
        for token, info in lista_ordenada:
            lineas_str = ", ".join(map(str, sorted(info['lineas'])))
            tree.insert("", "end", values=(token, info['id'], lineas_str))

    def crear_modulo_sintactico(self):
        frame_entrada = tk.LabelFrame(self.tab_sintactico, text="Módulo de Entrada", padx=10, pady=10)
        frame_entrada.pack(fill="x", padx=10, pady=5)

        tk.Label(frame_entrada, text="Ingrese un Token:", font=("Helvetica", 11)).pack(side="left", padx=5)
        
        self.entry_token = tk.Entry(frame_entrada, font=("Helvetica", 12), width=10)
        self.entry_token.pack(side="left", padx=5)
        self.entry_token.bind("<Return>", lambda event: self.modulo_analisis_sintactico())

        btn_analizar_token = tk.Button(frame_entrada, text="Analizar Token", command=self.modulo_analisis_sintactico,
                                       bg="#2196F3", fg="white", font=("Arial", 11, "bold"))
        btn_analizar_token.pack(side="left", padx=15)

        btn_limpiar = tk.Button(frame_entrada, text="Limpiar Resultados", command=self.limpiar_tabla_sintactica,
                                font=("Arial", 10))
        btn_limpiar.pack(side="right", padx=5)

        frame_resultados = tk.LabelFrame(self.tab_sintactico, text="Módulo de Resultados", padx=10, pady=10)
        frame_resultados.pack(fill="both", expand=True, padx=10, pady=5)

        cols = ("Token", "Reglas asociadas")
        self.tree_sintactico = ttk.Treeview(frame_resultados, columns=cols, show='headings')
        self.tree_sintactico.heading("Token", text="Token")
        self.tree_sintactico.heading("Reglas asociadas", text="Reglas asociadas")
        
        self.tree_sintactico.column("Token", width=100, anchor="center")
        self.tree_sintactico.column("Reglas asociadas", width=600, anchor="w")

        scroll_sintactico = ttk.Scrollbar(frame_resultados, orient="vertical", command=self.tree_sintactico.yview)
        self.tree_sintactico.configure(yscroll=scroll_sintactico.set)

        self.tree_sintactico.pack(side="left", fill="both", expand=True)
        scroll_sintactico.pack(side="right", fill="y")

    def modulo_analisis_sintactico(self):
        token_ingresado = self.entry_token.get().strip()
        
        #lógica 2

        if not token_ingresado.isdigit():
            messagebox.showerror("Error de Entrada", "Por favor, ingresa únicamente un valor numérico.")
            return

        token = int(token_ingresado)

        if not (1 <= token <= 99):
            messagebox.showwarning("Advertencia", "El token debe estar en el rango de 1 a 99.")
            return


        reglas = self.modulo_analisis_sintactico(token)


        if reglas:
            self.resultado_var.set(", ".join(reglas))
        else:
            self.resultado_var.set("Ninguna regla asociada (Celda vacía).")


        self.tree_sintactico.insert("", "end", values=(token_ingresado, reglas_str))
        
        self.entry_token.delete(0, tk.END)

    def limpiar_tabla_sintactica(self):
        for item in self.tree_sintactico.get_children():
            self.tree_sintactico.delete(item)


if __name__ == "__main__":
    root = tk.Tk()
    app = EscanerSQLApp(root)
    root.mainloop()