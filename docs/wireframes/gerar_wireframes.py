"""Gera os wireframes de alta fidelidade do Rota (LAB02) em HTML estático.

Saída: um HTML por área (publico, cliente, agente, componentes) e um geral
(wireframes.html). Cada tela é um <section class="screen"> de 1440 px, pronto
para ser convertido em camadas do Figma.

Uso: python gerar_wireframes.py
"""
import base64
import pathlib
import re

AQUI = pathlib.Path(__file__).parent
FONTES = AQUI / "fontes"


# ---------------------------------------------------------------- utilitários
# O conversor HTML -> Figma ignora a escala do viewBox, então os SVGs saem já
# em pixels: viewBox igual ao tamanho final e coordenadas multiplicadas.
NUM = r"-?(?:\d+\.\d*|\.\d+|\d+)(?:[eE][-+]?\d+)?"
PARAMS = {"M": 2, "L": 2, "T": 2, "H": 1, "V": 1, "C": 6, "S": 4, "Q": 4, "A": 7, "Z": 0}
ATRIBUTOS = {"cx", "cy", "r", "x", "y", "width", "height", "rx", "ry", "x1", "y1", "x2", "y2", "stroke-width"}


def fmt(n):
    return f"{n:.3f}".rstrip("0").rstrip(".") or "0"


def escala_path(d, s):
    saida, cmd, nums = [], None, []

    def despeja():
        if cmd is None:
            return
        n = PARAMS[cmd.upper()]
        if n == 0:
            saida.append(cmd)
            return
        for i in range(0, len(nums), n):
            grupo = nums[i:i + n]
            if cmd.upper() == "A":
                grupo = [g * s if j in (0, 1, 5, 6) else g for j, g in enumerate(grupo)]
                txt = " ".join(fmt(g) if j in (0, 1, 5, 6) else str(int(g)) if j in (3, 4) else fmt(g) for j, g in enumerate(grupo))
            else:
                txt = " ".join(fmt(g * s) for g in grupo)
            saida.append(cmd + txt)

    for tok in re.findall(r"[AaCcHhLlMmQqSsTtVvZz]|" + NUM, d):
        if tok.isalpha():
            despeja()
            cmd, nums = tok, []
        else:
            nums.append(float(tok))
    despeja()
    return " ".join(saida)


def escala_svg(interno, s):
    def troca(m):
        nome, val = m.group(1), m.group(2)
        if nome == "d":
            return f'd="{escala_path(val, s)}"'
        if nome in ATRIBUTOS:
            return f'{nome}="{fmt(float(val) * s)}"'
        return m.group(0)

    return re.sub(r'(?<![\w-])([a-z][\w-]*)="([^"]*)"', troca, interno)


def brl(v):
    s = f"{v:,.2f}"
    return "R$ " + s.replace(",", "X").replace(".", ",").replace("X", ".")


def parcela(pv, taxa, n):
    return pv * taxa / (1 - (1 + taxa) ** -n)


ICONES = {
    "home": '<path d="M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8"/><path d="M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>',
    "plus": '<path d="M5 12h14"/><path d="M12 5v14"/>',
    "clipboard": '<rect width="8" height="4" x="8" y="2" rx="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="M12 11h4"/><path d="M12 16h4"/><path d="M8 11h.01"/><path d="M8 16h.01"/>',
    "clipcheck": '<rect width="8" height="4" x="8" y="2" rx="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="m9 14 2 2 4-4"/>',
    "user": '<path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>',
    "logout": '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5"/><path d="M21 12H9"/>',
    "car": '<path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"/><circle cx="7" cy="17" r="2"/><path d="M9 17h6"/><circle cx="17" cy="17" r="2"/>',
    "file": '<path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v4a2 2 0 0 0 2 2h4"/><path d="M10 9H8"/><path d="M16 13H8"/><path d="M16 17H8"/>',
    "dashboard": '<rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/>',
    "search": '<circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>',
    "calendar": '<path d="M8 2v4"/><path d="M16 2v4"/><rect width="18" height="18" x="3" y="4" rx="2"/><path d="M3 10h18"/>',
    "eye": '<path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0"/><circle cx="12" cy="12" r="3"/>',
    "pencil": '<path d="M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z"/><path d="m15 5 4 4"/>',
    "x": '<path d="M18 6 6 18"/><path d="m6 6 12 12"/>',
    "check": '<path d="M20 6 9 17l-5-5"/>',
    "chevright": '<path d="m9 18 6-6-6-6"/>',
    "chevdown": '<path d="m6 9 6 6 6-6"/>',
    "chevleft": '<path d="m15 18-6-6 6-6"/>',
    "arrowright": '<path d="M5 12h14"/><path d="m12 5 7 7-7 7"/>',
    "building": '<path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z"/><path d="M6 12H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2"/><path d="M18 9h2a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-2"/><path d="M10 6h4"/><path d="M10 10h4"/><path d="M10 14h4"/><path d="M10 18h4"/>',
    "landmark": '<path d="M3 22h18"/><path d="M6 18v-7"/><path d="M10 18v-7"/><path d="M14 18v-7"/><path d="M18 18v-7"/><path d="M12 2 20 7H4z"/>',
    "alert": '<circle cx="12" cy="12" r="10"/><path d="M12 8v4"/><path d="M12 16h.01"/>',
    "trash": '<path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>',
    "lock": '<rect width="18" height="11" x="3" y="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>',
    "checkcircle": '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><path d="m9 11 3 3L22 4"/>',
    "xcircle": '<circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/>',
    "clock": '<circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>',
    "banknote": '<rect width="20" height="12" x="2" y="6" rx="2"/><circle cx="12" cy="12" r="2"/><path d="M6 12h.01M18 12h.01"/>',
    "briefcase": '<path d="M16 20V4a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/><rect width="20" height="14" x="2" y="6" rx="2"/>',
    "shield": '<path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/><path d="m9 12 2 2 4-4"/>',
    "bell": '<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/>',
    "info": '<circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/>',
    "mappin": '<path d="M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0"/><circle cx="12" cy="10" r="3"/>',
    "filter": '<path d="M22 3H2l8 9.46V19l4 2v-8.54z"/>',
    "inbox": '<path d="M22 12h-6l-2 3h-4l-2-3H2"/><path d="M5.45 5.11 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"/>',
    "loader": '<path d="M21 12a9 9 0 1 1-6.219-8.56"/>',
    "thumbup": '<path d="M7 10v12"/><path d="M15 5.88 14 10h5.83a2 2 0 0 1 1.92 2.56l-2.33 8A2 2 0 0 1 17.5 22H4a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2h2.76a2 2 0 0 0 1.79-1.11L12 2a3.13 3.13 0 0 1 3 3.88Z"/>',
    "thumbdown": '<path d="M17 14V2"/><path d="M9 18.12 10 14H4.17a2 2 0 0 1-1.92-2.56l2.33-8A2 2 0 0 1 6.5 2H20a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-2.76a2 2 0 0 0-1.79 1.11L12 22a3.13 3.13 0 0 1-3-3.88Z"/>',
}


def icone(nome, tam=18, cor="#15181E", traco=2):
    s = tam / 24
    return (
        f'<svg width="{tam}" height="{tam}" viewBox="0 0 {tam} {tam}" fill="none" stroke="{cor}" '
        f'stroke-width="{fmt(traco * s)}" stroke-linecap="round" stroke-linejoin="round" '
        f'xmlns="http://www.w3.org/2000/svg">{escala_svg(ICONES[nome], s)}</svg>'
    )


CARRO = '<ellipse cx="113" cy="85" rx="98" ry="4" fill="#15181E" opacity="0.14"/><path d="M14 62C14 52 20 48 34 46L62 42L88 24C94 20 100 18 110 18H150C160 18 168 22 174 28L188 42L200 45C208 47 212 52 212 60V66C212 69 210 70 207 70H196A20 20 0 0 0 156 70H74A20 20 0 0 0 34 70H18C15 70 14 68 14 66Z" fill="__COR__"/><path d="M70 43L92 28C96 25 100 24 106 24H126V43Z" fill="#DCE6F2"/><path d="M132 24H150C157 24 162 27 166 31L177 43H132Z" fill="#DCE6F2"/><rect x="15" y="51" width="10" height="4" rx="2" fill="#FFF3C4"/><rect x="203" y="51" width="8" height="4" rx="2" fill="#E5484D"/><rect x="98" y="47" width="12" height="2" rx="1" fill="#15181E" opacity="0.35"/><rect x="142" y="47" width="12" height="2" rx="1" fill="#15181E" opacity="0.35"/><circle cx="54" cy="70" r="15" fill="#15181E"/><circle cx="54" cy="70" r="6" fill="#C9CDD6"/><circle cx="176" cy="70" r="15" fill="#15181E"/><circle cx="176" cy="70" r="6" fill="#C9CDD6"/>'


def carro_svg(cor, largura=190):
    s = largura / 220
    altura = round(90 * s)
    return f'<svg width="{largura}" height="{altura}" viewBox="0 0 {largura} {altura}" xmlns="http://www.w3.org/2000/svg">{escala_svg(CARRO.replace("__COR__", cor), s)}</svg>'


CARROS = {
    "argo": dict(marca="Fiat", modelo="Argo", ano=2023, placa="QPX4E21", renavam="01284736519", diaria=159, cor="#C2410C", tinta="#FBEDE5", dono="Empresa"),
    "polo": dict(marca="Volkswagen", modelo="Polo", ano=2024, placa="RTA2B45", renavam="01359872046", diaria=189, cor="#2B59C3", tinta="#E8EEFC", dono="Empresa"),
    "onix": dict(marca="Chevrolet", modelo="Onix", ano=2024, placa="SHB7C09", renavam="01362590418", diaria=185, cor="#BDB8AD", tinta="#F1EFEA", dono="Cliente"),
    "hb20": dict(marca="Hyundai", modelo="HB20", ano=2023, placa="PUV3D18", renavam="01297413385", diaria=177, cor="#7D838D", tinta="#EEF0F3", dono="Empresa"),
    "renegade": dict(marca="Jeep", modelo="Renegade", ano=2022, placa="QNE8A62", renavam="01245068972", diaria=247, cor="#4D6B3C", tinta="#EAF0E5", dono="Banco"),
    "corolla": dict(marca="Toyota", modelo="Corolla", ano=2024, placa="RFZ1G37", renavam="01371854620", diaria=299, cor="#2A2F38", tinta="#ECEDEF", dono="Banco"),
}

STATUS = {
    "enviado": ("Enviado", "#475569", "#E2E8F0"),
    "analise": ("Em análise", "#92400E", "#FEF3C7"),
    "aprovado": ("Aprovado", "#166534", "#DCFCE7"),
    "reprovado": ("Reprovado", "#991B1B", "#FEE2E2"),
    "contratado": ("Contratado", "#1E40AF", "#DBEAFE"),
    "cancelado": ("Cancelado", "#6B7280", "#F3F4F6"),
}

TAXA = 0.019
PARCELAS = 3


# ---------------------------------------------------------------- componentes
def badge(chave):
    rotulo, texto, fundo = STATUS[chave]
    risco = ' style="text-decoration:line-through"' if chave == "cancelado" else ""
    return (
        f'<span class="badge" style="background:{fundo};color:{texto}">'
        f'<span class="dot" style="background:{texto}"></span><span{risco}>{rotulo}</span></span>'
    )


def placa(numero, tam="md"):
    return f'<div class="placa {tam}"><div class="placa-top">BRASIL</div><div class="placa-num">{numero}</div></div>'


def botao(rotulo, tipo="primary", ic=None, tam="", estado="", bloco=False, cor_ic=None):
    cores = {"primary": "#FFFFFF", "secondary": "#15181E", "ghost": "#1F4FD8", "danger": "#FFFFFF",
             "danger-outline": "#B42318", "amber": "#15181E", "dark": "#FFFFFF"}
    cor = cor_ic or cores.get(tipo, "#15181E")
    if estado == "disabled":
        cor = "#8E929C"
    if estado == "loading":
        ic = "loader"
    i = icone(ic, 16, cor, 2.2) if ic else ""
    classes = f"btn btn-{tipo} {tam} {estado} {'btn-block' if bloco else ''}".strip()
    return f'<div class="{classes}">{i}<span>{rotulo}</span></div>'


def campo(rotulo, valor="", ph=False, erro=None, dica=None, mono=False, ic=None, estado="", larg=None, dir_ic=None, area=False):
    est = estado or ("error" if erro else "")
    cls_val = "ph" if ph else ("mono" if mono else "")
    i = icone(ic, 18, "#6B7080") if ic else ""
    d = icone(dir_ic, 18, "#6B7080") if dir_ic else ""
    estilo = f' style="width:{larg}px"' if larg else ""
    msg = ""
    if erro:
        msg = f'<div class="help err">{icone("alert", 14, "#B42318")}<span>{erro}</span></div>'
    elif dica:
        msg = f'<div class="help"><span>{dica}</span></div>'
    rot = f'<div class="label">{rotulo}</div>' if rotulo else ""
    tipo = "input textarea" if area else "input"
    return f'<div class="field"{estilo}>{rot}<div class="{tipo} {est}">{i}<span class="val {cls_val}">{valor}</span>{d}</div>{msg}</div>'


def selecao(rotulo, valor, larg=None, ic=None):
    return campo(rotulo, valor, larg=larg, ic=ic, dir_ic="chevdown")


def data(rotulo, valor, larg=None):
    return campo(rotulo, valor, larg=larg, ic="calendar", mono=True)


def toggle(ligado=True):
    return f'<div class="toggle {"on" if ligado else ""}"><div class="knob"></div></div>'


