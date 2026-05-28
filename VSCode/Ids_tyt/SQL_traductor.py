import tkinter as tk
from tkinter import ttk

PALABRAS_RESERVADAS = {
    'SELECT': 10, 'FROM': 11, 'WHERE': 12, 'IN': 13, 'AND': 14, 'OR': 15,
    'CREATE': 16, 'TABLE': 17, 'CHAR': 18, 'NUMERIC': 19, 'NOT': 20, 'NULL': 21,
    'CONSTRAINT': 22, 'KEY': 23, 'PRIMARY': 24, 'FOREIGN': 25, 'REFERENCES': 26,
    'INSERT': 27, 'INTO': 28, 'VALUES': 29, 'DISTINCT': 30
}

DELIMITADORES = {',': 50, '.': 51, '(': 52, ')': 53, "'": 54, ';': 55}
OPERADORES    = {'+': 70, '-': 71, '*': 72, '/': 73}
RELACIONALES  = {'>=': 84, '<=': 85, '>': 81, '<': 82, '=': 83}

BD_INSCRITOS_TABLAS = [
    {'no': 1, 'nombre': 'DEPARTAMENTOS', 'no_atributos': 2, 'no_restricciones': 1},
    {'no': 2, 'nombre': 'CARRERAS',      'no_atributos': 5, 'no_restricciones': 2},
    {'no': 3, 'nombre': 'ALUMNOS',       'no_atributos': 5, 'no_restricciones': 2},
    {'no': 4, 'nombre': 'MATERIAS',      'no_atributos': 4, 'no_restricciones': 2},
    {'no': 5, 'nombre': 'PROFESORES',    'no_atributos': 7, 'no_restricciones': 2},
    {'no': 6, 'nombre': 'INSCRITOS',     'no_atributos': 7, 'no_restricciones': 4},
]

