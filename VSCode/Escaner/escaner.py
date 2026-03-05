import tkinter as tk
from tkinter import ttk, scrolledtext, messagebox
import re


class EscanerSQLApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Escáner SQL - Tablas Dinámicas")
        self.root.geometry("900x600")

        # Estilos para las tablas
        style = ttk.Style()
        style.configure("Treeview.Heading", font=('Helvetica', 10, 'bold'))
        style.configure("Treeview", font=('Helvetica', 10), rowheight=25)

        # Palabras reservadas a ignorar en la tabla de símbolos
        self.palabras_reservadas = {
            "SELECT", "FROM", "WHERE", "AND", "OR", "NOT", "INSERT", 
            "DELETE", "UPDATE", "AS", "INNER", "JOIN", "ON"
        }

        # Componentes
        self.crear_modulo_entrada()
        self.crear_modulo_resultados()

    # MÓDULO DE ENTRADA
    def crear_modulo_entrada(self):
        frame_entrada = tk.LabelFrame(self.root, text="Módulo de Entrada (Sentencia SQL)", padx=10, pady=10)
        frame_entrada.pack(fill="x", padx=10, pady=5)

        # Área de texto
        self.txt_entrada = scrolledtext.ScrolledText(frame_entrada, width=100, height=8, font=("Consolas", 11))
        self.txt_entrada.pack(fill="both", expand=True)

        # Usé el ejemplo del ejercicio
        sql_default = (
            "SELECT ANOMBRE, CALIFICACION, TURNO\n"
            "FROM ALUMNOS, INSCRITOS, MATERIAS, CARRERAS\n"
            "WHERE MNOMBRE='PROGSIST' AND TURNO = 'TV'\n"
            "AND CNOMBRE='IDS' AND SEMESTRE='EJ2026' AND CALIFICACION >= 60"
        )
        self.txt_entrada.insert(tk.END, sql_default)

        # Botón de Análisis
        btn_analizar = tk.Button(frame_entrada, text="Analizar Consulta", command=self.modulo_analisis, 
                                 bg="#4CAF50", fg="white", font=("Arial", 12, "bold"))
        btn_analizar.pack(pady=5)

    # MÓDULO DE RESULTADOS
    def crear_modulo_resultados(self):
        frame_resultados = tk.Frame(self.root)
        frame_resultados.pack(fill="both", expand=True, padx=10, pady=5)

        # Tabla de Identificadores
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

        # Tabla de Constantes
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

    # MÓDULO DE ANÁLISIS
    def modulo_analisis(self):
        # Limpiar tablas previas
        for item in self.tree_id.get_children():
            self.tree_id.delete(item)
        for item in self.tree_const.get_children():
            self.tree_const.delete(item)

        entrada = self.txt_entrada.get("1.0", tk.END).strip()
        if not entrada:
            messagebox.showwarning("Advertencia", "El campo de entrada está vacío.")
            return

        # Comillas
        entrada = entrada.replace("’", "'").replace("‘", "'")
        lineas = entrada.split("\n")

        # Diccionarios para almacenar los tokens y evitar duplicados de ID
        datos_identificadores = {}
        datos_constantes = {}

        contador_id = 401
        contador_const = 600

        patron = re.compile(r"('[^']*')|(\b\d+\b)|([a-zA-Z_][a-zA-Z0-9_]*)")

        for num_linea, contenido_linea in enumerate(lineas, start=1):
            iterador = patron.finditer(contenido_linea)
            
            for match in iterador:
                token = match.group()
                
                # Clasificación
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

        # Llenar las tablas visuales
        self.llenar_tabla_visual(self.tree_id, datos_identificadores)
        self.llenar_tabla_visual(self.tree_const, datos_constantes)

    def llenar_tabla_visual(self, tree, datos):
        # Lista para ordenar por ID antes de mostrar
        lista_ordenada = sorted(datos.items(), key=lambda x: x[1]['id'])
        
        for token, info in lista_ordenada:
            lineas_str = ", ".join(map(str, sorted(info['lineas'])))
            tree.insert("", "end", values=(token, info['id'], lineas_str))

if __name__ == "__main__":
    root = tk.Tk()
    app = EscanerSQLApp(root)
    root.mainloop()