def segmentado(itens, ativo):
    out = []
    for i, (rot, ic) in enumerate(itens):
        a = i == ativo
        ico = icone(ic, 16, "#15181E" if a else "#6B7080") if ic else ""
        out.append(f'<div class="seg-item {"active" if a else ""}">{ico}<span>{rot}</span></div>')
    return f'<div class="seg">{"".join(out)}</div>'


def stepper(passos, atual):
    out = []
    for i, p in enumerate(passos):
        if i < atual:
            cls, dentro = "done", icone("check", 14, "#FFFFFF", 3)
        elif i == atual:
            cls, dentro = "active", f"<span>{i + 1}</span>"
        else:
            cls, dentro = "", f"<span>{i + 1}</span>"
        out.append(f'<div class="step {cls}"><div class="step-num">{dentro}</div><span>{p}</span></div>')
        if i < len(passos) - 1:
            out.append(f'<div class="step-line {"done" if i < atual else ""}"></div>')
    return f'<div class="stepper">{"".join(out)}</div>'


def marca_rota(escuro=True):
    cor = "#FFFFFF" if escuro else "#15181E"
    return (
        f'<div class="brand"><div class="brand-mark">{icone("car", 20, "#15181E", 2.2)}</div>'
        f'<span class="brand-name" style="color:{cor}">Rota</span></div>'
    )


def car_card(k, selecionado=False, larg=204):
    c = CARROS[k]
    sel = " selected" if selecionado else ""
    check = f'<div class="car-check">{icone("check", 14, "#FFFFFF", 3)}</div>' if selecionado else ""
    return f'''<div class="car-card{sel}" style="width:{larg}px"><div class="car-img" style="background:{c["tinta"]}">{carro_svg(c["cor"], 164)}<div class="car-placa">{placa(c["placa"], "sm")}</div>{check}</div><div class="car-body"><div class="car-name">{c["marca"]} {c["modelo"]}</div><div class="car-meta">{c["ano"]} · <b>{brl(c["diaria"])}</b>/dia</div></div></div>'''


def thumb(k, larg=72, alt=48):
    c = CARROS[k]
    return f'<div class="thumb" style="width:{larg}px;height:{alt}px;background:{c["tinta"]}">{carro_svg(c["cor"], larg - 12)}</div>'


def stat(rotulo, numero, ic, cor, fundo, nota=""):
    n = f'<div class="stat-note">{nota}</div>' if nota else ""
    return f'''<div class="card stat"><div class="stat-top"><span class="stat-label">{rotulo}</span><div class="stat-icon" style="background:{fundo}">{icone(ic, 18, cor)}</div></div><div class="stat-num">{numero}</div>{n}</div>'''


def avatar(iniciais, tam=36, fundo="#F5B700", cor="#15181E"):
    return f'<div class="avatar" style="width:{tam}px;height:{tam}px;background:{fundo};color:{cor};font-size:{round(tam * 0.36)}px">{iniciais}</div>'


def toast(titulo, texto, tipo="ok"):
    ic = icone("checkcircle", 20, "#4ADE80") if tipo == "ok" else icone("xcircle", 20, "#F87171")
    return f'<div class="toast">{ic}<div class="toast-txt"><div class="toast-title">{titulo}</div><div class="toast-sub">{texto}</div></div>{icone("x", 16, "#8A90A0")}</div>'


def linha_info(rot, val, mono=False):
    return f'<div class="info-row"><span class="info-k">{rot}</span><span class="info-v {"mono" if mono else ""}">{val}</span></div>'


def dono_pill(dono):
    ic = {"Cliente": "user", "Empresa": "building", "Banco": "landmark"}[dono]
    return f'<span class="pill">{icone(ic, 14, "#3F4450")}<span>{dono}</span></span>'


# ---------------------------------------------------------------- estrutura
def sidebar_cliente(ativo):
    itens = [("home", "Início"), ("plus", "Novo pedido"), ("clipboard", "Meus pedidos"), ("user", "Meu perfil")]
    nav = []
    for ic, rot in itens:
        a = rot == ativo
        barra = '<div class="nav-bar"></div>' if a else ""
        nav.append(f'<div class="nav-item {"active" if a else ""}">{barra}{icone(ic, 18, "#FFFFFF" if a else "#9AA0AE")}<span>{rot}</span></div>')
    return f'''<aside class="sidebar">{marca_rota()}<div class="nav"><div class="nav-label">MENU</div>{"".join(nav)}</div><div class="side-foot"><div class="user-card">{avatar("MC")}<div class="user-txt"><div class="user-name">Mariana Costa</div><div class="user-role">Cliente</div></div>{icone("logout", 18, "#9AA0AE")}</div></div></aside>'''


def sidebar_agente(ativo, banco=False):
    itens = [("dashboard", "Painel", None), ("clipcheck", "Pedidos para avaliar", "4"), ("file", "Contratos", None)]
    if not banco:
        itens.append(("car", "Automóveis", None))
    nav = []
    for ic, rot, cont in itens:
        a = rot == ativo
        barra = '<div class="nav-bar"></div>' if a else ""
        c = f'<span class="nav-count">{cont}</span>' if cont else ""
        nav.append(f'<div class="nav-item {"active" if a else ""}">{barra}{icone(ic, 18, "#FFFFFF" if a else "#9AA0AE")}<span>{rot}</span>{c}</div>')
    nome = "Banco Horizonte" if banco else "Locadora Serra Verde"
    tipo = "Banco" if banco else "Empresa"
    ic_inst = "landmark" if banco else "building"
    pessoa, cargo, ini = ("Beatriz Souza", "Analista de crédito", "BS") if banco else ("Rafael Lima", "Analista de locação", "RL")
    return f'''<aside class="sidebar">{marca_rota()}<div class="inst"><div class="inst-icon">{icone(ic_inst, 18, "#F5B700")}</div><div class="inst-txt"><div class="inst-name">{nome}</div><span class="inst-tag">{tipo}</span></div></div><div class="nav"><div class="nav-label">AGENTE</div>{"".join(nav)}</div><div class="side-foot"><div class="user-card">{avatar(ini, fundo="#3A4150", cor="#FFFFFF")}<div class="user-txt"><div class="user-name">{pessoa}</div><div class="user-role">{cargo}</div></div>{icone("logout", 18, "#9AA0AE")}</div></div></aside>'''


def topbar(busca="Buscar pedidos, automóveis ou clientes"):
    return f'''<div class="topbar"><div class="search">{icone("search", 18, "#6B7080")}<span class="ph">{busca}</span></div><div class="top-right"><span class="top-date">quarta-feira, 28 de outubro de 2026</span><div class="icon-btn">{icone("bell", 18, "#3F4450")}<span class="bell-dot"></span></div></div></div>'''


def cabecalho(titulo, sub="", trilha=None, acoes="", extra=""):
    cr = ""
    if trilha:
        partes = []
        for i, t in enumerate(trilha):
            partes.append(f'<span class="{"crumb-cur" if i == len(trilha) - 1 else "crumb"}">{t}</span>')
            if i < len(trilha) - 1:
                partes.append(icone("chevright", 14, "#9A9DA6"))
        cr = f'<div class="crumbs">{"".join(partes)}</div>'
    s = f'<p class="sub">{sub}</p>' if sub else ""
    return f'<div class="page-head"><div class="ph-left">{cr}<div class="ph-title"><h1>{titulo}</h1>{extra}</div>{s}</div><div class="ph-actions">{acoes}</div></div>'


def tela_app(nome, lateral, corpo, extra="", busca=None):
    tb = topbar(busca) if busca else topbar()
    return f'<section class="screen" id="{nome}" data-name="{nome}">{lateral}<main class="main">{tb}<div class="content">{corpo}</div></main>{extra}</section>'


def tela_publica(nome, corpo, direita='<span class="muted">Já tem conta?</span><span class="link">Entrar</span>'):
    return f'<section class="screen pub" id="{nome}" data-name="{nome}"><div class="pub-wrap"><div class="pub-top">{marca_rota(False)}<div class="pub-right">{direita}</div></div>{corpo}</div></section>'


# ---------------------------------------------------------------- telas públicas
def t01_login(erro=False):
    alerta = ""
    if erro:
        alerta = f'<div class="alert-err">{icone("alert", 18, "#B42318")}<div><div class="alert-title">Usuário ou senha inválidos</div><div class="alert-sub">Confira os dados e tente de novo.</div></div></div>'
    usuario = campo("Usuário", "mariana.costa", ic="user", estado="error" if erro else "focus")
    senha = campo("Senha", "••••••••••", ic="lock", dir_ic="eye", estado="error" if erro else "")
    nome = "01b Login - erro" if erro else "01 Login"
    tracos = "".join('<span class="dash"></span>' for _ in range(11))
    hero = f'''<div class="hero-art"><div class="road"><div class="road-line">{tracos}</div></div><div class="hero-car">{carro_svg("#F5F4F0", 330)}</div><div class="float-card"><div class="fc-top"><span class="mono fc-id">PED-1045</span>{badge("aprovado")}</div><div class="fc-car">Volkswagen Polo 2024</div><div class="fc-sub">Parecer positivo · Locadora Serra Verde</div></div><div class="float-placa">{placa("RTA2B45", "lg")}</div></div>'''
    feats = "".join(
        f'<div class="feat">{icone("checkcircle", 18, "#F5B700")}<span>{t}</span></div>'
        for t in ["Análise financeira por agentes", "Crédito com bancos parceiros", "Status do pedido em tempo real"]
    )
    return f'''<section class="screen login" id="{nome}" data-name="{nome}"><div class="login-left">{marca_rota()}<div class="login-copy"><div class="eyebrow">ALUGUEL DE CARROS</div><h1 class="hero-h1">Seu próximo carro começa com um pedido.</h1><p class="hero-p">Peça, acompanhe a análise e feche o contrato com locadoras e bancos parceiros, tudo num só lugar.</p></div>{hero}<div class="feats">{feats}</div></div><div class="login-right"><div class="login-form"><h2 class="form-h">Entrar</h2><p class="sub">Acesse com o usuário e a senha cadastrados.</p><div class="demo"><div class="demo-label"><span>Entrar como</span><span class="tag-demo">demonstração</span></div>{segmentado([("Cliente", "user"), ("Agente", "building")], 0)}</div>{alerta}{usuario}{senha}{botao("Entrar", bloco=True)}<div class="divider"></div><div class="center-txt"><span class="muted">Ainda não tem conta?</span> <span class="link">Criar conta</span></div></div><div class="login-foot">Rota · Sistema de aluguel de carros</div></div></section>'''


def t02_perfil():
    def cartao(ic, titulo, texto, itens, sel):
        lis = "".join(f'<div class="li">{icone("check", 16, "#1F4FD8" if sel else "#6B7080", 2.5)}<span>{x}</span></div>' for x in itens)
        marca = f'<div class="pick-check">{icone("check", 14, "#FFFFFF", 3)}</div>' if sel else '<div class="pick-radio"></div>'
        return f'''<div class="pick {"selected" if sel else ""}">{marca}<div class="pick-icon" style="background:{"#E8EEFC" if sel else "#EFEDE7"}">{icone(ic, 26, "#1F4FD8" if sel else "#3F4450")}</div><div class="pick-title">{titulo}</div><p class="pick-text">{texto}</p><div class="pick-list">{lis}</div></div>'''

    corpo = f'''<div class="center-col"><div class="eyebrow dark">CRIAR CONTA</div><h1 class="big-h">Como você vai usar o Rota?</h1><p class="sub center">Escolha o seu perfil. Ele define o que você pode fazer no sistema.</p><div class="pick-row">{cartao("user", "Sou cliente", "Para quem quer alugar um carro: faça pedidos, acompanhe a análise e o contrato.", ["Criar, editar e cancelar pedidos", "Solicitar crédito bancário", "Acompanhar o status"], True)}{cartao("building", "Sou agente", "Para locadoras e bancos: avalie pedidos, emita pareceres e execute contratos.", ["Avaliar pedidos (parecer)", "Conceder crédito (bancos)", "Gerenciar contratos e frota"], False)}</div><div class="actions-row">{botao("Voltar para o login", "secondary", "chevleft")}{botao("Continuar", "primary")}</div></div>'''
    return tela_publica("02 Cadastro - escolha de perfil", corpo)


PASSOS_CLIENTE = ["Dados pessoais", "Endereço", "Profissão e renda", "Acesso"]


def t03_dados():
    form = f'''<div class="card form-card"><div class="form-card-head"><h2 class="card-h">Dados pessoais</h2><p class="sub">Use os mesmos dados do seu documento de identidade.</p></div><div class="form-grid">{campo("Nome completo", "Mariana Costa")}<div class="row2">{campo("CPF", "123.456.789-00", mono=True, erro="CPF inválido. Confira os 11 dígitos.")}{campo("RG", "MG-12.345.678", mono=True)}</div></div><div class="form-foot">{botao("Voltar", "secondary", "chevleft")}{botao("Próximo", "primary")}</div></div>'''
    lado = f'''<div class="aside-note">{icone("shield", 22, "#1F4FD8")}<div class="an-title">Por que pedimos isso?</div><p class="an-text">Os agentes (locadoras e bancos) usam seus dados e a sua renda para fazer a análise financeira dos seus pedidos de aluguel.</p><div class="an-list"><div class="li">{icone("check", 16, "#1F4FD8", 2.5)}<span>Dados guardados com segurança</span></div><div class="li">{icone("check", 16, "#1F4FD8", 2.5)}<span>Você pode editar depois em Meu perfil</span></div></div></div>'''
    corpo = f'''<div class="signup"><div class="signup-head"><div class="eyebrow dark">CADASTRO DE CLIENTE · PASSO 1 DE 4</div><h1 class="big-h left">Crie sua conta de cliente</h1>{stepper(PASSOS_CLIENTE, 0)}</div><div class="signup-body">{form}{lado}</div></div>'''
    return tela_publica("03 Cadastro de cliente - dados pessoais", corpo)