BD_INSCRITOS_ATRIBUTOS = [
    {'no_tabla': 1, 'no_atr': 1,  'nombre': 'D#',            'tipo': 'CHAR',    'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 1, 'no_atr': 2,  'nombre': 'DNOMBRE',        'tipo': 'CHAR',    'longitud': 6,  'no_nulo': 1},
    {'no_tabla': 2, 'no_atr': 3,  'nombre': 'C#',             'tipo': 'CHAR',    'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 2, 'no_atr': 4,  'nombre': 'CNOMBRE',        'tipo': 'CHAR',    'longitud': 3,  'no_nulo': 1},
    {'no_tabla': 2, 'no_atr': 5,  'nombre': 'VIGENCIA',       'tipo': 'CHAR',    'longitud': 4,  'no_nulo': 1},
    {'no_tabla': 2, 'no_atr': 6,  'nombre': 'SEMESTRES',      'tipo': 'NUMERIC', 'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 2, 'no_atr': 7,  'nombre': 'D#',             'tipo': 'CHAR',    'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 3, 'no_atr': 8,  'nombre': 'A#',             'tipo': 'CHAR',    'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 3, 'no_atr': 9,  'nombre': 'ANOMBRE',        'tipo': 'CHAR',    'longitud': 20, 'no_nulo': 1},
    {'no_tabla': 3, 'no_atr': 10, 'nombre': 'GENERACION',     'tipo': 'CHAR',    'longitud': 4,  'no_nulo': 1},
    {'no_tabla': 3, 'no_atr': 11, 'nombre': 'SEXO',           'tipo': 'CHAR',    'longitud': 1,  'no_nulo': 1},
    {'no_tabla': 3, 'no_atr': 12, 'nombre': 'C#',             'tipo': 'CHAR',    'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 4, 'no_atr': 13, 'nombre': 'M#',             'tipo': 'CHAR',    'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 4, 'no_atr': 14, 'nombre': 'MNOMBRE',        'tipo': 'CHAR',    'longitud': 6,  'no_nulo': 1},
    {'no_tabla': 4, 'no_atr': 15, 'nombre': 'CREDITOS',       'tipo': 'NUMERIC', 'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 4, 'no_atr': 16, 'nombre': 'C#',             'tipo': 'CHAR',    'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 5, 'no_atr': 17, 'nombre': 'P#',             'tipo': 'CHAR',    'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 5, 'no_atr': 18, 'nombre': 'PNOMBRE',        'tipo': 'CHAR',    'longitud': 20, 'no_nulo': 1},
    {'no_tabla': 5, 'no_atr': 19, 'nombre': 'EDAD',           'tipo': 'NUMERIC', 'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 5, 'no_atr': 20, 'nombre': 'SEXO',           'tipo': 'CHAR',    'longitud': 1,  'no_nulo': 1},
    {'no_tabla': 5, 'no_atr': 21, 'nombre': 'ESP',            'tipo': 'CHAR',    'longitud': 4,  'no_nulo': 1},
    {'no_tabla': 5, 'no_atr': 22, 'nombre': 'GRADO',          'tipo': 'CHAR',    'longitud': 3,  'no_nulo': 1},
    {'no_tabla': 5, 'no_atr': 23, 'nombre': 'D#',             'tipo': 'CHAR',    'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 6, 'no_atr': 24, 'nombre': 'R#',             'tipo': 'CHAR',    'longitud': 3,  'no_nulo': 1},
    {'no_tabla': 6, 'no_atr': 25, 'nombre': 'A#',             'tipo': 'CHAR',    'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 6, 'no_atr': 26, 'nombre': 'M#',             'tipo': 'CHAR',    'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 6, 'no_atr': 27, 'nombre': 'P#',             'tipo': 'CHAR',    'longitud': 2,  'no_nulo': 1},
    {'no_tabla': 6, 'no_atr': 28, 'nombre': 'TURNO',          'tipo': 'CHAR',    'longitud': 1,  'no_nulo': 1},
    {'no_tabla': 6, 'no_atr': 29, 'nombre': 'SEMESTRE',       'tipo': 'CHAR',    'longitud': 6,  'no_nulo': 1},
    {'no_tabla': 6, 'no_atr': 30, 'nombre': 'CALIFICACION',   'tipo': 'NUMERIC', 'longitud': 3,  'no_nulo': 1},
]

BD_INSCRITOS_RESTRICCIONES = [
    {'no_tabla': 1, 'no_res': 1,  'tipo': 1, 'nombre': 'PK_DEPARTAMENTOS',  'atr_asoc': 1,  'tabla_ref': '-', 'atr_ref': '-'},
    {'no_tabla': 2, 'no_res': 2,  'tipo': 1, 'nombre': 'PK_CARRERAS',       'atr_asoc': 3,  'tabla_ref': '-', 'atr_ref': '-'},
    {'no_tabla': 2, 'no_res': 3,  'tipo': 2, 'nombre': 'FK_CARRERAS',       'atr_asoc': 7,  'tabla_ref': 1,   'atr_ref': 1},
    {'no_tabla': 3, 'no_res': 4,  'tipo': 1, 'nombre': 'PK_ALUMNOS',        'atr_asoc': 8,  'tabla_ref': '-', 'atr_ref': '-'},
    {'no_tabla': 3, 'no_res': 5,  'tipo': 2, 'nombre': 'FK_ALUMNOS',        'atr_asoc': 12, 'tabla_ref': 2,   'atr_ref': 3},
    {'no_tabla': 4, 'no_res': 6,  'tipo': 1, 'nombre': 'PK_MATERIAS',       'atr_asoc': 13, 'tabla_ref': '-', 'atr_ref': '-'},
    {'no_tabla': 4, 'no_res': 7,  'tipo': 2, 'nombre': 'FK_MATERIAS',       'atr_asoc': 16, 'tabla_ref': 2,   'atr_ref': 3},
    {'no_tabla': 5, 'no_res': 8,  'tipo': 1, 'nombre': 'PK_PROFESORES',     'atr_asoc': 17, 'tabla_ref': '-', 'atr_ref': '-'},
    {'no_tabla': 5, 'no_res': 9,  'tipo': 2, 'nombre': 'FK_PROFESORES',     'atr_asoc': 23, 'tabla_ref': 1,   'atr_ref': 1},
    {'no_tabla': 6, 'no_res': 10, 'tipo': 1, 'nombre': 'PK_INSCRITOS',      'atr_asoc': 24, 'tabla_ref': '-', 'atr_ref': '-'},
    {'no_tabla': 6, 'no_res': 11, 'tipo': 2, 'nombre': 'FK_INSCRITOS_01',   'atr_asoc': 25, 'tabla_ref': 3,   'atr_ref': 8},
    {'no_tabla': 6, 'no_res': 12, 'tipo': 2, 'nombre': 'FK_INSCRITOS_02',   'atr_asoc': 26, 'tabla_ref': 4,   'atr_ref': 13},
    {'no_tabla': 6, 'no_res': 13, 'tipo': 2, 'nombre': 'FK_INSCRITOS_03',   'atr_asoc': 27, 'tabla_ref': 5,   'atr_ref': 17},
]

ts_tablas       = []
ts_atributos    = []
ts_restricciones = []

def ts_inicializar():
    global ts_tablas, ts_atributos, ts_restricciones
    ts_tablas        = []
    ts_atributos     = []
    ts_restricciones = []

def ts_actualizar():
    global ts_tablas, ts_atributos, ts_restricciones
    ts_tablas = [{**r, 'de_bd': True} for r in BD_INSCRITOS_TABLAS]
    ts_atributos = [{**r, 'de_bd': True} for r in BD_INSCRITOS_ATRIBUTOS]
    ts_restricciones = [{**r, 'de_bd': True} for r in BD_INSCRITOS_RESTRICCIONES]


def tokenizar(sql):
    tokens = []
    errores_lexicos = []
    lineas = sql.split('\n')

    for li, linea in enumerate(lineas, start=1):
        i = 0
        while i < len(linea):
            if linea[i].isspace():
                i += 1
                continue
            if linea[i:i + 2] == '--':
                break
            dos = linea[i:i + 2]
            if dos in RELACIONALES:
                tokens.append({'lexema': dos, 'tipo': 8, 'codigo': RELACIONALES[dos], 'linea': li})
                i += 2
                continue
            if linea[i] in RELACIONALES:
                tokens.append({'lexema': linea[i], 'tipo': 8, 'codigo': RELACIONALES[linea[i]], 'linea': li})
                i += 1
                continue
            if linea[i] in OPERADORES:
                tokens.append({'lexema': linea[i], 'tipo': 7, 'codigo': OPERADORES[linea[i]], 'linea': li})
                i += 1
                continue
            if linea[i] == "'":
                es_suelta = False
                if tokens:
                    ultimo = tokens[-1]
                    if ultimo['linea'] == li and ultimo['tipo'] in (4, 6):
                        es_suelta = True
                if es_suelta:
                    tokens.append({'lexema': "'", 'tipo': 5, 'codigo': 54, 'linea': li})
                    i += 1
                else:
                    j = i + 1
                    while j < len(linea) and linea[j] != "'":
                        j += 1
                    if j >= len(linea):
                        errores_lexicos.append({'tipo': 2, 'codigo': 205, 'linea': li,
                                                'descripcion': "Se esperaba Delimitador (comilla de cierre ')"})
                        valor = linea[i + 1:j]
                        tokens.append({'lexema': valor, 'tipo': 6, 'sub': 62, 'linea': li, 'es_const': True})
                        i = j
                    else:
                        valor = linea[i + 1:j]
                        tokens.append({'lexema': valor, 'tipo': 6, 'sub': 62, 'linea': li, 'es_const': True})
                        i = j + 1
                continue
            if linea[i] in DELIMITADORES:
                tokens.append({'lexema': linea[i], 'tipo': 5, 'codigo': DELIMITADORES[linea[i]], 'linea': li})
                i += 1
                continue
            if linea[i].isdigit():
                j = i
                while j < len(linea) and (linea[j].isdigit() or linea[j] == '.'):
                    j += 1
                tokens.append({'lexema': linea[i:j], 'tipo': 6, 'sub': 61, 'linea': li, 'es_const': True})
                i = j
                continue
            if linea[i].isalpha() or linea[i] == '_':
                j = i
                while j < len(linea) and (linea[j].isalnum() or linea[j] == '_' or linea[j] == '#'):
                    j += 1
                palabra = linea[i:j].upper()
                if palabra in PALABRAS_RESERVADAS:
                    tokens.append({'lexema': palabra, 'tipo': 1, 'codigo': PALABRAS_RESERVADAS[palabra], 'linea': li})
                else:
                    tokens.append({'lexema': palabra, 'tipo': 4, 'linea': li, 'es_ident': True})
                i = j
                continue
            errores_lexicos.append({'tipo': 1, 'codigo': 101, 'linea': li,
                                    'descripcion': f"Símbolo desconocido: '{linea[i]}'"})
            tokens.append({'lexema': linea[i], 'tipo': 9, 'codigo': 101, 'linea': li, 'desconocido': True})
            i += 1

    return tokens, errores_lexicos


def analizar(sql):
    brutos, errores_lexicos = tokenizar(sql)

    mapa_ident = {}
    lista_const = []
    cont_ident = 401
    cont_const = 600

    for idx, t in enumerate(brutos):
        if t.get('es_ident'):
            if t['lexema'] not in mapa_ident:
                mapa_ident[t['lexema']] = {'valor': cont_ident, 'lineas': []}
                cont_ident += 1
            if t['linea'] not in mapa_ident[t['lexema']]['lineas']:
                mapa_ident[t['lexema']]['lineas'].append(t['linea'])
        if t.get('es_const'):
            encontrado = next((c for c in lista_const if c['lexema'] == t['lexema'] and c['sub'] == t['sub']), None)
            if not encontrado:
                lista_const.append({'lexema': t['lexema'], 'sub': t['sub'], 'no': idx + 1, 'valor': cont_const})
                cont_const += 1

    filas_sem = []
    for idx, t in enumerate(brutos):
        if t['tipo'] == 4:
            codigo = mapa_ident[t['lexema']]['valor']
            token_display = t['lexema']
        elif t.get('es_const'):
            c = next(c for c in lista_const if c['lexema'] == t['lexema'] and c['sub'] == t['sub'])
            codigo = c['valor']
            token_display = 'CONSTANTE'
        elif t.get('desconocido'):
            codigo = 101
            token_display = t['lexema']
        else:
            codigo = t.get('codigo', '?')
            token_display = t['lexema']
        filas_sem.append((idx + 1, t['linea'], token_display, t['tipo'], codigo))

    errores_sint_sem = analizador_sintactico(brutos)
    todos_errores = errores_lexicos + errores_sint_sem
    return filas_sem, mapa_ident, lista_const, todos_errores

def analizador_sintactico(tokens):
    global ts_tablas, ts_atributos, ts_restricciones

    toks = [t for t in tokens if t['tipo'] != 9]
    if not toks:
        return []

    errores = []
    pos = [0]

    def ver():
        return toks[pos[0]] if pos[0] < len(toks) else None

    def consumir():
        t = toks[pos[0]]
        pos[0] += 1
        return t

    def linea_actual():
        t = ver()
        return t['linea'] if t else toks[-1]['linea']

    def esperar_tipo(tipo, cod_err, desc):
        t = ver()
        if t and t['tipo'] == tipo:
            return consumir()
        errores.append({'tipo': 2, 'codigo': cod_err, 'linea': linea_actual(), 'descripcion': desc})
        return None

    def esperar_reservada(palabra):
        t = ver()
        if t and t['tipo'] == 1 and t['lexema'] == palabra:
            return consumir()
        errores.append({'tipo': 2, 'codigo': 201, 'linea': linea_actual(),
                        'descripcion': f"Se esperaba Palabra Reservada '{palabra}'"})
        return None

    def esperar_delimitador(simbolo):
        t = ver()
        if t and t['tipo'] == 5 and t['lexema'] == simbolo:
            return consumir()
        errores.append({'tipo': 2, 'codigo': 205, 'linea': linea_actual(),
                        'descripcion': f"Se esperaba Delimitador '{simbolo}'"})
        return None

    def es_reservada(palabra):
        t = ver()
        return t and t['tipo'] == 1 and t['lexema'] == palabra

    def es_tipo(tipo):
        t = ver()
        return t and t['tipo'] == tipo

    def buscar_tabla(nombre):
        for t in ts_tablas:
            if t['nombre'] == nombre:
                return t
        return None

    def buscar_atributo_en_tabla(no_tabla, nombre_atr):
        for a in ts_atributos:
            if a['no_tabla'] == no_tabla and a['nombre'] == nombre_atr:
                return a
        return None

    def buscar_restriccion(nombre):
        for r in ts_restricciones:
            if r['nombre'] == nombre:
                return r
        return None

    def no_tabla_siguiente():
        return len(ts_tablas) + 1

    def no_atributo_siguiente():
        return len(ts_atributos) + 1

    def no_restriccion_siguiente():
        return len(ts_restricciones) + 1

    def parsear_select():
        consumir()  # SELECT

        # Verificar DISTINCT opcional
        if es_reservada('DISTINCT'):
            consumir()

        # Recolectar columnas del SELECT para validar después
        cols_select = []
        if es_tipo(7) and ver()['lexema'] == '*':
            consumir()
        else:
            t = ver()
            if t and t['tipo'] == 4:
                nombre_col = t['lexema']
                linea_col  = t['linea']
                consumir()
                if ver() and ver()['tipo'] == 5 and ver()['lexema'] == '.':
                    consumir()
                    t2 = ver()
                    if t2 and t2['tipo'] == 4:
                        cols_select.append({'tabla': nombre_col, 'col': t2['lexema'], 'linea': t2['linea']})
                        consumir()
                    else:
                        esperar_tipo(4, 204, "Se esperaba Identificador después de '.'")
                else:
                    cols_select.append({'tabla': None, 'col': nombre_col, 'linea': linea_col})
            else:
                esperar_tipo(4, 204, "Se esperaba Identificador (columna)")

            while ver() and ver()['tipo'] == 5 and ver()['lexema'] == ',':
                consumir()
                t = ver()
                if t is None or (t['tipo'] == 1 and t['lexema'] == 'FROM'):
                    errores.append({'tipo': 2, 'codigo': 204, 'linea': linea_actual(),
                                    'descripcion': "Se esperaba Identificador después de ','"})
                    break
                if t and t['tipo'] == 4:
                    nombre_col = t['lexema']
                    linea_col  = t['linea']
                    consumir()
                    if ver() and ver()['tipo'] == 5 and ver()['lexema'] == '.':
                        consumir()
                        t2 = ver()
                        if t2 and t2['tipo'] == 4:
                            cols_select.append({'tabla': nombre_col, 'col': t2['lexema'], 'linea': t2['linea']})
                            consumir()
                        else:
                            esperar_tipo(4, 204, "Se esperaba Identificador después de '.'")
                    else:
                        cols_select.append({'tabla': None, 'col': nombre_col, 'linea': linea_col})
                else:
                    esperar_tipo(4, 204, "Se esperaba Identificador después de ','")

        esperar_reservada('FROM')

        # Recolectar tablas del FROM con alias
        tablas_from = []  # lista de {'nombre': ..., 'alias': ..., 'linea': ...}
        t = ver()
        if t is None or t['tipo'] == 1:
            errores.append({'tipo': 2, 'codigo': 204, 'linea': linea_actual(),
                            'descripcion': "Se esperaba Identificador (tabla)"})
        else:
            t_tab = ver()
            nombre_tab = t_tab['lexema']
            linea_tab  = t_tab['linea']
            consumir()
            alias = None
            if es_tipo(4):
                alias = ver()['lexema']
                consumir()
            tablas_from.append({'nombre': nombre_tab, 'alias': alias, 'linea': linea_tab})

            while ver() and ver()['tipo'] == 5 and ver()['lexema'] == ',':
                consumir()
                t = ver()
                if t is None or (t['tipo'] == 1 and t['lexema'] == 'WHERE'):
                    errores.append({'tipo': 2, 'codigo': 204, 'linea': linea_actual(),
                                    'descripcion': "Se esperaba Identificador después de ','"})
                    break
                t_tab = ver()
                nombre_tab = t_tab['lexema']
                linea_tab  = t_tab['linea']
                consumir()
                alias = None
                if es_tipo(4):
                    alias = ver()['lexema']
                    consumir()
                tablas_from.append({'nombre': nombre_tab, 'alias': alias, 'linea': linea_tab})

        # Validar semánticamente las tablas del FROM
        tablas_validas = []
        for tf in tablas_from:
            obj = buscar_tabla(tf['nombre'])
            if obj is None:
                errores.append({'tipo': 3, 'codigo': 314, 'linea': tf['linea'],
                                'descripcion': f'El nombre de la tabla "{tf["nombre"]}" no es válido.'})
            else:
                tablas_validas.append({'nombre': tf['nombre'], 'alias': tf['alias'], 'no': obj['no']})

        # Construir mapa alias→nombre_real para validaciones
        # clave: alias o nombre_real → no_tabla
        mapa_tablas = {}
        for tv in tablas_validas:
            mapa_tablas[tv['nombre']] = tv['no']
            if tv['alias']:
                mapa_tablas[tv['alias']] = tv['no']

        # Validar columnas del SELECT
        for cs in cols_select:
            validar_columna_select(cs['tabla'], cs['col'], cs['linea'], mapa_tablas, tablas_validas)

        # WHERE opcional
        if es_reservada('WHERE'):
            consumir()
            parsear_condicion(mapa_tablas, tablas_validas)

    def validar_columna_select(nombre_tabla_col, nombre_col, linea_col, mapa_tablas, tablas_validas):
        if not ts_tablas:
            return
        if nombre_tabla_col:
            # Formato TABLA.COL o ALIAS.COL
            if nombre_tabla_col not in mapa_tablas:
                errores.append({'tipo': 3, 'codigo': 315, 'linea': linea_col,
                                'descripcion': f'El identificador "{nombre_tabla_col}.{nombre_col}" no es válido.'})
                return
            no_tabla = mapa_tablas[nombre_tabla_col]
            atr = buscar_atributo_en_tabla(no_tabla, nombre_col)
            if not atr:
                errores.append({'tipo': 3, 'codigo': 311, 'linea': linea_col,
                                'descripcion': f'El nombre del atributo "{nombre_col}" no es válido.'})
        else:
            # Sin prefijo de tabla — buscar en todas las tablas del FROM
            encontrados = []
            for tv in tablas_validas:
                atr = buscar_atributo_en_tabla(tv['no'], nombre_col)
                if atr:
                    encontrados.append(tv)
            if len(encontrados) == 0:
                errores.append({'tipo': 3, 'codigo': 311, 'linea': linea_col,
                                'descripcion': f'El nombre del atributo "{nombre_col}" no es válido.'})
            elif len(encontrados) > 1:
                errores.append({'tipo': 3, 'codigo': 312, 'linea': linea_col,
                                'descripcion': f'El nombre del atributo "{nombre_col}" es ambigüo.'})

    def parsear_condicion(mapa_tablas=None, tablas_validas=None):
        if mapa_tablas is None:
            mapa_tablas = {}
        if tablas_validas is None:
            tablas_validas = []

        # Lado izquierdo de la condición
        t_ident = ver()
        nombre_izq = t_ident['lexema'] if t_ident and t_ident['tipo'] == 4 else None
        linea_izq  = t_ident['linea'] if t_ident else linea_actual()
        esperar_tipo(4, 204, "Se esperaba Identificador en condición")

        nombre_tabla_izq = None
        nombre_col_izq   = nombre_izq

        if ver() and ver()['tipo'] == 5 and ver()['lexema'] == '.':
            consumir()
            t2 = ver()
            nombre_tabla_izq = nombre_izq
            nombre_col_izq   = t2['lexema'] if t2 else ''
            linea_izq        = t2['linea'] if t2 else linea_izq
            esperar_tipo(4, 204, "Se esperaba Identificador después de '.'")

        # Validar el lado izquierdo si hay tablas semánticas
        if ts_tablas and tablas_validas:
            validar_columna_condicion(nombre_tabla_izq, nombre_col_izq, linea_izq,
                                      mapa_tablas, tablas_validas)

        t = ver()
        if t and t['tipo'] == 1 and t['lexema'] == 'IN':
            consumir()
            esperar_delimitador('(')
            if es_reservada('SELECT'):
                parsear_select()
            else:
                t2 = ver()
                if t2 and t2['tipo'] in (4, 6):
                    consumir()
                else:
                    errores.append({'tipo': 2, 'codigo': 206, 'linea': linea_actual(),
                                    'descripcion': "Se esperaba Constante o Identificador"})
                while ver() and ver()['tipo'] == 5 and ver()['lexema'] == ',':
                    consumir()
                    t2 = ver()
                    if t2 and t2['tipo'] in (4, 6):
                        consumir()
                    else:
                        errores.append({'tipo': 2, 'codigo': 206, 'linea': linea_actual(),
                                        'descripcion': "Se esperaba Constante o Identificador"})
            esperar_delimitador(')')

        elif t and t['tipo'] == 8:
            op = consumir()
            t2 = ver()
            if t2 and t2['tipo'] in (4, 6):
                # Validar error 3:313 — comparar CHAR con número entero
                if ts_tablas and tablas_validas and t2['tipo'] == 6 and t2.get('sub') == 61:
                    # Es constante numérica — verificar tipo del atributo izquierdo
                    atr_izq = obtener_atributo(nombre_tabla_izq, nombre_col_izq, mapa_tablas)
                    if atr_izq and atr_izq['tipo'] == 'CHAR':
                        errores.append({'tipo': 3, 'codigo': 313, 'linea': t2['linea'],
                                        'descripcion': f"Error de conversión al convertir el valor del atributo "
                                                       f"'{nombre_col_izq}' del tipo char a tipo de dato int."})
                consumir()
                if ver() and ver()['tipo'] == 5 and ver()['lexema'] == '.':
                    consumir()
                    esperar_tipo(4, 204, "Se esperaba Identificador después de '.'")
            else:
                errores.append({'tipo': 2, 'codigo': 206, 'linea': linea_actual(),
                                'descripcion': "Se esperaba Constante o Identificador"})
        else:
            t2 = ver()
            if t2 and t2['tipo'] == 5 and t2['lexema'] == '(':
                errores.append({'tipo': 2, 'codigo': 201, 'linea': t2['linea'],
                                'descripcion': "Se esperaba Palabra Reservada (IN)"})
            else:
                errores.append({'tipo': 2, 'codigo': 208, 'linea': linea_actual(),
                                'descripcion': "Se esperaba Operador Relacional"})
            return

        if es_reservada('AND') or es_reservada('OR'):
            consumir()
            parsear_condicion(mapa_tablas, tablas_validas)
        else:
            t = ver()
            if t and not (t['tipo'] == 5 and t['lexema'] == ')'):
                errores.append({'tipo': 2, 'codigo': 201, 'linea': t['linea'],
                                'descripcion': "Se esperaba Palabra Reservada (AND/OR)"})

    def validar_columna_condicion(nombre_tabla_col, nombre_col, linea_col, mapa_tablas, tablas_validas):
        if not nombre_col:
            return
        if nombre_tabla_col:
            if nombre_tabla_col not in mapa_tablas:
                errores.append({'tipo': 3, 'codigo': 315, 'linea': linea_col,
                                'descripcion': f'El identificador "{nombre_tabla_col}.{nombre_col}" no es válido.'})
                return
            no_tabla = mapa_tablas[nombre_tabla_col]
            atr = buscar_atributo_en_tabla(no_tabla, nombre_col)
            if not atr:
                errores.append({'tipo': 3, 'codigo': 311, 'linea': linea_col,
                                'descripcion': f'El nombre del atributo "{nombre_col}" no es válido.'})
        else:
            encontrados = []
            for tv in tablas_validas:
                atr = buscar_atributo_en_tabla(tv['no'], nombre_col)
                if atr:
                    encontrados.append(tv)
            if len(encontrados) == 0:
                errores.append({'tipo': 3, 'codigo': 311, 'linea': linea_col,
                                'descripcion': f'El nombre del atributo "{nombre_col}" no es válido.'})
            elif len(encontrados) > 1:
                errores.append({'tipo': 3, 'codigo': 312, 'linea': linea_col,
                                'descripcion': f'El nombre del atributo "{nombre_col}" es ambigüo.'})

    def obtener_atributo(nombre_tabla_col, nombre_col, mapa_tablas):
        if not nombre_col:
            return None
        if nombre_tabla_col and nombre_tabla_col in mapa_tablas:
            return buscar_atributo_en_tabla(mapa_tablas[nombre_tabla_col], nombre_col)
        for no_t in set(mapa_tablas.values()):
            atr = buscar_atributo_en_tabla(no_t, nombre_col)
            if atr:
                return atr
        return None

    def parsear_create_table():
        consumir()  # CREATE
        esperar_reservada('TABLE')

        t_nombre = ver()
        if not (t_nombre and t_nombre['tipo'] == 4):
            esperar_tipo(4, 204, "Se esperaba nombre de tabla")
            return
        consumir()
        nombre_tabla = t_nombre['lexema']
        linea_tabla  = t_nombre['linea']

        if buscar_tabla(nombre_tabla):
            errores.append({'tipo': 3, 'codigo': 306, 'linea': linea_tabla,
                            'descripcion': f'El nombre del atributo "{nombre_tabla}" está duplicado.'})

        esperar_delimitador('(')

        columnas_tabla    = []
        restricciones_tabla = []
        parsear_definicion_columnas(columnas_tabla, restricciones_tabla, nombre_tabla, linea_tabla)

        esperar_delimitador(')')

        if not buscar_tabla(nombre_tabla):
            no_t = no_tabla_siguiente()
            ts_tablas.append({
                'no': no_t,
                'nombre': nombre_tabla,
                'no_atributos': len(columnas_tabla),
                'no_restricciones': len(restricciones_tabla)
            })
            for col in columnas_tabla:
                col['no_tabla'] = no_t
                ts_atributos.append(col)
            for res in restricciones_tabla:
                res['no_tabla'] = no_t
                ts_restricciones.append(res)

    def parsear_definicion_columnas(columnas, restricciones, nombre_tabla, linea_tabla):
        parsear_def_columna_o_constraint(columnas, restricciones, nombre_tabla, linea_tabla)
        while ver() and ver()['tipo'] == 5 and ver()['lexema'] == ',':
            consumir()
            if ver() and ver()['tipo'] == 5 and ver()['lexema'] in (')', ';'):
                break
            parsear_def_columna_o_constraint(columnas, restricciones, nombre_tabla, linea_tabla)

    def parsear_def_columna_o_constraint(columnas, restricciones, nombre_tabla, linea_tabla):
        if ver() and ver()['tipo'] == 1 and ver()['lexema'] == 'CONSTRAINT':
            parsear_constraint(columnas, restricciones, nombre_tabla, linea_tabla)
        elif ver() and ver()['tipo'] == 4:
            t_col = consumir()
            nombre_col = t_col['lexema']
            linea_col  = t_col['linea']

            if any(c['nombre'] == nombre_col for c in columnas):
                errores.append({'tipo': 3, 'codigo': 302, 'linea': linea_col,
                                'descripcion': f'El nombre del atributo "{nombre_col}" se especifica más de una vez.'})

            t = ver()
            tipo_dato = None
            longitud  = 0
            if t and t['tipo'] == 1 and t['lexema'] in ('CHAR', 'NUMERIC'):
                tipo_dato = consumir()['lexema']
                if ver() and ver()['lexema'] == '(':
                    consumir()
                    t_lon = esperar_tipo(6, 206, "Se esperaba tamaño numérico")
                    longitud = int(t_lon['lexema']) if t_lon else 0
                    esperar_delimitador(')')
            else:
                errores.append({'tipo': 2, 'codigo': 201, 'linea': linea_actual(),
                                'descripcion': "Se esperaba tipo de dato (CHAR o NUMERIC)"})
                while ver() and not (
                    (ver()['tipo'] == 5 and ver()['lexema'] in (',', ')')) or
                    (ver()['tipo'] == 1 and ver()['lexema'] == 'CONSTRAINT')
                ):
                    consumir()
                return

            no_nulo = 0
            if es_reservada('NOT'):
                consumir()
                esperar_reservada('NULL')
                no_nulo = 1

            no_atr = no_atributo_siguiente() + len(columnas)
            columnas.append({
                'no_tabla': 0,
                'no_atr': no_atr,
                'nombre': nombre_col,
                'tipo': tipo_dato,
                'longitud': longitud,
                'no_nulo': no_nulo
            })
        else:
            if ver():
                consumir()

    def parsear_constraint(columnas, restricciones, nombre_tabla, linea_tabla, solo_sintaxis=False):
        consumir()  # CONSTRAINT
        t_nombre = ver()
        if not (t_nombre and t_nombre['tipo'] == 4):
            esperar_tipo(4, 204, "Se esperaba nombre de constraint")
            return
        consumir()
        nombre_res = t_nombre['lexema']
        linea_res  = t_nombre['linea']

        # Solo validar duplicado si no es modo solo_sintaxis
        if not solo_sintaxis:
            if buscar_restriccion(nombre_res) or any(r['nombre'] == nombre_res for r in restricciones):
                errores.append({'tipo': 3, 'codigo': 306, 'linea': linea_res,
                                'descripcion': f'El nombre de la restricción "{nombre_res}" esta duplicado.'})

        t = ver()
        if t and t['tipo'] == 1 and t['lexema'] == 'PRIMARY':
            consumir()
            esperar_reservada('KEY')
            esperar_delimitador('(')
            t_col = ver()
            nombre_col_pk = t_col['lexema'] if t_col else ''
            linea_col_pk  = t_col['linea'] if t_col else linea_res
            esperar_tipo(4, 204, "Se esperaba columna en PRIMARY KEY")

            if not solo_sintaxis:
                idx_col = next((i for i, c in enumerate(columnas) if c['nombre'] == nombre_col_pk), -1)
                if idx_col == -1:
                    errores.append({'tipo': 3, 'codigo': 303, 'linea': linea_col_pk,
                                    'descripcion': f'El nombre del atributo "{nombre_col_pk}" no existe en la tabla "{nombre_tabla}".'})
                while ver() and ver()['lexema'] == ',':
                    consumir()
                    t_col2 = ver()
                    nom2 = t_col2['lexema'] if t_col2 else ''
                    lin2 = t_col2['linea'] if t_col2 else linea_res
                    esperar_tipo(4, 204, "Se esperaba columna")
                    if not any(c['nombre'] == nom2 for c in columnas):
                        errores.append({'tipo': 3, 'codigo': 303, 'linea': lin2,
                                        'descripcion': f'El nombre del atributo "{nom2}" no existe en la tabla "{nombre_tabla}".'})
                esperar_delimitador(')')
                no_res = no_restriccion_siguiente() + len(restricciones)
                atr_no = columnas[idx_col]['no_atr'] if idx_col != -1 else '-'
                restricciones.append({
                    'no_tabla': 0, 'no_res': no_res, 'tipo': 1,
                    'nombre': nombre_res, 'atr_asoc': atr_no,
                    'tabla_ref': '-', 'atr_ref': '-'
                })
            else:
                while ver() and ver()['lexema'] == ',':
                    consumir()
                    esperar_tipo(4, 204, "Se esperaba columna")
                esperar_delimitador(')')

        elif t and t['tipo'] == 1 and t['lexema'] == 'FOREIGN':
            consumir()
            esperar_reservada('KEY')
            esperar_delimitador('(')
            t_col = ver()
            nombre_col_fk = t_col['lexema'] if t_col else ''
            linea_col_fk  = t_col['linea'] if t_col else linea_res
            esperar_tipo(4, 204, "Se esperaba columna en FOREIGN KEY")
            esperar_delimitador(')')
            esperar_reservada('REFERENCES')
            t_ref_tabla = ver()
            nombre_tabla_ref = t_ref_tabla['lexema'] if t_ref_tabla else ''
            esperar_tipo(4, 204, "Se esperaba tabla referenciada")
            esperar_delimitador('(')
            t_ref_col = ver()
            nombre_col_ref = t_ref_col['lexema'] if t_ref_col else ''
            esperar_tipo(4, 204, "Se esperaba columna referenciada")
            esperar_delimitador(')')

            if not solo_sintaxis:
                # 1. Validar columna local de la llave foránea
                idx_col_fk = next((i for i, c in enumerate(columnas) if c['nombre'] == nombre_col_fk), -1)
                if idx_col_fk == -1:
                    errores.append({'tipo': 3, 'codigo': 305, 'linea': linea_col_fk,
                                    'descripcion': f'Se hace referencia al atributo "{nombre_col_fk}" no válido en la tabla "{nombre_tabla}".'})

                # 2. Validar existencia de la TABLA referenciada
                tabla_ref_obj = buscar_tabla(nombre_tabla_ref)
                if not tabla_ref_obj:
                    errores.append(
                        {'tipo': 3, 'codigo': 304, 'linea': t_ref_tabla['linea'] if t_ref_tabla else linea_res,
                         'descripcion': f'La tabla referenciada "{nombre_tabla_ref}" no existe.'})
                    tabla_ref_no = '-'
                else:
                    tabla_ref_no = tabla_ref_obj['no']

                # 3. Validar existencia de la COLUMNA referenciada en esa tabla
                atr_ref_obj = buscar_atributo_en_tabla(tabla_ref_no, nombre_col_ref) if tabla_ref_obj else None
                if tabla_ref_obj and not atr_ref_obj:
                    errores.append({'tipo': 3, 'codigo': 305, 'linea': t_ref_col['linea'] if t_ref_col else linea_res,
                                    'descripcion': f'Se hace referencia al atributo "{nombre_col_ref}" no válido en la tabla "{nombre_tabla_ref}".'})
                    atr_ref_no = '-'
                else:
                    atr_ref_no = atr_ref_obj['no_atr'] if atr_ref_obj else '-'

                # Registrar la restricción
                atr_fk_no = columnas[idx_col_fk]['no_atr'] if idx_col_fk != -1 else '-'
                no_res = no_restriccion_siguiente() + len(restricciones)
                restricciones.append({
                    'no_tabla': 0, 'no_res': no_res, 'tipo': 2,
                    'nombre': nombre_res, 'atr_asoc': atr_fk_no,
                    'tabla_ref': tabla_ref_no, 'atr_ref': atr_ref_no
                })
        else:
            errores.append({'tipo': 2, 'codigo': 201, 'linea': linea_actual(),
                            'descripcion': "Se esperaba PRIMARY o FOREIGN después de CONSTRAINT"})

    def parsear_insert():
        consumir()  # INSERT
        esperar_reservada('INTO')

        t_tabla = ver()
        nombre_tabla = t_tabla['lexema'].upper() if t_tabla and t_tabla['tipo'] == 4 else None
        linea_insert = t_tabla['linea'] if t_tabla else linea_actual()
        esperar_tipo(4, 204, "Se esperaba nombre de tabla")

        info_tabla = buscar_tabla(nombre_tabla) if nombre_tabla else None
        cols_tabla = [a for a in ts_atributos if info_tabla and a['no_tabla'] == info_tabla['no']]

        if ver() and ver()['tipo'] == 5 and ver()['lexema'] == '(':
            consumir()
            esperar_tipo(4, 204, "Se esperaba columna")
            while ver() and ver()['lexema'] == ',':
                consumir()
                esperar_tipo(4, 204, "Se esperaba columna")
            esperar_delimitador(')')

        esperar_reservada('VALUES')
        esperar_delimitador('(')

        valores = []
        t = ver()
        if t and t['tipo'] in (4, 6):
            valores.append(t)
            consumir()
        else:
            errores.append({'tipo': 2, 'codigo': 206, 'linea': linea_actual(),
                            'descripcion': "Se esperaba Constante"})
            while ver() and not (ver()['tipo'] == 5 and ver()['lexema'] == ')'):
                consumir()
            if ver():
                consumir()
            return

        while ver() and ver()['tipo'] == 5 and ver()['lexema'] == ',':
            consumir()
            t = ver()
            if t and t['tipo'] in (4, 6):
                valores.append(t)
                consumir()
                t2 = ver()
                if t2 and t2['tipo'] in (4, 6):
                    errores.append({'tipo': 2, 'codigo': 201, 'linea': t2['linea'],
                                    'descripcion': "Se esperaba Palabra Reservada (coma entre valores)"})
                    while ver() and not (ver()['tipo'] == 5 and ver()['lexema'] == ')'):
                        consumir()
                    if ver():
                        consumir()
                    return
                elif t2 and t2['tipo'] == 5 and t2['codigo'] == 54:
                    errores.append({'tipo': 2, 'codigo': 201, 'linea': t2['linea'],
                                    'descripcion': "Se esperaba Palabra Reservada (coma entre valores)"})
                    while ver() and not (ver()['tipo'] == 5 and ver()['lexema'] == ')'):
                        consumir()
                    if ver():
                        consumir()
                    return
            else:
                errores.append({'tipo': 2, 'codigo': 206, 'linea': linea_actual(),
                                'descripcion': "Se esperaba Constante"})
                while ver() and not (ver()['tipo'] == 5 and ver()['lexema'] == ')'):
                    consumir()
                if ver():
                    consumir()
                return

        esperar_delimitador(')')

        if cols_tabla and len(valores) != len(cols_tabla):
            errores.append({'tipo': 3, 'codigo': 307, 'linea': linea_insert,
                            'descripcion': f'Los valores especificados no corresponden a la definición de la tabla.'})
            return

        if cols_tabla:
            for i, val in enumerate(valores):
                if i >= len(cols_tabla):
                    break
                col = cols_tabla[i]
                if val['tipo'] == 6 and val.get('sub') == 62:
                    if col['tipo'] == 'CHAR' and len(val['lexema']) > col['longitud']:
                        errores.append({'tipo': 3, 'codigo': 308, 'linea': val['linea'],
                                        'descripcion': f'Los datos de cadena o binarios se truncarían.'})

    if ver() is None:
        return []

    while ver() is not None:
        t = ver()
        if t['tipo'] == 5 and t['lexema'] == ';':
            consumir()
            continue
        errores_antes = len(errores)
        if t['tipo'] == 1 and t['lexema'] == 'SELECT':
            parsear_select()
        elif t['tipo'] == 1 and t['lexema'] == 'CREATE':
            parsear_create_table()
        elif t['tipo'] == 1 and t['lexema'] == 'INSERT':
            parsear_insert()
        else:
            errores.append({'tipo': 2, 'codigo': 201, 'linea': t['linea'],
                            'descripcion': "Se esperaba SELECT, CREATE o INSERT al inicio"})
            consumir()
            continue
        if len(errores) > errores_antes:
            while ver() is not None and not (ver()['tipo'] == 5 and ver()['lexema'] == ';'):
                consumir()

    return errores


# ── GUI ───────────────────────────────────────────────────────────────────────

class Aplicacion(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Traductor DML - Avance 4")
        self.resizable(True, True)
        self._construir_ui()

    def _construir_ui(self):
        marco_entrada = tk.Frame(self, padx=10, pady=8)
        marco_entrada.pack(fill='x')

        tk.Label(marco_entrada, text="Sentencia SQL:").pack(anchor='w')
        self.txt_sql = tk.Text(marco_entrada, height=6, font=('Courier', 10), wrap='word')
        self.txt_sql.pack(fill='x', pady=(4, 6))

        marco_botones = tk.Frame(marco_entrada)
        marco_botones.pack(anchor='w')
        tk.Button(marco_botones, text="Analizar",   width=12, command=self._analizar).pack(side='left', padx=(0, 6))
        tk.Button(marco_botones, text="Limpiar",    width=12, command=self._limpiar).pack(side='left', padx=(0, 6))
        tk.Button(marco_botones, text="Actualizar", width=14, command=self._actualizar_ts).pack(side='left', padx=(0, 6))
        tk.Button(marco_botones, text="Inicializar",width=14, command=self._inicializar_ts).pack(side='left')

        ttk.Separator(self, orient='horizontal').pack(fill='x', padx=10)

        self.nb = ttk.Notebook(self)
        self.nb.pack(fill='both', expand=True, padx=10, pady=8)

        f1 = tk.Frame(self.nb)
        self.nb.add(f1, text="Tabla Semantica")
        self.arbol_sem = self._crear_arbol(f1, ('No.', 'Linea', 'TOKEN', 'Tipo', 'Codigo'),
                                           anchos=[45, 50, 160, 50, 70])

        f2 = tk.Frame(self.nb)
        self.nb.add(f2, text="Identificadores")
        self.arbol_id = self._crear_arbol(f2, ('Identificador', 'Valor', 'Linea(s)'),
                                          anchos=[160, 70, 120])

        f3 = tk.Frame(self.nb)
        self.nb.add(f3, text="Constantes")
        self.arbol_ct = self._crear_arbol(f3, ('No.', 'Constante', 'Tipo', 'Valor'),
                                          anchos=[45, 160, 50, 70])

        f4 = tk.Frame(self.nb)
        self.nb.add(f4, text="TS Tablas")
        self.arbol_ts_tablas = self._crear_arbol(f4,
            ('No. Tabla', 'Nombre', 'No. Atributos', 'No. Restricciones'),
            anchos=[70, 160, 100, 120])

        f5 = tk.Frame(self.nb)
        self.nb.add(f5, text="TS Atributos")
        self.arbol_ts_atributos = self._crear_arbol(f5,
            ('No. Tabla', 'No. Atributo', 'Nombre', 'Tipo', 'Longitud', 'No Nulo'),
            anchos=[70, 90, 140, 70, 70, 60])

        f6 = tk.Frame(self.nb)
        self.nb.add(f6, text="TS Restricciones")
        self.arbol_ts_restricciones = self._crear_arbol(f6,
            ('No. Tabla', 'No. Restriccion', 'Tipo', 'Nombre', 'Atr. Asoc.', 'Tabla Ref.', 'Atr. Ref.'),
            anchos=[70, 100, 45, 160, 80, 80, 70])

        f7 = tk.Frame(self.nb)
        self.nb.add(f7, text="Errores")
        self.lbl_resultado = tk.Label(f7, text="", font=('Arial', 10, 'bold'), anchor='w')
        self.lbl_resultado.pack(fill='x', padx=6, pady=(4, 2))
        self.arbol_err = self._crear_arbol(f7,
            ('No.', 'Tipo', 'Codigo', 'Linea', 'Descripcion'),
            anchos=[40, 50, 65, 55, 380])

    def _crear_arbol(self, padre, columnas, anchos=None):
        marco = tk.Frame(padre)
        marco.pack(fill='both', expand=True)
        arbol = ttk.Treeview(marco, columns=columnas, show='headings', height=18)
        for i, c in enumerate(columnas):
            arbol.heading(c, text=c)
            w = anchos[i] if anchos else 100
            arbol.column(c, width=w,
                         anchor='center' if c in ('No.', 'Linea', 'Tipo', 'Codigo', 'Valor',
                                                   'No. Tabla', 'No. Atributo', 'No. Restriccion',
                                                   'Longitud', 'No Nulo', 'Atr. Asoc.',
                                                   'Tabla Ref.', 'Atr. Ref.') else 'w')
        barra = ttk.Scrollbar(marco, orient='vertical', command=arbol.yview)
        arbol.configure(yscrollcommand=barra.set)
        arbol.pack(side='left', fill='both', expand=True)
        barra.pack(side='right', fill='y')
        return arbol

    def _analizar(self):
        sql = self.txt_sql.get('1.0', 'end').strip()
        if not sql:
            return
        self._limpiar_arboles()

        global ts_tablas, ts_atributos, ts_restricciones
        ts_tablas = [r for r in ts_tablas if r.get('de_bd')]
        ts_atributos = [r for r in ts_atributos if r.get('de_bd')]
        ts_restricciones = [r for r in ts_restricciones if r.get('de_bd')]

        filas_sem, mapa_ident, lista_const, errores = analizar(sql)

        for fila in filas_sem:
            self.arbol_sem.insert('', 'end', values=fila)
        for nombre, info in mapa_ident.items():
            lineas_str = ', '.join(str(l) for l in info['lineas'])
            self.arbol_id.insert('', 'end', values=(nombre, info['valor'], lineas_str))
        for c in lista_const:
            self.arbol_ct.insert('', 'end', values=(c['no'], c['lexema'], c['sub'], c['valor']))

        self._refrescar_ts()

        if errores:
            for i, e in enumerate(errores, start=1):
                self.arbol_err.insert('', 'end', values=(i, e['tipo'], e['codigo'], e['linea'], e['descripcion']))
            self.lbl_resultado.config(text=f"Se encontraron {len(errores)} error(es).", fg='red')
            self.nb.select(6)
        else:
            self.lbl_resultado.config(text="Sentencia libre de errores. (Código 200)", fg='green')

    def _refrescar_ts(self):
        for arbol in (self.arbol_ts_tablas, self.arbol_ts_atributos, self.arbol_ts_restricciones):
            arbol.delete(*arbol.get_children())
        for r in ts_tablas:
            self.arbol_ts_tablas.insert('', 'end',
                values=(r['no'], r['nombre'], r['no_atributos'], r['no_restricciones']))
        for r in ts_atributos:
            self.arbol_ts_atributos.insert('', 'end',
                values=(r['no_tabla'], r['no_atr'], r['nombre'], r['tipo'], r['longitud'], r['no_nulo']))
        for r in ts_restricciones:
            self.arbol_ts_restricciones.insert('', 'end',
                values=(r['no_tabla'], r['no_res'], r['tipo'], r['nombre'], r['atr_asoc'], r['tabla_ref'], r['atr_ref']))

    def _actualizar_ts(self):
        ts_actualizar()
        self._refrescar_ts()
        self.lbl_resultado.config(text="Tablas semánticas actualizadas con BD Inscritos.", fg='blue')

    def _inicializar_ts(self):
        ts_inicializar()
        self._refrescar_ts()
        self.lbl_resultado.config(text="Tablas semánticas inicializadas.", fg='blue')

    def _limpiar(self):
        self.txt_sql.delete('1.0', 'end')
        self._limpiar_arboles()
        self.lbl_resultado.config(text="")

    def _limpiar_arboles(self):
        for arbol in (self.arbol_sem, self.arbol_id, self.arbol_ct, self.arbol_err):
            arbol.delete(*arbol.get_children())


if __name__ == '__main__':
    app = Aplicacion()
    app.mainloop()