def t03b_renda():
    rendas = [("Teknova Sistemas Ltda.", 6200), ("Estúdio Pixel (freelance)", 1800), ("Colégio Horizonte (aulas)", 1200)]
    linhas = "".join(
        f'<div class="renda-row"><div class="renda-n">{i + 1}</div>{campo("Entidade empregadora" if i == 0 else "", e, larg=380)}{campo("Rendimento mensal" if i == 0 else "", brl(v), mono=True, larg=200)}<div class="icon-btn sq">{icone("trash", 18, "#6B7080")}</div></div>'
        for i, (e, v) in enumerate(rendas)
    )
    total = sum(v for _, v in rendas)
    form = f'''<div class="card form-card"><div class="form-card-head"><h2 class="card-h">Profissão e renda</h2><p class="sub">Informe até 3 rendimentos. Eles entram na análise financeira.</p></div><div class="form-grid">{campo("Profissão", "Analista de sistemas", ic="briefcase")}<div class="sec-label"><span>Rendimentos</span><span class="count-pill">3 de 3</span></div><div class="rendas">{linhas}</div><div class="add-row">{botao("Adicionar empregador", "secondary", "plus", estado="disabled")}<div class="help warn">{icone("info", 14, "#92400E")}<span>Máximo de 3 rendimentos.</span></div></div><div class="total-box"><span>Renda mensal total</span><span class="mono total-v">{brl(total)}</span></div></div><div class="form-foot">{botao("Voltar", "secondary", "chevleft")}{botao("Próximo", "primary")}</div></div>'''
    resumo = f'''<div class="aside-note"><div class="an-title">Seu cadastro</div><div class="mini-steps"><div class="ms done">{icone("checkcircle", 16, "#166534")}<span>Dados pessoais</span></div><div class="ms done">{icone("checkcircle", 16, "#166534")}<span>Endereço</span></div><div class="ms cur">{icone("clock", 16, "#1F4FD8")}<span>Profissão e renda</span></div><div class="ms">{icone("lock", 16, "#9A9DA6")}<span>Acesso</span></div></div></div>'''
    corpo = f'''<div class="signup"><div class="signup-head"><div class="eyebrow dark">CADASTRO DE CLIENTE · PASSO 3 DE 4</div><h1 class="big-h left">Crie sua conta de cliente</h1>{stepper(PASSOS_CLIENTE, 2)}</div><div class="signup-body">{form}{resumo}</div></div>'''
    return tela_publica("03b Cadastro de cliente - profissão e renda", corpo)


def t04_agente():
    form = f'''<div class="card form-card narrow"><div class="form-card-head"><h2 class="card-h">Cadastro de agente</h2><p class="sub">Para locadoras e bancos que avaliam pedidos de aluguel.</p></div><div class="form-grid"><div class="field"><div class="label">Tipo de agente</div>{segmentado([("Empresa", "building"), ("Banco", "landmark")], 0)}</div>{campo("Razão social", "Locadora Serra Verde Ltda.")}{campo("CNPJ", "12.345.678/0001-90", mono=True)}<div class="sep"></div><div class="sec-label"><span>Acesso</span></div>{campo("Usuário", "serraverde", ic="user")}<div class="row2">{campo("Senha", "••••••••••", ic="lock")}{campo("Confirmar senha", "••••••••••", ic="lock")}</div></div><div class="form-foot">{botao("Voltar", "secondary", "chevleft")}{botao("Criar conta", "primary")}</div></div>'''
    corpo = f'<div class="signup"><div class="signup-head"><div class="eyebrow dark">CADASTRO DE AGENTE</div><h1 class="big-h left">Cadastre sua empresa ou banco</h1></div><div class="signup-body single">{form}</div></div>'
    return tela_publica("04 Cadastro de agente", corpo)


# ---------------------------------------------------------------- área do cliente
PEDIDOS = [
    ("PED-1052", "corolla", "20/11 a 25/11/2026", 5, "enviado", False),
    ("PED-1048", "renegade", "10/11 a 17/11/2026", 7, "analise", True),
    ("PED-1045", "polo", "05/11 a 12/11/2026", 7, "aprovado", True),
    ("PED-1041", "argo", "22/10 a 24/10/2026", 2, "reprovado", False),
    ("PED-1039", "onix", "01/10 a 15/10/2026", 14, "contratado", False),
    ("PED-1031", "hb20", "12/09 a 14/09/2026", 2, "cancelado", False),
]


def valor_pedido(k, dias):
    return CARROS[k]["diaria"] * dias


def t05_inicio():
    stats = "".join([
        stat("Em análise", "2", "clock", "#92400E", "#FEF3C7", "Enviado ou em análise"),
        stat("Aprovados", "1", "checkcircle", "#166534", "#DCFCE7", "Aguardando contrato"),
        stat("Contratados", "1", "file", "#1E40AF", "#DBEAFE", "Contrato ativo"),
        stat("Cancelados", "1", "xcircle", "#6B7280", "#F3F4F6", "Nos últimos 90 dias"),
    ])
    linhas = ""
    for pid, k, per, dias, st, _ in PEDIDOS[:4]:
        c = CARROS[k]
        linhas += f'''<div class="list-row">{thumb(k)}<div class="lr-main"><div class="lr-title">{c["marca"]} {c["modelo"]} {c["ano"]}</div><div class="lr-sub"><span class="mono">{pid}</span> · {per}</div></div><div class="lr-val mono">{brl(valor_pedido(k, dias))}</div>{badge(st)}{icone("chevright", 18, "#9A9DA6")}</div>'''
    recentes = f'<div class="card col-main"><div class="card-head"><span class="card-title">Pedidos recentes</span><span class="link sm">Ver todos</span></div><div class="list">{linhas}</div></div>'
    proximo = f'''<div class="card next"><div class="next-tag">{icone("thumbup", 16, "#166534")}<span>Boa notícia</span></div><div class="next-title">Seu pedido PED-1045 foi aprovado</div><p class="next-text">A Locadora Serra Verde deu parecer positivo. Agora é só aguardar a execução do contrato.</p>{botao("Ver pedido", "secondary", tam="btn-sm")}</div>'''
    promo = f'''<div class="promo"><div class="promo-txt"><div class="promo-title">Precisa de um carro?</div><p class="promo-p">Escolha o automóvel, o período e, se quiser, peça crédito ao banco. São 3 passos.</p>{botao("Novo pedido", "amber", "plus")}</div><div class="promo-car">{carro_svg("#F5F4F0", 210)}</div></div>'''
    corpo = cabecalho("Olá, Mariana", "Acompanhe seus pedidos de aluguel e o andamento de cada análise.", acoes=botao("Novo pedido", "primary", "plus")) + f'<div class="stats">{stats}</div><div class="cols">{recentes}<div class="col-side">{proximo}{promo}</div></div>'
    return tela_app("05 Início do cliente", sidebar_cliente("Início"), corpo)


def t06_novo_pedido():
    chips = "".join(f'<span class="chip {"active" if i == 0 else ""}">{m}</span>' for i, m in enumerate(["Todas", "Fiat", "Volkswagen", "Chevrolet", "Hyundai", "Jeep", "Toyota"]))
    cards = "".join(car_card(k, k == "polo") for k in ["argo", "polo", "onix", "hb20", "renegade", "corolla"])
    passo1 = f'''<div class="card step-card"><div class="sc-head"><div class="sc-num">1</div><div><div class="card-title">Escolha o automóvel</div><div class="sc-sub">6 automóveis disponíveis para o período</div></div><div class="search sm" style="width:220px">{icone("search", 16, "#6B7080")}<span class="ph">Buscar modelo</span></div></div><div class="chips">{chips}</div><div class="car-grid">{cards}</div></div>'''
    passo2 = f'''<div class="card step-card"><div class="sc-head"><div class="sc-num">2</div><div><div class="card-title">Período</div><div class="sc-sub">Datas de retirada e devolução do automóvel</div></div></div><div class="row3">{data("Retirada", "05/11/2026")}{data("Devolução", "12/11/2026")}<div class="days-box"><span class="days-n mono">7</span><span>diárias</span></div></div></div>'''
    passo3 = f'''<div class="card step-card"><div class="sc-head"><div class="sc-num">3</div><div><div class="card-title">Crédito bancário</div><div class="sc-sub">Opcional. Um banco agente analisa e pode conceder o crédito.</div></div></div><div class="toggle-row">{toggle(True)}<div><div class="tr-title">Solicitar crédito bancário</div><div class="tr-sub">O contrato de crédito fica associado ao aluguel.</div></div></div><div class="row2">{selecao("Banco", "Banco Horizonte", ic="landmark")}{campo("Valor do crédito", brl(1323), mono=True)}</div></div>'''
    c = CARROS["polo"]
    resumo = f'''<div class="card summary"><div class="sum-head"><span class="card-title">Resumo do pedido</span></div><div class="sum-car" style="background:{c["tinta"]}">{carro_svg(c["cor"], 220)}</div><div class="sum-name-row"><div><div class="sum-name">Volkswagen Polo</div><div class="sc-sub">2024 · {brl(189)}/dia</div></div>{placa("RTA2B45")}</div><div class="sum-lines">{linha_info("Retirada", "05/11/2026", True)}{linha_info("Devolução", "12/11/2026", True)}{linha_info("Diárias", "7 × " + brl(189), True)}{linha_info("Crédito", "Banco Horizonte")}</div><div class="sum-total"><span>Valor estimado</span><span class="mono sum-total-v">{brl(1323)}</span></div><div class="note">{icone("info", 16, "#3F4450")}<span>Depois de enviado, o pedido passa pela análise financeira de um agente.</span></div>{botao("Enviar pedido", "primary", "arrowright", bloco=True)}{botao("Cancelar", "ghost", bloco=True)}</div>'''
    corpo = cabecalho("Novo pedido", "Monte o pedido em 3 passos. Você poderá editar enquanto ele estiver em análise.", ["Meus pedidos", "Novo pedido"]) + f'<div class="cols order">{"<div class=col-main>" + passo1 + passo2 + passo3 + "</div>"}<div class="col-side wide">{resumo}</div></div>'
    return tela_app("06 Novo pedido", sidebar_cliente("Novo pedido"), corpo)


COLS_PED = [("Pedido", 120), ("Automóvel", 0), ("Período", 200), ("Valor", 130), ("Status", 140), ("Ações", 120)]


def cab_tabela(cols):
    return '<div class="tr th">' + "".join(f'<div class="td" style="{"flex:1" if w == 0 else f"width:{w}px"}">{n}</div>' for n, w in cols) + "</div>"


def t07_meus_pedidos(vazio=False):
    tabs_def = [("Todos", 6), ("Enviado", 1), ("Em análise", 1), ("Aprovado", 1), ("Reprovado", 1), ("Contratado", 1), ("Cancelado", 1)]
    tabs = "".join(f'<div class="tab {"active" if i == 0 else ""}"><span>{n}</span><span class="tab-count">{0 if vazio else c}</span></div>' for i, (n, c) in enumerate(tabs_def))
    barra = f'<div class="table-bar"><div class="tabs">{tabs}</div><div class="search sm">{icone("search", 16, "#6B7080")}<span class="ph">Buscar por nº ou automóvel</span></div></div>'
    if vazio:
        corpo_t = f'''<div class="empty">{f'<div class="empty-icon">{icone("inbox", 28, "#3F4450")}</div>'}<div class="empty-title">Você ainda não fez nenhum pedido</div><p class="empty-text">Quando você enviar um pedido de aluguel, ele aparece aqui com o status da análise.</p>{botao("Fazer meu primeiro pedido", "primary", "plus")}</div>'''
    else:
        linhas = ""
        for pid, k, per, dias, st, _ in PEDIDOS:
            c = CARROS[k]
            pode = st in ("enviado", "analise")
            acoes = f'<div class="icon-btn sq sm">{icone("eye", 16, "#3F4450")}</div>'
            if pode:
                acoes += f'<div class="icon-btn sq sm">{icone("pencil", 16, "#3F4450")}</div><div class="icon-btn sq sm">{icone("x", 16, "#B42318")}</div>'
            linhas += f'''<div class="tr"><div class="td mono strong" style="width:120px">{pid}</div><div class="td" style="flex:1"><div class="car-cell">{thumb(k, 64, 42)}<div><div class="lr-title">{c["marca"]} {c["modelo"]} {c["ano"]}</div><div class="lr-sub">Diária {brl(c["diaria"])}</div></div></div></div><div class="td mono" style="width:200px">{per}</div><div class="td mono" style="width:130px">{brl(valor_pedido(k, dias))}</div><div class="td" style="width:140px">{badge(st)}</div><div class="td" style="width:120px"><div class="acts">{acoes}</div></div></div>'''
        corpo_t = cab_tabela(COLS_PED) + linhas + '<div class="table-foot"><span class="muted">Mostrando 6 de 6 pedidos</span><span class="muted">Editar e cancelar só aparecem em pedidos Enviado ou Em análise.</span></div>'
    corpo = cabecalho("Meus pedidos", "Todos os seus pedidos de aluguel e o status de cada um.", acoes=botao("Novo pedido", "primary", "plus")) + f'<div class="card table">{barra}{corpo_t}</div>'
    nome = "07b Meus pedidos - vazio" if vazio else "07 Meus pedidos"
    return tela_app(nome, sidebar_cliente("Meus pedidos"), corpo)


def timeline(passos):
    out = []
    for i, (titulo, sub, estado) in enumerate(passos):
        if estado == "done":
            dot = f'<div class="tl-dot done">{icone("check", 14, "#FFFFFF", 3)}</div>'
        elif estado == "cur":
            dot = '<div class="tl-dot cur"><div class="tl-inner"></div></div>'
        else:
            dot = '<div class="tl-dot"></div>'
        linha = ""
        if i < len(passos) - 1:
            linha = f'<div class="tl-line {"done" if estado == "done" else ""}"></div>'
        out.append(f'<div class="tl-step"><div class="tl-track">{dot}{linha}</div><div class="tl-title {estado}">{titulo}</div><div class="tl-sub">{sub}</div></div>')
    return f'<div class="timeline">{"".join(out)}</div>'


def detalhe_pedido_corpo():
    c = CARROS["renegade"]
    tl = timeline([
        ("Enviado", "27/10/2026 às 14:32", "done"),
        ("Em análise", "Desde 28/10 · Locadora Serra Verde", "cur"),
        ("Parecer", "Positivo ou negativo", ""),
        ("Contrato", "Execução pelo agente", ""),
    ])
    auto = f'''<div class="card"><div class="card-head"><span class="card-title">Automóvel</span></div><div class="auto-box"><div class="auto-img" style="background:{c["tinta"]}">{carro_svg(c["cor"], 230)}</div><div class="auto-info"><div class="sum-name">Jeep Renegade</div><div class="sc-sub">Ano 2022</div>{placa("QNE8A62")}<div class="sum-lines">{linha_info("Matrícula (RENAVAM)", c["renavam"], True)}{linha_info("Diária", brl(247), True)}</div></div></div></div>'''
    periodo = f'''<div class="card"><div class="card-head"><span class="card-title">Período e valor</span></div><div class="card-pad sum-lines">{linha_info("Retirada", "10/11/2026", True)}{linha_info("Devolução", "17/11/2026", True)}{linha_info("Diárias", "7 × " + brl(247), True)}<div class="sum-total"><span>Valor estimado</span><span class="mono sum-total-v">{brl(1729)}</span></div></div></div>'''
    credito = f'''<div class="card"><div class="card-head"><span class="card-title">Crédito bancário</span>{badge("analise")}</div><div class="card-pad sum-lines">{linha_info("Solicitado", "Sim")}{linha_info("Banco", "Banco Horizonte")}{linha_info("Valor", brl(1729), True)}</div></div>'''
    parecer = f'''<div class="card"><div class="card-head"><span class="card-title">Parecer do agente</span></div><div class="card-pad"><div class="wait">{icone("clock", 20, "#92400E")}<div><div class="wait-title">Aguardando parecer</div><p class="wait-text">A Locadora Serra Verde está fazendo a análise financeira do seu pedido. Você recebe o parecer aqui, com a justificativa.</p></div></div></div></div>'''
    acoes = botao("Editar pedido", "secondary", "pencil") + botao("Cancelar pedido", "danger-outline", "x")
    return cabecalho("Pedido PED-1048", "Enviado em 27/10/2026. Você ainda pode editar ou cancelar enquanto ele estiver em análise.", ["Meus pedidos", "PED-1048"], acoes, badge("analise")) + f'<div class="card card-pad">{tl}</div><div class="cols"><div class="col-main gap">{auto}{periodo}</div><div class="col-side wide gap">{credito}{parecer}</div></div>'


def t08_detalhe(modal=False):
    extra = ""
    nome = "08 Detalhe do pedido"
    if modal:
        nome = "08b Detalhe do pedido - cancelar"
        extra = f'''<div class="overlay"><div class="modal"><div class="modal-icon danger">{icone("alert", 24, "#B42318")}</div><div class="modal-title">Cancelar o pedido PED-1048?</div><p class="modal-text">O pedido sai da análise da Locadora Serra Verde e o crédito solicitado ao Banco Horizonte também é cancelado. Essa ação não pode ser desfeita.</p>{campo("Motivo (opcional)", "Mudei as datas da viagem", area=False)}<div class="modal-actions">{botao("Manter pedido", "secondary")}{botao("Cancelar pedido", "danger", "x")}</div></div></div>'''
    return tela_app(nome, sidebar_cliente("Meus pedidos"), detalhe_pedido_corpo(), extra)


def t09_perfil():
    rendas = [("Teknova Sistemas Ltda.", 6200), ("Estúdio Pixel (freelance)", 1800), ("Colégio Horizonte (aulas)", 1200)]
    rl = "".join(f'<div class="renda-row">{campo("Entidade empregadora" if i == 0 else "", e, larg=360)}{campo("Rendimento mensal" if i == 0 else "", brl(v), mono=True, larg=190)}<div class="icon-btn sq">{icone("trash", 18, "#6B7080")}</div></div>' for i, (e, v) in enumerate(rendas))
    lado = f'''<div class="card profile-card"><div class="pc-top">{avatar("MC", 72)}<div class="pc-name">Mariana Costa</div><div class="pc-sub mono">123.456.789-09</div><span class="pill">{icone("user", 14, "#3F4450")}<span>Cliente desde out/2026</span></span></div><div class="pc-nav"><div class="pc-link active">{icone("user", 16, "#15181E")}<span>Dados pessoais</span></div><div class="pc-link">{icone("mappin", 16, "#6B7080")}<span>Endereço</span></div><div class="pc-link">{icone("briefcase", 16, "#6B7080")}<span>Profissão e renda</span></div><div class="pc-link">{icone("lock", 16, "#6B7080")}<span>Acesso</span></div><div class="pc-link danger">{icone("trash", 16, "#B42318")}<span>Zona de perigo</span></div></div></div>'''
    s1 = f'<div class="card"><div class="card-head"><span class="card-title">Dados pessoais</span></div><div class="card-pad form-grid">{campo("Nome completo", "Mariana Costa")}<div class="row2">{campo("CPF", "123.456.789-09", mono=True, estado="disabled", dica="O CPF não pode ser alterado.")}{campo("RG", "MG-12.345.678", mono=True)}</div></div></div>'
    s2 = f'<div class="card"><div class="card-head"><span class="card-title">Endereço</span></div><div class="card-pad form-grid"><div class="row-addr">{campo("CEP", "30140-071", mono=True, larg=160)}{campo("Rua", "Avenida Afonso Pena", larg=0)}{campo("Número", "1500", mono=True, larg=110)}</div><div class="row3">{campo("Bairro", "Centro")}{campo("Cidade", "Belo Horizonte")}{selecao("UF", "MG")}</div></div></div>'
    s3 = f'<div class="card"><div class="card-head"><span class="card-title">Profissão e renda</span><span class="count-pill">3 de 3</span></div><div class="card-pad form-grid">{campo("Profissão", "Analista de sistemas", ic="briefcase")}<div class="rendas">{rl}</div><div class="total-box"><span>Renda mensal total</span><span class="mono total-v">{brl(9200)}</span></div></div></div>'
    perigo = f'<div class="card danger-zone"><div><div class="dz-title">Excluir conta</div><p class="dz-text">Apaga seu cadastro e cancela os pedidos que ainda estão em análise. Contratos já executados continuam registrados.</p></div>{botao("Excluir conta", "danger-outline", "trash")}</div>'
    salvar = f'<div class="save-bar"><span class="muted">Você alterou 1 campo.</span><div class="ph-actions">{botao("Descartar", "secondary")}{botao("Salvar alterações", "primary", "check")}</div></div>'
    corpo = cabecalho("Meu perfil", "Mantenha seus dados atualizados. Eles são usados na análise dos seus pedidos.") + f'<div class="cols"><div class="col-side narrow">{lado}</div><div class="col-main gap">{s1}{s2}{s3}{salvar}{perigo}</div></div>'
    return tela_app("09 Meu perfil", sidebar_cliente("Meu perfil"), corpo)


# ---------------------------------------------------------------- área do agente
FILA = [
    ("PED-1036", "João Pereira", "hb20", "há 4 dias", 885, False, "analise"),
    ("PED-1044", "Ana Ribeiro", "corolla", "há 2 dias", 2093, True, "analise"),
    ("PED-1048", "Mariana Costa", "renegade", "há 1 dia", 1729, True, "analise"),
    ("PED-1052", "Mariana Costa", "corolla", "há 3 horas", 1495, False, "enviado"),
]


def t10_painel():
    stats = "".join([
        stat("Aguardando análise", "4", "clock", "#92400E", "#FEF3C7", "O mais antigo há 4 dias"),
        stat("Aprovados no mês", "12", "checkcircle", "#166534", "#DCFCE7", "+3 em relação a setembro"),
        stat("Reprovados no mês", "3", "xcircle", "#991B1B", "#FEE2E2", "20% dos pareceres"),
        stat("Contratos ativos", "9", "file", "#1E40AF", "#DBEAFE", "2 vencem esta semana"),
    ])
    cols = [("Pedido", 100), ("Cliente", 170), ("Automóvel", 0), ("Enviado", 100), ("Valor", 110), ("Crédito", 64), ("Status", 120), ("", 84)]
    linhas = ""
    for pid, cli, k, quando, val, cred, st in FILA:
        c = CARROS[k]
        linhas += f'''<div class="tr"><div class="td mono strong" style="width:100px">{pid}</div><div class="td" style="width:170px"><div class="cli-cell">{avatar("".join(p[0] for p in cli.split()[:2]), 28, "#EFEDE7", "#3F4450")}<span>{cli}</span></div></div><div class="td" style="flex:1"><div class="car-cell">{thumb(k, 56, 38)}<div><div class="lr-title">{c["marca"]} {c["modelo"]}</div><div class="lr-sub">{c["ano"]} · {c["placa"]}</div></div></div></div><div class="td muted" style="width:100px">{quando}</div><div class="td mono" style="width:110px">{brl(val)}</div><div class="td" style="width:64px">{"Sim" if cred else "Não"}</div><div class="td" style="width:120px">{badge(st)}</div><div class="td" style="width:84px">{botao("Avaliar", "secondary", tam="btn-sm")}</div></div>'''
    fila = f'<div class="card table"><div class="card-head"><div><div class="card-title">Fila de análise</div><div class="sc-sub">Do pedido mais antigo para o mais novo</div></div><div class="legend"><span class="muted">Pareceres em outubro</span><span>{badge("aprovado")} 12</span><span>{badge("reprovado")} 3</span></div></div>{cab_tabela(cols)}{linhas}</div>'
    corpo = cabecalho("Painel", "Locadora Serra Verde · pedidos aguardando o seu parecer.", acoes=botao("Ver pedidos para avaliar", "primary", "clipcheck")) + f'<div class="stats">{stats}</div>{fila}'
    return tela_app("10 Painel do agente", sidebar_agente("Painel"), corpo)


def t11_avaliar():
    c = CARROS["renegade"]
    rendas = [("Teknova Sistemas Ltda.", 6200), ("Estúdio Pixel (freelance)", 1800), ("Colégio Horizonte (aulas)", 1200)]
    rl = "".join(f'<div class="info-row">{icone("briefcase", 16, "#6B7080")}<span class="info-k grow">{e}</span><span class="info-v mono">{brl(v)}</span></div>' for e, v in rendas)
    cliente = f'''<div class="card"><div class="card-head"><span class="card-title">Cliente</span><span class="link sm">Ver cadastro</span></div><div class="card-pad"><div class="cli-head">{avatar("MC", 48)}<div><div class="sum-name">Mariana Costa</div><div class="sc-sub">Analista de sistemas · Belo Horizonte/MG</div></div></div><div class="grid2">{linha_info("CPF", "123.456.789-09", True)}{linha_info("RG", "MG-12.345.678", True)}</div><div class="sep"></div><div class="sec-label"><span>Rendimentos</span><span class="count-pill">3</span></div>{rl}<div class="total-box"><span>Renda mensal total</span><span class="mono total-v">{brl(9200)}</span></div><div class="ratio"><div class="ratio-top"><span>Valor do aluguel sobre a renda</span><span class="mono strong">18,8%</span></div><div class="bar thin"><div class="bar-ok" style="width:18.8%"></div><div class="bar-rest"></div></div></div></div></div>'''
    pedido = f'''<div class="card"><div class="card-head"><span class="card-title">Pedido e automóvel</span>{badge("analise")}</div><div class="auto-box"><div class="auto-img" style="background:{c["tinta"]}">{carro_svg(c["cor"], 220)}</div><div class="auto-info"><div class="sum-name">Jeep Renegade 2022</div>{placa("QNE8A62")}<div class="sum-lines">{linha_info("Matrícula (RENAVAM)", c["renavam"], True)}{linha_info("Período", "10/11 a 17/11/2026", True)}{linha_info("Diárias", "7 × " + brl(247), True)}{linha_info("Valor estimado", brl(1729), True)}{linha_info("Crédito solicitado", "Sim · " + brl(1729))}</div></div></div></div>'''
    p = parcela(1729, TAXA, PARCELAS)
    decisao = f'''<div class="card decision"><div class="dec-head"><div class="card-title">Parecer financeiro</div><div class="sc-sub">Obrigatório para seguir com o pedido.</div></div><div class="dec-body"><div class="verdict"><div class="vd ok selected">{icone("thumbup", 18, "#166534")}<span>Positivo</span></div><div class="vd no">{icone("thumbdown", 18, "#6B7080")}<span>Negativo</span></div></div>{campo("Justificativa", "Renda total compatível com o valor do aluguel. Comprometimento de 18,8%, abaixo do limite de 30%.", area=True)}<div class="sep"></div><div class="credit-head"><div class="card-title sm">Contrato de crédito</div><span class="inst-chip">{icone("landmark", 14, "#1E40AF")}<span>Você é banco · cliente pediu crédito</span></span></div>{campo("Valor do crédito", brl(1729), mono=True)}<div class="row2">{selecao("Parcelas", f"{PARCELAS}x")}{campo("Taxa ao mês", "1,90%", mono=True)}</div><div class="calc"><span>{PARCELAS} parcelas de</span><span class="mono strong">{brl(p)}</span></div></div><div class="dec-foot">{botao("Salvar parecer", "primary", "check", bloco=True)}{botao("Modificar pedido", "secondary", "pencil", bloco=True)}</div></div>'''
    corpo = cabecalho("Avaliar pedido PED-1048", "Enviado por Mariana Costa em 27/10/2026.", ["Pedidos para avaliar", "PED-1048"], extra=badge("analise")) + f'<div class="cols"><div class="col-main gap">{pedido}{cliente}</div><div class="col-side wide">{decisao}</div></div>'
    return tela_app("11 Avaliar pedido", sidebar_agente("Pedidos para avaliar", banco=True), corpo)


def t12_executar():
    c = CARROS["polo"]
    p = parcela(1323, TAXA, PARCELAS)

    def opcao(ic, titulo, nome, sel):
        marca = f'<div class="pick-check sm">{icone("check", 12, "#FFFFFF", 3)}</div>' if sel else '<div class="pick-radio sm"></div>'
        return f'<div class="own {"selected" if sel else ""}">{marca}<div class="own-icon">{icone(ic, 20, "#1F4FD8" if sel else "#3F4450")}</div><div class="own-title">{titulo}</div><div class="own-sub">{nome}</div></div>'

    resumo = f'''<div class="card"><div class="card-head"><span class="card-title">Resumo do pedido PED-1045</span>{badge("aprovado")}</div><div class="auto-box"><div class="auto-img" style="background:{c["tinta"]}">{carro_svg(c["cor"], 220)}</div><div class="auto-info"><div class="sum-name">Volkswagen Polo 2024</div>{placa("RTA2B45")}<div class="sum-lines">{linha_info("Cliente", "Mariana Costa")}{linha_info("Período", "05/11 a 12/11/2026", True)}{linha_info("Valor", brl(1323), True)}</div></div></div><div class="card-pad top-line"><div class="parecer-ok">{icone("thumbup", 18, "#166534")}<div><div class="po-title">Parecer positivo · Locadora Serra Verde</div><p class="po-text">"Renda compatível e cadastro completo. Pode seguir para contrato." · 28/10/2026</p></div></div></div></div>'''
    dono = f'''<div class="card"><div class="card-head"><div><div class="card-title">Proprietário do automóvel</div><div class="sc-sub">Em nome de quem o automóvel fica registrado durante o contrato.</div></div></div><div class="card-pad"><div class="own-row">{opcao("user", "Cliente", "Mariana Costa", False)}{opcao("building", "Empresa", "Locadora Serra Verde", True)}{opcao("landmark", "Banco", "Banco Horizonte", False)}</div></div></div>'''
    credito = f'''<div class="card"><div class="card-head"><span class="card-title">Crédito associado</span>{badge("aprovado")}</div><div class="card-pad sum-lines">{linha_info("Banco", "Banco Horizonte")}{linha_info("Valor", brl(1323), True)}{linha_info("Parcelas", f"{PARCELAS}x de {brl(p)}", True)}{linha_info("Taxa", "1,90% ao mês", True)}</div></div>'''
    confirma = f'''<div class="card card-pad confirm"><div class="card-title">Executar contrato</div><p class="sub">Ao executar, o pedido passa para Contratado e fica somente leitura para o cliente.</p><div class="sum-lines">{linha_info("Nº do contrato", "CTR-2026-0087", True)}{linha_info("Proprietário", "Empresa")}{linha_info("Crédito", "Sim")}</div>{botao("Executar contrato", "primary", "file", bloco=True)}{botao("Voltar", "ghost", bloco=True)}</div>'''
    corpo = cabecalho("Executar contrato", "Pedido aprovado. Confira os dados e defina o proprietário do automóvel.", ["Pedidos para avaliar", "PED-1045", "Contrato"]) + f'<div class="cols"><div class="col-main gap">{resumo}{dono}</div><div class="col-side gap">{credito}{confirma}</div></div>'
    extra = f'<div class="toast-wrap">{toast("Contrato executado", "CTR-2026-0087 criado. O pedido PED-1045 agora está Contratado.")}</div>'
    return tela_app("12 Executar contrato", sidebar_agente("Pedidos para avaliar"), corpo, extra)


CONTRATOS = [
    ("CTR-2026-0087", "Mariana Costa", "polo", "Empresa", "Banco Horizonte", "28/10/2026"),
    ("CTR-2026-0081", "Mariana Costa", "onix", "Cliente", None, "30/09/2026"),
    ("CTR-2026-0079", "Pedro Almeida", "corolla", "Banco", "Banco Horizonte", "24/09/2026"),
    ("CTR-2026-0072", "Luiza Fernandes", "argo", "Empresa", None, "11/09/2026"),
    ("CTR-2026-0066", "Carlos Mendes", "renegade", "Empresa", "Banco Horizonte", "02/09/2026"),
]


def t13_contratos():
    cols = [("Contrato", 140), ("Cliente", 150), ("Automóvel", 0), ("Proprietário", 130), ("Crédito", 170), ("Data", 104), ("", 24)]
    linhas = ""
    for num, cli, k, dono, cred, dt in CONTRATOS:
        c = CARROS[k]
        cr = f'<span class="cred-yes">{icone("landmark", 14, "#1E40AF")}<span>{cred}</span></span>' if cred else '<span class="muted">Não</span>'
        linhas += f'''<div class="tr"><div class="td mono strong" style="width:140px">{num}</div><div class="td" style="width:150px">{cli}</div><div class="td" style="flex:1"><div class="car-cell">{thumb(k, 56, 38)}<div class="stack-xs"><div class="lr-title">{c["marca"]} {c["modelo"]} {c["ano"]}</div>{placa(c["placa"], "sm")}</div></div></div><div class="td" style="width:130px">{dono_pill(dono)}</div><div class="td" style="width:170px">{cr}</div><div class="td mono" style="width:104px">{dt}</div><div class="td" style="width:24px">{icone("chevright", 18, "#9A9DA6")}</div></div>'''
    chips = "".join(f'<span class="chip {"active" if i == 0 else ""}">{m}</span>' for i, m in enumerate(["Todos", "Com crédito", "Sem crédito"]))
    barra = f'<div class="table-bar pad"><div class="chips">{chips}</div><div class="search sm">{icone("search", 16, "#6B7080")}<span class="ph">Buscar contrato ou cliente</span></div></div>'
    corpo = cabecalho("Contratos", "Contratos executados a partir de pedidos aprovados.") + f'<div class="card table">{barra}{cab_tabela(cols)}{linhas}<div class="table-foot"><span class="muted">Mostrando 5 de 9 contratos</span><div class="pager"><div class="icon-btn sq sm">{icone("chevleft", 16, "#3F4450")}</div><span class="page-n active">1</span><span class="page-n">2</span><div class="icon-btn sq sm">{icone("chevright", 16, "#3F4450")}</div></div></div></div>'
    return tela_app("13 Contratos", sidebar_agente("Contratos"), corpo)


def t14_automoveis():
    cols = [("Automóvel", 0), ("Ano", 70), ("Placa", 120), ("Matrícula (RENAVAM)", 180), ("Proprietário", 140), ("", 48)]
    linhas = ""
    for k in ["argo", "polo", "onix", "hb20", "renegade", "corolla"]:
        c = CARROS[k]
        sel = " row-sel" if k == "polo" else ""
        linhas += f'''<div class="tr{sel}"><div class="td" style="flex:1"><div class="car-cell">{thumb(k, 64, 42)}<div><div class="lr-title">{c["marca"]} {c["modelo"]}</div><div class="lr-sub">Diária {brl(c["diaria"])}</div></div></div></div><div class="td mono" style="width:70px">{c["ano"]}</div><div class="td" style="width:120px">{placa(c["placa"], "sm")}</div><div class="td mono" style="width:180px">{c["renavam"]}</div><div class="td" style="width:140px">{dono_pill(c["dono"])}</div><div class="td" style="width:48px"><div class="icon-btn sq sm">{icone("pencil", 16, "#3F4450")}</div></div></div>'''
    corpo = cabecalho("Automóveis", "Frota da Locadora Serra Verde disponível para pedidos.", acoes=botao("Cadastrar automóvel", "primary", "plus")) + f'<div class="card table">{cab_tabela(cols)}{linhas}</div>'
    drawer = f'''<div class="overlay light"></div><div class="drawer"><div class="dr-head"><div><div class="card-title">Editar automóvel</div><div class="sc-sub">Volkswagen Polo 2024</div></div><div class="icon-btn sq">{icone("x", 18, "#3F4450")}</div></div><div class="dr-body"><div class="dr-preview" style="background:{CARROS["polo"]["tinta"]}">{carro_svg(CARROS["polo"]["cor"], 220)}<div class="dr-placa">{placa("RTA2B45", "lg")}</div></div>{campo("Matrícula (RENAVAM)", "01359872046", mono=True)}<div class="row2">{selecao("Marca", "Volkswagen")}{campo("Modelo", "Polo")}</div><div class="row2">{campo("Ano", "2024", mono=True)}{campo("Placa", "RTA2B45", mono=True, dica="Padrão Mercosul: ABC1D23")}</div><div class="field"><div class="label">Proprietário</div>{segmentado([("Cliente", "user"), ("Empresa", "building"), ("Banco", "landmark")], 1)}</div></div><div class="dr-foot">{botao("Cancelar", "secondary")}{botao("Salvar automóvel", "primary", "check")}</div></div>'''
    return tela_app("14 Automóveis", sidebar_agente("Automóveis"), corpo, drawer)


# ---------------------------------------------------------------- folha de componentes
def t00_componentes():
    cores = [("Fundo", "#F5F4F0"), ("Superfície", "#FFFFFF"), ("Borda", "#E3E1DA"), ("Grafite", "#15181E"), ("Texto 2", "#3F4450"), ("Texto suave", "#6B7080"), ("Primária", "#1F4FD8"), ("Destaque", "#F5B700"), ("Perigo", "#B42318")]
    sw = "".join(f'<div class="swatch"><div class="sw-color" style="background:{h}"></div><div class="sw-name">{n}</div><div class="sw-hex mono">{h}</div></div>' for n, h in cores)
    tipos = f'''<div class="type-list"><div class="type-row"><span class="type-meta mono">Sora 28/36 · 600</span><span style="font:600 28px/36px Sora">Título de página</span></div><div class="type-row"><span class="type-meta mono">Sora 16/24 · 600</span><span style="font:600 16px/24px Sora">Título de card</span></div><div class="type-row"><span class="type-meta mono">Inter 14/20 · 400</span><span style="font:400 14px/20px Inter">Texto de interface e formulários</span></div><div class="type-row"><span class="type-meta mono">Inter 12/16 · 500</span><span style="font:500 12px/16px Inter;color:#6B7080">Legenda e ajuda de campo</span></div><div class="type-row"><span class="type-meta mono">JetBrains Mono 14 · 500</span><span class="mono" style="font-size:14px">PED-1048 · 123.456.789-09 · R$ 1.729,00</span></div></div>'''
    estados = ["", "hover", "disabled", "loading"]
    nomes_est = ["Padrão", "Hover", "Desabilitado", "Carregando"]
    linhas_btn = ""
    for tipo, rot in [("primary", "Primary"), ("secondary", "Secondary"), ("ghost", "Ghost"), ("danger", "Danger")]:
        cel = "".join(f'<div class="cmp-cell">{botao("Botão", tipo, None if e == "loading" else "plus", estado=e)}</div>' for e in estados)
        linhas_btn += f'<div class="cmp-row"><div class="cmp-k">{rot}</div>{cel}</div>'
    head_btn = '<div class="cmp-row head"><div class="cmp-k"></div>' + "".join(f'<div class="cmp-cell muted">{n}</div>' for n in nomes_est) + "</div>"
    inputs = f'''<div class="cmp-grid4">{campo("Padrão", "Digite aqui", ph=True)}{campo("Foco", "mariana.costa", estado="focus")}{campo("Erro", "123.456.789-00", mono=True, erro="CPF inválido.")}{campo("Desabilitado", "123.456.789-09", mono=True, estado="disabled")}{selecao("Select", "Banco Horizonte", ic="landmark")}{data("DatePicker", "05/11/2026")}<div class="field"><div class="label">Toggle</div><div class="inline">{toggle(True)}{toggle(False)}</div></div><div class="field"><div class="label">Segmentado</div>{segmentado([("Cliente", "user"), ("Agente", "building")], 0)}</div></div>'''
    badges = "".join(badge(k) for k in STATUS)
    placas = placa("RTA2B45", "sm") + placa("RTA2B45") + placa("RTA2B45", "lg")
    chips = "".join(f'<span class="chip {"active" if i == 0 else ""}">{m}</span>' for i, m in enumerate(["Ativo", "Inativo", "Inativo"]))
    tabs = '<div class="tabs"><div class="tab active"><span>Todos</span><span class="tab-count">6</span></div><div class="tab"><span>Em análise</span><span class="tab-count">1</span></div><div class="tab"><span>Aprovado</span><span class="tab-count">1</span></div></div>'
    tl = timeline([("Enviado", "27/10 · 14:32", "done"), ("Em análise", "Desde 28/10", "cur"), ("Parecer", "Pendente", ""), ("Contrato", "Pendente", "")])
    st = stepper(PASSOS_CLIENTE, 2)
    cards = car_card("polo", True) + car_card("renegade", False)
    stat_ex = stat("Em análise", "2", "clock", "#92400E", "#FEF3C7", "Enviado ou em análise")
    toasts = toast("Contrato executado", "CTR-2026-0087 criado com sucesso.") + toast("Não foi possível salvar", "Confira os campos destacados.", "erro")
    modal = f'''<div class="modal static"><div class="modal-icon danger">{icone("trash", 22, "#B42318")}</div><div class="modal-title">Excluir sua conta?</div><p class="modal-text">Seu cadastro será apagado e os pedidos em análise serão cancelados. Contratos já executados continuam registrados.</p><div class="modal-actions">{botao("Manter conta", "secondary")}{botao("Excluir conta", "danger", "trash")}</div></div>'''
    vazio = f'''<div class="empty boxed"><div class="empty-icon">{icone("inbox", 28, "#3F4450")}</div><div class="empty-title">Nada por aqui ainda</div><p class="empty-text">Quando houver registros, eles aparecem nesta lista.</p>{botao("Criar novo", "primary", "plus")}</div>'''
    nav = f'''<div class="nav-demo"><div class="nav-item active"><div class="nav-bar"></div>{icone("home", 18, "#FFFFFF")}<span>Item ativo</span></div><div class="nav-item">{icone("clipboard", 18, "#9AA0AE")}<span>Item padrão</span></div><div class="nav-item">{icone("clipcheck", 18, "#9AA0AE")}<span>Com contador</span><span class="nav-count">4</span></div></div>'''

    def bloco(titulo, nome_cmp, conteudo):
        return f'<div class="cmp-block"><div class="cmp-title"><span>{titulo}</span><span class="cmp-name mono">{nome_cmp}</span></div>{conteudo}</div>'

    corpo = f'''<div class="sheet"><div class="sheet-head">{marca_rota(False)}<div><h1 class="big-h left">Componentes e estilos</h1><p class="sub">Base visual do Rota. Os nomes à direita são os componentes React correspondentes.</p></div></div>
{bloco("Cores", "tokens", f'<div class="swatches">{sw}</div>')}
{bloco("Tipografia", "Sora · Inter · JetBrains Mono", tipos)}
{bloco("Botões", "Button", head_btn + linhas_btn)}
{bloco("Campos", "Input · Select · DatePicker · Toggle", inputs)}
<div class="cmp-2">{bloco("Status do pedido", "StatusBadge", f'<div class="inline wrap">{badges}</div>')}{bloco("Placa Mercosul", "PlacaMercosul", f'<div class="inline">{placas}</div>')}</div>
<div class="cmp-2">{bloco("Filtros e abas", "Chip · Tabs", f'<div class="inline">{chips}</div><div style="height:16px"></div>{tabs}')}{bloco("Menu lateral", "Sidebar", nav)}</div>
{bloco("Etapas", "Stepper · Timeline", st + '<div style="height:28px"></div>' + tl)}
<div class="cmp-2">{bloco("Automóvel", "CarCard", f'<div class="inline top">{cards}</div>')}{bloco("Indicador", "StatCard", f'<div style="width:260px">{stat_ex}</div>')}</div>
<div class="cmp-2">{bloco("Avisos", "Toast", f'<div class="stack">{toasts}</div>')}{bloco("Confirmação", "Modal", modal)}</div>
{bloco("Estado vazio", "EmptyState", vazio)}
</div>'''
    return f'<section class="screen sheet-screen" id="00 Componentes" data-name="00 Componentes">{corpo}</section>'


# ---------------------------------------------------------------- CSS e montagem
CSS = r"""
@font-face{font-family:'Inter';src:url(data:font/woff2;base64,__INTER__) format('woff2');font-weight:400 700;font-style:normal}
@font-face{font-family:'Sora';src:url(data:font/woff2;base64,__SORA__) format('woff2');font-weight:400 700;font-style:normal}
@font-face{font-family:'JetBrains Mono';src:url(data:font/woff2;base64,__MONO__) format('woff2');font-weight:400 700;font-style:normal}
*{box-sizing:border-box;margin:0;padding:0}
svg{flex:none}
body{background:#D9D6CE;font-family:'Inter',sans-serif;color:#15181E;font-size:14px;line-height:20px;padding:48px;display:flex;flex-direction:column;gap:96px;align-items:flex-start}
.screen{width:1440px;min-height:900px;background:#F5F4F0;position:relative;overflow:hidden;display:flex;align-items:stretch;flex:none}
h1,h2,h3{font-family:'Sora',sans-serif;font-weight:600;letter-spacing:-0.01em}
.mono{font-family:'JetBrains Mono',monospace;font-weight:500}
.strong{font-weight:600}
.muted{color:#6B7080}
.link{color:#1F4FD8;font-weight:600}
.link.sm{font-size:13px}
.sub{color:#6B7080;font-size:14px;line-height:22px}
.sub.center{text-align:center}
.sep{height:1px;background:#EEECE6;width:100%}
.inline{display:flex;align-items:center;gap:12px}
.inline.wrap{flex-wrap:wrap}
.inline.top{align-items:flex-start}
.stack{display:flex;flex-direction:column;gap:12px}

/* marca */
.brand{display:flex;align-items:center;gap:10px}
.brand-mark{width:36px;height:36px;border-radius:10px;background:#F5B700;display:flex;align-items:center;justify-content:center;flex:none}
.brand-name{font-family:'Sora';font-weight:700;font-size:21px;line-height:28px;letter-spacing:-0.02em}

/* sidebar */
.sidebar{width:264px;flex:none;background:#15181E;display:flex;flex-direction:column;padding:24px 16px;gap:28px}
.sidebar .brand{padding:0 8px}
.nav{display:flex;flex-direction:column;gap:4px}
.nav-label{font-size:11px;line-height:16px;letter-spacing:0.08em;color:#7D8494;padding:0 12px 8px;font-weight:600}
.nav-item{display:flex;align-items:center;gap:12px;height:44px;padding:0 12px;border-radius:10px;font-size:14px;font-weight:500;color:#C9CDD6;position:relative;white-space:nowrap}
.nav-item.active{background:#252A35;color:#FFFFFF}
.nav-bar{position:absolute;left:0;top:11px;width:3px;height:22px;border-radius:2px;background:#F5B700}
.nav-count{margin-left:auto;background:#F5B700;color:#15181E;font-size:12px;line-height:18px;font-weight:700;border-radius:999px;padding:1px 7px}
.side-foot{margin-top:auto}
.user-card{display:flex;align-items:center;gap:12px;padding:12px;border-radius:12px;background:#1E222A}
.user-txt{flex:1}
.user-name{color:#FFFFFF;font-size:14px;font-weight:600}
.user-role{color:#9AA0AE;font-size:12px;line-height:16px}
.avatar{border-radius:50%;display:flex;align-items:center;justify-content:center;font-family:'Sora';font-weight:600;flex:none}
.inst{display:flex;align-items:center;gap:12px;padding:12px;border-radius:12px;background:#1E222A;border:1px solid #2A2F3A}
.inst-icon{width:36px;height:36px;border-radius:10px;background:#2A2F3A;display:flex;align-items:center;justify-content:center;flex:none}
.inst-name{color:#FFFFFF;font-size:14px;font-weight:600;line-height:20px}
.inst-tag{display:inline-flex;margin-top:4px;font-size:11px;line-height:16px;font-weight:700;color:#15181E;background:#F5B700;border-radius:999px;padding:0 8px}
.nav-demo{background:#15181E;border-radius:12px;padding:12px;display:flex;flex-direction:column;gap:4px;width:300px}

/* principal */
.main{flex:1;display:flex;flex-direction:column;min-width:0}
.topbar{height:68px;flex:none;display:flex;align-items:center;justify-content:space-between;padding:0 40px;border-bottom:1px solid #E3E1DA}
.search{width:420px;height:40px;border-radius:10px;background:#FFFFFF;border:1px solid #E3E1DA;display:flex;align-items:center;gap:10px;padding:0 14px}
.search.sm{width:280px;height:38px}
.ph{color:#8E929C}
.top-right{display:flex;align-items:center;gap:16px}
.top-date{color:#6B7080;font-size:13px}
.icon-btn{width:40px;height:40px;border-radius:10px;border:1px solid #E3E1DA;background:#FFFFFF;display:flex;align-items:center;justify-content:center;position:relative;flex:none}
.icon-btn.sq.sm{width:32px;height:32px;border-radius:8px}
.bell-dot{position:absolute;top:9px;right:10px;width:8px;height:8px;border-radius:50%;background:#F5B700;border:2px solid #FFFFFF}
.content{padding:32px 40px 48px;display:flex;flex-direction:column;gap:24px}
.page-head{display:flex;align-items:flex-end;justify-content:space-between;gap:24px}
.ph-left{display:flex;flex-direction:column;gap:6px}
.ph-title{display:flex;align-items:center;gap:12px}
.ph-title h1{font-size:28px;line-height:36px}
.ph-actions{display:flex;gap:12px;align-items:center}
.crumbs{display:flex;align-items:center;gap:6px;font-size:13px}
.crumb{color:#6B7080}
.crumb-cur{color:#15181E;font-weight:600}

/* cards */
.card{background:#FFFFFF;border:1px solid #E3E1DA;border-radius:12px;box-shadow:0 1px 2px rgba(21,24,30,0.04)}
.card-pad{padding:24px}
.card-head{display:flex;align-items:center;justify-content:space-between;padding:18px 24px;border-bottom:1px solid #EEECE6;gap:12px}
.card-title{font-family:'Sora';font-weight:600;font-size:16px;line-height:24px}
.card-title.sm{font-size:14px;line-height:20px}
.top-line{border-top:1px solid #EEECE6}
.cols{display:flex;gap:24px;align-items:flex-start}
.col-main{flex:1;display:flex;flex-direction:column;gap:24px;min-width:0}
.col-side{width:340px;flex:none;display:flex;flex-direction:column;gap:24px}
.col-side.wide{width:372px}
.col-side.narrow{width:280px}
.gap{gap:24px}

/* botões */
.btn{height:44px;padding:0 18px;border-radius:10px;display:inline-flex;align-items:center;justify-content:center;gap:8px;font-size:14px;font-weight:600;border:1px solid transparent;white-space:nowrap;flex:none}
.btn-primary{background:#1F4FD8;color:#FFFFFF}
.btn-primary.hover{background:#1A43B8}
.btn-secondary{background:#FFFFFF;color:#15181E;border-color:#D3D0C7}
.btn-secondary.hover{background:#F5F4F0}
.btn-ghost{background:transparent;color:#1F4FD8}
.btn-ghost.hover{background:#E8EEFC}
.btn-danger{background:#B42318;color:#FFFFFF}
.btn-danger.hover{background:#931C13}
.btn-danger-outline{background:#FFFFFF;color:#B42318;border-color:#F1B5AE}
.btn-amber{background:#F5B700;color:#15181E}
.btn.disabled{background:#E9E7E1;color:#8E929C;border-color:#E9E7E1}
.btn.loading{opacity:0.8}
.btn-sm{height:36px;padding:0 14px;font-size:13px}
.btn-block{width:100%}

/* campos */
.field{display:flex;flex-direction:column;gap:6px;flex:1;min-width:0}
.label{font-size:13px;line-height:18px;font-weight:600;color:#3F4450}
.input{height:44px;border:1px solid #D3D0C7;background:#FFFFFF;border-radius:10px;padding:0 14px;display:flex;align-items:center;gap:10px;font-size:14px}
.input .val{flex:1}
.input.focus{border-color:#1F4FD8;box-shadow:0 0 0 3px #D6E0FA}
.input.error{border-color:#B42318;box-shadow:0 0 0 3px #FDE3E0}
.input.disabled{background:#EFEDE7;color:#6B7080;border-color:#E3E1DA}
.textarea{height:auto;min-height:104px;align-items:flex-start;padding:12px 14px;line-height:21px}
.help{font-size:12px;line-height:16px;color:#6B7080;display:flex;gap:6px;align-items:center}
.help.err{color:#B42318;font-weight:500}
.help.warn{color:#92400E;font-weight:500}
.toggle{width:44px;height:24px;border-radius:999px;background:#C9C6BD;position:relative;flex:none}
.toggle .knob{position:absolute;top:3px;left:3px;width:18px;height:18px;border-radius:50%;background:#FFFFFF;box-shadow:0 1px 2px rgba(0,0,0,0.2)}
.toggle.on{background:#1F4FD8}
.toggle.on .knob{left:23px}
.seg{display:flex;background:#EFEDE7;border-radius:10px;padding:4px;gap:4px}
.seg-item{flex:1;height:36px;border-radius:8px;display:flex;align-items:center;justify-content:center;gap:8px;font-size:14px;font-weight:600;color:#6B7080}
.seg-item.active{background:#FFFFFF;color:#15181E;box-shadow:0 1px 3px rgba(21,24,30,0.12)}
.row2{display:flex;gap:16px}
.row3{display:flex;gap:16px;align-items:flex-end}
.row-addr{display:flex;gap:16px}
.form-grid{display:flex;flex-direction:column;gap:18px}
.chips{display:flex;gap:8px;flex-wrap:wrap}
.chip{height:34px;padding:0 14px;border-radius:999px;border:1px solid #D3D0C7;background:#FFFFFF;font-size:13px;font-weight:500;display:inline-flex;align-items:center;color:#3F4450}
.chip.active{background:#15181E;color:#FFFFFF;border-color:#15181E}
.pill{display:inline-flex;align-items:center;gap:6px;height:28px;padding:0 10px;border-radius:8px;background:#F2F0EA;font-size:13px;font-weight:500;color:#3F4450}

/* badge e placa */
.badge{display:inline-flex;align-items:center;gap:6px;height:26px;padding:0 10px;border-radius:999px;font-size:12px;line-height:16px;font-weight:600;white-space:nowrap}
.badge .dot{width:6px;height:6px;border-radius:50%}
.placa{display:inline-flex;flex-direction:column;width:104px;border:1.5px solid #1F2937;border-radius:5px;background:#FFFFFF;overflow:hidden;flex:none}
.placa-top{height:11px;background:#1E3A8A;color:#FFFFFF;font-size:7px;line-height:11px;font-weight:700;letter-spacing:0.14em;text-align:center}
.placa-num{font-family:'JetBrains Mono';font-weight:700;font-size:15px;line-height:22px;text-align:center;letter-spacing:0.06em;color:#15181E}
.placa.sm{width:82px}
.placa.sm .placa-top{height:9px;font-size:6px;line-height:9px}
.placa.sm .placa-num{font-size:12px;line-height:18px}
.placa.lg{width:150px;border-width:2px;border-radius:7px}
.placa.lg .placa-top{height:15px;font-size:9px;line-height:15px}
.placa.lg .placa-num{font-size:22px;line-height:34px}

/* stepper e timeline */
.stepper{display:flex;align-items:center;gap:12px;width:100%}
.step{display:flex;align-items:center;gap:10px;font-size:14px;font-weight:600;color:#6B7080;white-space:nowrap}
.step-num{width:28px;height:28px;border-radius:50%;border:1.5px solid #C9C6BD;display:flex;align-items:center;justify-content:center;font-size:13px;background:#FFFFFF;color:#6B7080;flex:none}
.step.done{color:#15181E}
.step.done .step-num{background:#15181E;border-color:#15181E}
.step.active{color:#1F4FD8}
.step.active .step-num{background:#1F4FD8;border-color:#1F4FD8;color:#FFFFFF}
.step-line{flex:1;height:2px;background:#E3E1DA;min-width:24px}
.step-line.done{background:#15181E}
.timeline{display:flex}
.tl-step{flex:1;display:flex;flex-direction:column;gap:6px}
.tl-track{display:flex;align-items:center}
.tl-dot{width:28px;height:28px;border-radius:50%;border:2px solid #D3D0C7;background:#FFFFFF;display:flex;align-items:center;justify-content:center;flex:none}
.tl-dot.done{background:#166534;border-color:#166534}
.tl-dot.cur{border-color:#D97706;background:#FEF3C7}
.tl-inner{width:10px;height:10px;border-radius:50%;background:#D97706}
.tl-line{flex:1;height:2px;background:#E3E1DA;margin:0 8px}
.tl-line.done{background:#166534}
.tl-title{font-weight:600;font-size:14px;color:#6B7080;margin-top:6px}
.tl-title.done{color:#15181E}
.tl-title.cur{color:#92400E}
.tl-sub{font-size:12px;line-height:16px;color:#6B7080}

/* stat */
.stats{display:flex;gap:24px}
.stats .stat{flex:1}
.stat{padding:20px;display:flex;flex-direction:column;gap:8px}
.stat-top{display:flex;justify-content:space-between;align-items:center}
.stat-label{font-size:13px;font-weight:600;color:#3F4450}
.stat-icon{width:36px;height:36px;border-radius:10px;display:flex;align-items:center;justify-content:center}
.stat-num{font-family:'Sora';font-weight:600;font-size:32px;line-height:40px}
.stat-note{font-size:12px;line-height:16px;color:#6B7080}

/* listas e tabelas */
.list{display:flex;flex-direction:column}
.list-row{display:flex;align-items:center;gap:16px;padding:14px 24px;border-top:1px solid #EEECE6}
.list-row:first-child{border-top:none}
.lr-main{flex:1}
.lr-title{font-weight:600;font-size:14px}
.lr-sub{font-size:12px;line-height:16px;color:#6B7080;margin-top:2px}
.lr-val{width:110px;text-align:right}
.thumb{border-radius:8px;display:flex;align-items:center;justify-content:center;flex:none}
.table{overflow:hidden}
.table-bar{display:flex;align-items:flex-end;justify-content:space-between;padding:12px 24px 0;border-bottom:1px solid #E3E1DA}
.table-bar .search{margin-bottom:10px}
.table-bar.pad{align-items:center;padding:14px 24px}
.table-bar.pad .search{margin-bottom:0}
.tabs{display:flex;gap:4px}
.tab{padding:12px 10px;font-size:14px;font-weight:500;color:#6B7080;display:flex;gap:8px;align-items:center;border-bottom:2px solid transparent;white-space:nowrap;flex:none}
.table-bar .search.sm{width:240px;flex:none}
.tab.active{color:#15181E;border-bottom-color:#1F4FD8;font-weight:600}
.tab-count{background:#EFEDE7;border-radius:999px;font-size:12px;line-height:18px;padding:0 8px;color:#3F4450;font-weight:600}
.tr{display:flex;align-items:center;gap:16px;padding:0 24px;min-height:68px;border-top:1px solid #EEECE6}
.tr.th{min-height:44px;background:#FAF9F6;font-size:12px;font-weight:600;color:#6B7080;border-top:none;border-bottom:1px solid #EEECE6}
.tr.th + .tr{border-top:none}
.tr.row-sel{background:#F3F6FE}
.td{flex:none;min-width:0}
.car-cell{display:flex;align-items:center;gap:12px}
.stack-xs{display:flex;flex-direction:column;gap:4px;align-items:flex-start}
.lr-title{white-space:nowrap}
.cli-cell{display:flex;align-items:center;gap:10px}
.acts{display:flex;gap:6px}
.table-foot{display:flex;justify-content:space-between;align-items:center;padding:16px 24px;border-top:1px solid #EEECE6;font-size:13px}
.pager{display:flex;align-items:center;gap:6px}
.page-n{width:32px;height:32px;border-radius:8px;display:flex;align-items:center;justify-content:center;font-weight:600;font-size:13px;color:#3F4450}
.page-n.active{background:#15181E;color:#FFFFFF}
.cred-yes{display:inline-flex;align-items:center;gap:6px;color:#1E40AF;font-weight:600;font-size:13px}

/* info */
.info-row{display:flex;align-items:center;justify-content:space-between;gap:10px;font-size:14px;padding:3px 0}
.info-k{color:#6B7080}
.info-k.grow{flex:1;color:#15181E}
.info-v{font-weight:600;text-align:right}
.sum-lines{display:flex;flex-direction:column;gap:8px}
.sum-total{display:flex;justify-content:space-between;align-items:center;padding-top:12px;margin-top:4px;border-top:1px dashed #D3D0C7;font-weight:600}
.sum-total-v{font-size:20px;line-height:28px;font-weight:700}
.total-box{display:flex;justify-content:space-between;align-items:center;padding:14px 16px;border-radius:10px;background:#F5F4F0;font-weight:600}
.total-v{font-size:18px;font-weight:700}
.note{display:flex;gap:10px;align-items:flex-start;padding:12px;border-radius:10px;background:#F5F4F0;font-size:13px;line-height:19px;color:#3F4450}
.count-pill{background:#EFEDE7;border-radius:999px;font-size:12px;line-height:20px;padding:0 10px;font-weight:600;color:#3F4450}
.sec-label{display:flex;align-items:center;gap:10px;font-weight:600;font-size:14px}

/* público */
.pub{display:block}
.pub-wrap{width:100%;display:flex;flex-direction:column}
.pub-top{height:76px;display:flex;align-items:center;justify-content:space-between;padding:0 48px;border-bottom:1px solid #E3E1DA}
.pub-right{display:flex;gap:6px;font-size:14px}
.center-col{width:900px;margin:0 auto;padding:56px 0 48px;display:flex;flex-direction:column;align-items:center;gap:12px}
.eyebrow{font-size:12px;line-height:16px;letter-spacing:0.12em;font-weight:700;color:#F5B700}
.eyebrow.dark{color:#1F4FD8}
.big-h{font-size:36px;line-height:44px;text-align:center}
.big-h.left{text-align:left}
.pick-row{display:flex;gap:24px;margin-top:28px;width:100%}
.pick{flex:1;background:#FFFFFF;border:1px solid #E3E1DA;border-radius:16px;padding:28px;position:relative;display:flex;flex-direction:column;gap:10px}
.pick.selected{border:2px solid #1F4FD8;box-shadow:0 0 0 5px #E3EAFC}
.pick-check{position:absolute;top:22px;right:22px;width:26px;height:26px;border-radius:50%;background:#1F4FD8;display:flex;align-items:center;justify-content:center}
.pick-radio{position:absolute;top:22px;right:22px;width:26px;height:26px;border-radius:50%;border:2px solid #D3D0C7;background:#FFFFFF}
.pick-check.sm,.pick-radio.sm{top:14px;right:14px;width:22px;height:22px}
.pick-icon{width:56px;height:56px;border-radius:14px;display:flex;align-items:center;justify-content:center}
.pick-title{font-family:'Sora';font-weight:600;font-size:22px;line-height:30px;margin-top:6px}
.pick-text{color:#6B7080;font-size:14px;line-height:22px}
.pick-list{display:flex;flex-direction:column;gap:8px;margin-top:8px}
.li{display:flex;gap:10px;align-items:center;font-size:14px;color:#3F4450}
.actions-row{display:flex;justify-content:space-between;width:100%;margin-top:28px}
.signup{width:1120px;margin:0 auto;padding:40px 0 48px;display:flex;flex-direction:column;gap:28px}
.signup-head{display:flex;flex-direction:column;gap:12px}
.signup-head .stepper{margin-top:12px}
.signup-body{display:flex;gap:24px;align-items:flex-start}
.signup-body.single{justify-content:flex-start}
.form-card{flex:1;padding:0}
.form-card.narrow{max-width:640px}
.form-card-head{padding:24px 28px 0;display:flex;flex-direction:column;gap:4px}
.form-card .form-grid{padding:24px 28px}
.card-h{font-size:20px;line-height:28px}
.form-foot{display:flex;justify-content:space-between;padding:18px 28px;border-top:1px solid #EEECE6}
.aside-note{width:340px;flex:none;background:#EFEDE6;border-radius:16px;padding:24px;display:flex;flex-direction:column;gap:10px}
.an-title{font-family:'Sora';font-weight:600;font-size:16px}
.an-text{color:#3F4450;font-size:14px;line-height:22px}
.an-list{display:flex;flex-direction:column;gap:8px;margin-top:6px}
.mini-steps{display:flex;flex-direction:column;gap:12px;margin-top:6px}
.ms{display:flex;gap:10px;align-items:center;font-size:14px;color:#6B7080}
.ms.done{color:#15181E}
.ms.cur{color:#1F4FD8;font-weight:600}
.rendas{display:flex;flex-direction:column;gap:12px}
.renda-row{display:flex;gap:12px;align-items:flex-end}
.renda-n{width:28px;height:44px;display:flex;align-items:center;justify-content:center;font-family:'Sora';font-weight:600;color:#6B7080;flex:none}
.renda-row .icon-btn{width:44px;height:44px}
.add-row{display:flex;align-items:center;gap:14px}

/* login */
.login{display:flex}
.login-left{width:640px;flex:none;background:#15181E;padding:44px 56px;display:flex;flex-direction:column;gap:36px}
.login-copy{display:flex;flex-direction:column;gap:14px;margin-top:20px}
.hero-h1{color:#FFFFFF;font-size:40px;line-height:48px;letter-spacing:-0.02em}
.hero-p{color:#A9AFBD;font-size:16px;line-height:26px}
.hero-art{height:270px;border-radius:20px;background:#1E222A;border:1px solid #2A2F3A;position:relative;overflow:hidden}
.road{position:absolute;left:0;right:0;bottom:0;height:78px;background:#262B35}
.road-line{position:absolute;left:24px;top:37px;display:flex;gap:20px}
.dash{width:28px;height:4px;border-radius:2px;background:#F5B700;display:block}
.hero-car{position:absolute;left:150px;bottom:36px}
.float-card{position:absolute;left:24px;top:24px;width:250px;background:#FFFFFF;border-radius:14px;padding:14px 16px;box-shadow:0 12px 30px rgba(0,0,0,0.35);display:flex;flex-direction:column;gap:4px}
.fc-top{display:flex;justify-content:space-between;align-items:center}
.fc-id{font-size:13px;font-weight:700}
.fc-car{font-weight:600;font-size:14px;margin-top:4px}
.fc-sub{font-size:12px;line-height:16px;color:#6B7080}
.float-placa{position:absolute;right:28px;top:30px;transform:rotate(4deg)}
.feats{display:flex;flex-direction:column;gap:12px}
.feat{display:flex;gap:10px;align-items:center;color:#C9CDD6;font-size:14px}
.login-right{flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;position:relative}
.login-form{width:420px;display:flex;flex-direction:column;gap:18px}
.form-h{font-size:32px;line-height:40px}
.demo{display:flex;flex-direction:column;gap:8px;padding:14px;border-radius:12px;background:#FFFFFF;border:1px dashed #D3D0C7}
.demo-label{display:flex;justify-content:space-between;align-items:center;font-size:13px;font-weight:600;color:#3F4450}
.tag-demo{font-size:11px;line-height:16px;font-weight:700;color:#92400E;background:#FEF3C7;border-radius:999px;padding:0 8px}
.alert-err{display:flex;gap:12px;align-items:flex-start;padding:14px;border-radius:10px;background:#FEF3F2;border:1px solid #F7C4BE}
.alert-title{font-weight:600;color:#912018}
.alert-sub{font-size:13px;color:#B42318}
.divider{height:1px;background:#E3E1DA}
.center-txt{text-align:center;font-size:14px}
.login-foot{position:absolute;bottom:28px;font-size:12px;color:#8E929C}

/* início */
.next{padding:24px;display:flex;flex-direction:column;gap:10px;align-items:flex-start;border-left:4px solid #166534}
.next-tag{display:flex;gap:8px;align-items:center;font-size:12px;font-weight:700;color:#166534;letter-spacing:0.02em}
.next-title{font-family:'Sora';font-weight:600;font-size:17px;line-height:24px}
.next-text{font-size:14px;line-height:22px;color:#3F4450}
.promo{background:#15181E;border-radius:12px;padding:24px;position:relative;overflow:hidden;height:236px}
.promo-txt{position:relative;display:flex;flex-direction:column;gap:10px;align-items:flex-start;width:230px}
.promo-title{font-family:'Sora';font-weight:600;font-size:20px;line-height:28px;color:#FFFFFF}
.promo-p{color:#A9AFBD;font-size:13px;line-height:20px}
.promo-car{position:absolute;right:-56px;bottom:14px}

/* novo pedido */
.step-card{padding:24px;display:flex;flex-direction:column;gap:20px}
.sc-head{display:flex;gap:14px;align-items:center}
.sc-num{width:32px;height:32px;border-radius:50%;background:#15181E;color:#FFFFFF;font-family:'Sora';font-weight:600;display:flex;align-items:center;justify-content:center;flex:none}
.sc-sub{font-size:13px;color:#6B7080;line-height:18px}
.filters{display:flex;justify-content:space-between;align-items:center;gap:16px}
.car-grid{display:flex;flex-wrap:wrap;gap:16px}
.car-card{background:#FFFFFF;border:1px solid #E3E1DA;border-radius:12px;overflow:hidden;flex:none}
.car-card.selected{border:2px solid #1F4FD8;box-shadow:0 0 0 4px #E3EAFC}
.car-img{height:132px;display:flex;align-items:center;justify-content:center;position:relative;padding-top:22px}
.car-placa{position:absolute;left:10px;top:10px}
.car-check{position:absolute;top:10px;right:10px;width:26px;height:26px;border-radius:50%;background:#1F4FD8;display:flex;align-items:center;justify-content:center}
.car-body{padding:12px 14px 14px;display:flex;flex-direction:column;gap:2px}
.car-row{display:flex;justify-content:space-between;align-items:center;gap:8px}
.car-name{font-family:'Sora';font-weight:600;font-size:15px;line-height:22px}
.car-meta{font-size:12px;color:#6B7080}
.car-meta b{color:#15181E;font-weight:600}
.days-box{height:44px;padding:0 18px;border-radius:10px;background:#F5F4F0;display:flex;align-items:center;gap:8px;font-weight:600;color:#3F4450;flex:none}
.days-n{font-size:20px;color:#15181E}
.toggle-row{display:flex;gap:14px;align-items:center;padding:14px 16px;border-radius:10px;background:#F3F6FE;border:1px solid #D6E0FA}
.tr-title{font-weight:600}
.tr-sub{font-size:13px;color:#3F4450}
.summary{padding:0;overflow:hidden;display:flex;flex-direction:column}
.sum-head{padding:18px 24px;border-bottom:1px solid #EEECE6}
.sum-car{height:150px;display:flex;align-items:center;justify-content:center}
.sum-name-row{display:flex;justify-content:space-between;align-items:center;padding:16px 24px 0}
.sum-name{font-family:'Sora';font-weight:600;font-size:18px;line-height:26px}
.summary .sum-lines{padding:16px 24px 0}
.summary .note{margin:16px 24px 0}
.summary .sum-total{margin:12px 24px 0}
.sc-head .search{margin-left:auto}
.summary .btn-primary{margin:16px 24px 0;width:auto}
.summary .btn-ghost{margin:4px 24px 16px;width:auto}

/* detalhe */
.auto-box{display:flex;gap:24px;padding:24px;align-items:center}
.auto-img{width:280px;height:170px;border-radius:12px;display:flex;align-items:center;justify-content:center;flex:none}
.auto-info{flex:1;display:flex;flex-direction:column;gap:10px;align-items:flex-start}
.auto-info .sum-lines{width:100%;margin-top:6px}
.wait{display:flex;gap:14px;align-items:flex-start;padding:16px;border-radius:10px;background:#FFFBEB;border:1px solid #FDE68A}
.wait-title{font-weight:600;color:#92400E}
.wait-text{font-size:13px;line-height:20px;color:#3F4450;margin-top:2px}

/* overlay, modal, drawer, toast */
.overlay{position:absolute;left:0;top:0;width:100%;height:100%;background:rgba(21,24,30,0.55);display:flex;align-items:center;justify-content:center}
.overlay.light{background:rgba(21,24,30,0.35)}
.modal{width:500px;background:#FFFFFF;border-radius:16px;padding:28px;box-shadow:0 24px 60px rgba(21,24,30,0.3);display:flex;flex-direction:column;gap:14px}
.modal.static{box-shadow:0 12px 32px rgba(21,24,30,0.14);border:1px solid #E3E1DA;width:460px}
.modal-icon{width:48px;height:48px;border-radius:12px;display:flex;align-items:center;justify-content:center}
.modal-icon.danger{background:#FEE4E2}
.modal-title{font-family:'Sora';font-weight:600;font-size:20px;line-height:28px}
.modal-text{color:#3F4450;font-size:14px;line-height:22px}
.modal-actions{display:flex;justify-content:flex-end;gap:12px;margin-top:6px}
.drawer{position:absolute;right:0;top:0;width:480px;height:100%;background:#FFFFFF;box-shadow:-16px 0 40px rgba(21,24,30,0.18);display:flex;flex-direction:column}
.dr-head{display:flex;justify-content:space-between;align-items:center;padding:20px 24px;border-bottom:1px solid #EEECE6}
.dr-body{flex:1;padding:24px;display:flex;flex-direction:column;gap:18px}
.dr-preview{height:150px;border-radius:12px;display:flex;align-items:center;justify-content:center;position:relative}
.dr-placa{position:absolute;right:14px;bottom:12px}
.dr-foot{display:flex;justify-content:flex-end;gap:12px;padding:18px 24px;border-top:1px solid #EEECE6}
.toast-wrap{position:absolute;top:84px;right:40px}
.toast{width:420px;background:#15181E;color:#FFFFFF;border-radius:12px;padding:16px;display:flex;gap:12px;align-items:flex-start;box-shadow:0 16px 40px rgba(21,24,30,0.28)}
.toast-txt{flex:1}
.toast-title{font-weight:600}
.toast-sub{font-size:13px;color:#C9CDD6;line-height:19px}

/* perfil */
.profile-card{overflow:hidden}
.pc-top{display:flex;flex-direction:column;align-items:center;gap:6px;padding:28px 20px 20px;border-bottom:1px solid #EEECE6}
.pc-name{font-family:'Sora';font-weight:600;font-size:18px;margin-top:6px}
.pc-sub{color:#6B7080;font-size:13px}
.pc-nav{padding:10px;display:flex;flex-direction:column;gap:2px}
.pc-link{display:flex;gap:10px;align-items:center;height:40px;padding:0 12px;border-radius:8px;font-size:14px;color:#3F4450;font-weight:500}
.pc-link.active{background:#F5F4F0;color:#15181E;font-weight:600}
.pc-link.danger{color:#B42318}
.save-bar{display:flex;justify-content:space-between;align-items:center;padding:14px 20px;border-radius:12px;background:#FFFFFF;border:1px solid #D6E0FA;box-shadow:0 8px 24px rgba(31,79,216,0.08)}
.danger-zone{display:flex;justify-content:space-between;align-items:center;gap:24px;padding:24px;border-color:#F7C4BE;background:#FFFBFA}
.dz-title{font-family:'Sora';font-weight:600;font-size:16px;color:#912018}
.dz-text{font-size:14px;line-height:22px;color:#3F4450;margin-top:4px;max-width:560px}

/* painel e avaliação */
.side-kpi{display:flex;flex-direction:column;gap:14px}
.bar{display:flex;height:10px;border-radius:999px;overflow:hidden;background:#EFEDE7}
.bar.thin{height:8px}
.bar-ok{background:#16A34A;height:100%}
.bar-no{background:#DC2626;height:100%}
.bar-rest{flex:1}
.legend{display:flex;gap:16px;font-weight:600}
.legend span{display:flex;align-items:center;gap:6px}
.cli-head{display:flex;gap:14px;align-items:center;margin-bottom:16px}
.grid2{display:flex;gap:24px}
.grid2 .info-row{flex:1}
.ratio{margin-top:14px;display:flex;flex-direction:column;gap:8px}
.ratio-top{display:flex;justify-content:space-between;font-size:13px;color:#3F4450}
.card .sec-label{margin:14px 0 6px}
.card-pad .total-box{margin-top:10px}
.decision{overflow:hidden;border-color:#D6E0FA;box-shadow:0 12px 32px rgba(31,79,216,0.08)}
.dec-head{padding:20px 24px;border-bottom:1px solid #EEECE6;background:#F7F9FE}
.dec-body{padding:20px 24px;display:flex;flex-direction:column;gap:16px}
.verdict{display:flex;gap:12px}
.vd{flex:1;height:52px;border-radius:12px;border:1px solid #D3D0C7;display:flex;align-items:center;justify-content:center;gap:10px;font-weight:600;color:#6B7080;background:#FFFFFF}
.vd.ok.selected{border:2px solid #16A34A;background:#F0FDF4;color:#166534}
.credit-head{display:flex;flex-direction:column;gap:8px;align-items:flex-start}
.inst-chip{display:inline-flex;align-items:center;gap:6px;font-size:12px;font-weight:600;color:#1E40AF;background:#DBEAFE;border-radius:999px;padding:3px 10px}
.calc{display:flex;justify-content:space-between;align-items:center;padding:12px 14px;border-radius:10px;background:#F5F4F0;font-size:14px}
.dec-foot{padding:16px 24px 24px;display:flex;flex-direction:column;gap:10px}

/* contrato */
.parecer-ok{display:flex;gap:12px;align-items:flex-start;padding:14px 16px;border-radius:10px;background:#F0FDF4;border:1px solid #BBF7D0}
.po-title{font-weight:600;color:#166534}
.po-text{font-size:13px;line-height:20px;color:#3F4450;margin-top:2px}
.own-row{display:flex;gap:16px}
.own{flex:1;border:1px solid #D3D0C7;border-radius:12px;padding:18px;position:relative;display:flex;flex-direction:column;gap:6px;background:#FFFFFF}
.own.selected{border:2px solid #1F4FD8;background:#F7F9FE}
.own-icon{width:40px;height:40px;border-radius:10px;background:#EFEDE7;display:flex;align-items:center;justify-content:center}
.own.selected .own-icon{background:#E8EEFC}
.own-title{font-weight:600;font-size:15px;margin-top:4px}
.own-sub{font-size:13px;color:#6B7080}
.confirm{display:flex;flex-direction:column;gap:14px}

/* estado vazio */
.empty{display:flex;flex-direction:column;align-items:center;gap:12px;padding:80px 24px 96px;text-align:center}
.empty.boxed{background:#FFFFFF;border:1px dashed #D3D0C7;border-radius:12px;padding:40px 24px}
.empty-icon{width:64px;height:64px;border-radius:16px;background:#EFEDE7;display:flex;align-items:center;justify-content:center}
.empty-title{font-family:'Sora';font-weight:600;font-size:18px;margin-top:6px}
.empty-text{color:#6B7080;max-width:420px;line-height:22px}

/* folha de componentes */
.sheet-screen{display:block}
.sheet{padding:56px 64px 72px;display:flex;flex-direction:column;gap:28px}
.sheet-head{display:flex;flex-direction:column;gap:20px;padding-bottom:12px;border-bottom:1px solid #E3E1DA}
.cmp-block{background:#FFFFFF;border:1px solid #E3E1DA;border-radius:16px;padding:24px 28px;display:flex;flex-direction:column;gap:18px;flex:1}
.cmp-title{display:flex;justify-content:space-between;align-items:center;font-family:'Sora';font-weight:600;font-size:16px}
.cmp-name{font-size:12px;color:#1F4FD8;background:#E8EEFC;padding:2px 10px;border-radius:999px}
.cmp-2{display:flex;gap:28px;align-items:stretch}
.swatches{display:flex;gap:14px}
.swatch{flex:1;display:flex;flex-direction:column;gap:6px}
.sw-color{height:72px;border-radius:10px;border:1px solid #E3E1DA}
.sw-name{font-weight:600;font-size:13px}
.sw-hex{font-size:12px;color:#6B7080}
.type-list{display:flex;flex-direction:column;gap:14px}
.type-row{display:flex;align-items:center;gap:24px}
.type-meta{width:220px;font-size:12px;color:#6B7080;flex:none}
.cmp-row{display:flex;align-items:center;gap:16px;padding:8px 0;border-top:1px solid #F2F0EA}
.cmp-row.head{border-top:none;padding-top:0;font-size:12px;font-weight:600}
.cmp-k{width:120px;font-weight:600;font-size:13px;color:#3F4450;flex:none}
.cmp-cell{flex:1}
.cmp-grid4{display:flex;flex-wrap:wrap;gap:20px}
.cmp-grid4 .field{flex:none;width:300px}
"""


def documento(titulo, telas):
    fontes = {k: base64.b64encode((FONTES / f"{k.lower()}.woff2").read_bytes()).decode() for k in ("INTER", "SORA", "MONO")}
    css = CSS
    for k, v in fontes.items():
        css = css.replace(f"__{k}__", v)
    return f'<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><title>{titulo}</title><style>{css}</style></head><body>{"".join(telas)}</body></html>'


def main():
    grupos = {
        "publico": [t01_login(), t01_login(True), t02_perfil(), t03_dados(), t03b_renda(), t04_agente()],
        "cliente": [t05_inicio(), t06_novo_pedido(), t07_meus_pedidos(), t07_meus_pedidos(True), t08_detalhe(), t08_detalhe(True), t09_perfil()],
        "agente": [t10_painel(), t11_avaliar(), t12_executar(), t13_contratos(), t14_automoveis()],
        "componentes": [t00_componentes()],
    }
    saida = AQUI / "html"
    saida.mkdir(exist_ok=True)
    todas = []
    for nome, telas in grupos.items():
        (saida / f"{nome}.html").write_text(documento(f"Rota · {nome}", telas), encoding="utf-8")
        todas += telas
    (AQUI / "wireframes.html").write_text(documento("Rota · wireframes", todas), encoding="utf-8")
    print("ok:", {k: len(v) for k, v in grupos.items()})


if __name__ == "__main__":
    main()
