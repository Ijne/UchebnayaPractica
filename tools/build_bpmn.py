"""Generate importable BPMN 2.0 files with BPMN-DI and matching PNG previews."""

from pathlib import Path
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / "models"
TMP = Path("/tmp")

BPMN = "http://www.omg.org/spec/BPMN/20100524/MODEL"
BPMNDI = "http://www.omg.org/spec/BPMN/20100524/DI"
DC = "http://www.omg.org/spec/DD/20100524/DC"
DI = "http://www.omg.org/spec/DD/20100524/DI"
BIOC = "http://bpmn.io/schema/bpmn/biocolor/1.0"
for prefix, uri in [("bpmn", BPMN), ("bpmndi", BPMNDI), ("dc", DC), ("di", DI), ("bioc", BIOC)]:
    ET.register_namespace(prefix, uri)


def q(ns, tag):
    return f"{{{ns}}}{tag}"


def build_bpmn(filename, title, lanes, nodes, flows, data_objects, associations, automated):
    defs = ET.Element(q(BPMN, "definitions"), {"id": f"Defs_{filename}", "targetNamespace": "https://mirea.example/potato-control"})
    collaboration = ET.SubElement(defs, q(BPMN, "collaboration"), {"id": f"Collab_{filename}"})
    ET.SubElement(collaboration, q(BPMN, "participant"), {"id": "Participant_Plant", "name": title, "processRef": "Process_Main"})
    process = ET.SubElement(defs, q(BPMN, "process"), {"id": "Process_Main", "name": title, "isExecutable": "false"})
    lane_set = ET.SubElement(process, q(BPMN, "laneSet"), {"id": "LaneSet_Main"})
    for lane_id, lane_name, _, _ in lanes:
        lane = ET.SubElement(lane_set, q(BPMN, "lane"), {"id": lane_id, "name": lane_name})
        for node in nodes:
            if node[3] == lane_id:
                ET.SubElement(lane, q(BPMN, "flowNodeRef")).text = node[0]

    for node_id, name, tag, _, _, _, _, _ in nodes:
        element = ET.SubElement(process, q(BPMN, tag), {"id": node_id, "name": name})
        if node_id in automated:
            doc = ET.SubElement(element, q(BPMN, "documentation"))
            doc.text = "Будущая автоматизируемая операция; в модели AS-IS выполняется сотрудником вручную."
    for flow_id, source, target, name in flows:
        attrs = {"id": flow_id, "sourceRef": source, "targetRef": target}
        if name:
            attrs["name"] = name
        ET.SubElement(process, q(BPMN, "sequenceFlow"), attrs)
    for obj_id, name, x, y in data_objects:
        base = ET.SubElement(process, q(BPMN, "dataObject"), {"id": f"Object_{obj_id}", "name": name})
        ET.SubElement(process, q(BPMN, "dataObjectReference"), {"id": obj_id, "name": name, "dataObjectRef": base.get("id")})
    for assoc_id, source, target in associations:
        ET.SubElement(process, q(BPMN, "association"), {"id": assoc_id, "sourceRef": source, "targetRef": target})

    diagram = ET.SubElement(defs, q(BPMNDI, "BPMNDiagram"), {"id": f"BPMNDiagram_{filename}"})
    plane = ET.SubElement(diagram, q(BPMNDI, "BPMNPlane"), {"id": f"BPMNPlane_{filename}", "bpmnElement": f"Collab_{filename}"})
    max_x = max(n[4] + n[6] for n in nodes) + 180
    min_y = min(y for _, _, y, _ in lanes)
    max_y = max(y + h for _, _, y, h in lanes)
    shape = ET.SubElement(plane, q(BPMNDI, "BPMNShape"), {"id": "Participant_Plant_di", "bpmnElement": "Participant_Plant", "isHorizontal": "true"})
    ET.SubElement(shape, q(DC, "Bounds"), {"x": "20", "y": str(min_y), "width": str(max_x), "height": str(max_y-min_y)})
    for lane_id, _, y, height in lanes:
        shape = ET.SubElement(plane, q(BPMNDI, "BPMNShape"), {"id": f"{lane_id}_di", "bpmnElement": lane_id, "isHorizontal": "true"})
        ET.SubElement(shape, q(DC, "Bounds"), {"x": "80", "y": str(y), "width": str(max_x-60), "height": str(height)})
    positions = {}
    for node_id, _, _, _, x, y, width, height in nodes:
        attrs = {"id": f"{node_id}_di", "bpmnElement": node_id}
        if node_id in automated:
            attrs[q(BIOC, "fill")] = "#FFF2A8"
            attrs[q(BIOC, "stroke")] = "#A56A00"
        shape = ET.SubElement(plane, q(BPMNDI, "BPMNShape"), attrs)
        ET.SubElement(shape, q(DC, "Bounds"), {"x": str(x), "y": str(y), "width": str(width), "height": str(height)})
        positions[node_id] = (x, y, width, height)
    for obj_id, _, x, y in data_objects:
        shape = ET.SubElement(plane, q(BPMNDI, "BPMNShape"), {"id": f"{obj_id}_di", "bpmnElement": obj_id})
        ET.SubElement(shape, q(DC, "Bounds"), {"x": str(x), "y": str(y), "width": "70", "height": "90"})
        positions[obj_id] = (x, y, 70, 90)
    for flow_id, source, target, _ in flows:
        sx, sy, sw, sh = positions[source]; tx, ty, tw, th = positions[target]
        edge = ET.SubElement(plane, q(BPMNDI, "BPMNEdge"), {"id": f"{flow_id}_di", "bpmnElement": flow_id})
        ET.SubElement(edge, q(DI, "waypoint"), {"x": str(sx+sw), "y": str(sy+sh/2)})
        ET.SubElement(edge, q(DI, "waypoint"), {"x": str(tx), "y": str(ty+th/2)})
    for assoc_id, source, target in associations:
        sx, sy, sw, sh = positions[source]; tx, ty, tw, th = positions[target]
        edge = ET.SubElement(plane, q(BPMNDI, "BPMNEdge"), {"id": f"{assoc_id}_di", "bpmnElement": assoc_id})
        ET.SubElement(edge, q(DI, "waypoint"), {"x": str(sx+sw/2), "y": str(sy+sh/2)})
        ET.SubElement(edge, q(DI, "waypoint"), {"x": str(tx+tw/2), "y": str(ty+th/2)})

    ET.indent(defs, space="  ")
    ET.ElementTree(defs).write(MODELS / filename, encoding="utf-8", xml_declaration=True)


def render_dot(filename, title, lanes, nodes, flows, data_objects, associations, automated):
    ids = {n[0] for n in nodes}
    node_by_lane = {lane[0]: [] for lane in lanes}
    for node in nodes:
        node_by_lane[node[3]].append(node)
    lines = ["digraph G {", "graph [rankdir=LR,bgcolor=white,splines=ortho,nodesep=0.35,ranksep=0.55,labelloc=t,fontsize=22,fontname=\"DejaVu Sans\",label=\""+title+"\"];", "node [shape=box,style=\"rounded,filled\",fillcolor=white,fontname=\"DejaVu Sans\",fontsize=10,width=1.65,height=0.72];", "edge [fontname=\"DejaVu Sans\",fontsize=9];"]
    for lane_id, lane_name, _, _ in lanes:
        lines.append(f"subgraph cluster_{lane_id} {{ label=\"{lane_name}\"; color=\"#8A98A6\"; penwidth=1.2;")
        for node_id, name, tag, _, *_ in node_by_lane[lane_id]:
            label = name.replace('"', '\\"')
            attrs = []
            if tag in ("startEvent", "endEvent"):
                attrs += ["shape=doublecircle" if tag == "endEvent" else "shape=circle", "width=0.42", "height=0.42", "label=\"\""]
            elif "Gateway" in tag:
                attrs += ["shape=diamond", f"label=\"{label}\"", "width=1.1", "height=0.9"]
            else:
                attrs.append(f"label=\"{label}\"")
            if node_id in automated:
                attrs += ["fillcolor=\"#FFF2A8\"", "color=\"#A56A00\"", "penwidth=2"]
            lines.append(f'"{node_id}" [{",".join(attrs)}];')
        lines.append("}")
    for obj_id, name, _, _ in data_objects:
        lines.append(f'"{obj_id}" [shape=note,style=filled,fillcolor="#EAF2F8",label="{name}",fontsize=9];')
    for _, source, target, name in flows:
        if source in ids and target in ids:
            lbl = f',label="{name}"' if name else ""
            lines.append(f'"{source}" -> "{target}" [{lbl.lstrip(",")}];')
    for _, source, target in associations:
        lines.append(f'"{source}" -> "{target}" [style=dashed,arrowhead=none,color="#668099",constraint=false];')
    if automated:
        lines.append('legend [shape=note,style=filled,fillcolor="#FFF2A8",label="Жёлтым: будущая автоматизация"];')
    lines.append("}")
    dot_path = TMP / f"{filename}.dot"
    dot_path.write_text("\n".join(lines), encoding="utf-8")
    subprocess.run(["dot", "-Tpng", str(dot_path), "-o", str(MODELS / filename)], check=True)


lanes_as_is = [("Lane_Operator", "Оператор линии", 80, 240), ("Lane_Lab", "Лаборант", 320, 240), ("Lane_Tech", "Технолог", 560, 300)]
nodes_as_is = [
    ("Start", "Поступила партия сырья", "startEvent", "Lane_Operator", 130, 150, 48, 48),
    ("CreateBatch", "Создать карточку партии", "userTask", "Lane_Operator", 240, 135, 170, 80),
    ("InputControl", "Выполнить входной контроль сырья", "manualTask", "Lane_Lab", 480, 375, 190, 80),
    ("RawOK", "Сырьё соответствует?", "exclusiveGateway", "Lane_Lab", 730, 385, 70, 70),
    ("Reject", "Партия сырья отклонена", "endEvent", "Lane_Lab", 850, 475, 48, 48),
    ("SelectSpec", "Назначить технологическую спецификацию", "userTask", "Lane_Tech", 880, 625, 205, 80),
    ("Prep", "Зарегистрировать подготовительные операции", "manualTask", "Lane_Operator", 1140, 135, 210, 80),
    ("Heat", "Зарегистрировать тепловую обработку", "manualTask", "Lane_Operator", 1410, 135, 205, 80),
    ("Dry", "Зарегистрировать параметры сушки", "manualTask", "Lane_Operator", 1680, 135, 200, 80),
    ("OutputControl", "Выполнить выходной контроль", "manualTask", "Lane_Lab", 1940, 375, 190, 80),
    ("Validate", "Проверить измерения по нормативам", "userTask", "Lane_Tech", 2190, 625, 205, 80),
    ("HasDeviation", "Есть отклонения?", "exclusiveGateway", "Lane_Tech", 2450, 635, 70, 70),
    ("Correct", "Зарегистрировать корректирующее действие", "userTask", "Lane_Tech", 2580, 745, 215, 80),
    ("RepeatControl", "Выполнить повторное измерение", "manualTask", "Lane_Lab", 2600, 375, 190, 80),
    ("Assess", "Сопоставить параметры со спецификациями", "userTask", "Lane_Tech", 2600, 595, 220, 80),
    ("Form", "Сформировать электронный протокол партии", "userTask", "Lane_Tech", 2880, 595, 220, 80),
    ("Approve", "Утвердить результат контроля", "userTask", "Lane_Tech", 3160, 595, 190, 80),
    ("End", "Протокол утверждён", "endEvent", "Lane_Tech", 3420, 610, 48, 48),
]
flows_as_is = [
    ("F1","Start","CreateBatch",""),("F2","CreateBatch","InputControl",""),("F3","InputControl","RawOK",""),
    ("F4","RawOK","Reject","Нет"),("F5","RawOK","SelectSpec","Да"),("F6","SelectSpec","Prep",""),
    ("F7","Prep","Heat",""),("F8","Heat","Dry",""),("F9","Dry","OutputControl",""),
    ("F10","OutputControl","Validate",""),("F11","Validate","HasDeviation",""),("F12","HasDeviation","Correct","Да"),
    ("F13","Correct","RepeatControl",""),("F14","RepeatControl","Validate",""),("F15","HasDeviation","Assess","Нет"),
    ("F16","Assess","Form",""),("F17","Form","Approve",""),("F18","Approve","End","")]
data_as_is = [("DataRaw","Данные партии сырья",520,265),("DataSpec","Спецификация",940,760),("DataMeasurements","Журнал измерений",1970,265),("DataDeviation","Журнал отклонений",2600,850),("DataProtocol","Протокол партии",2960,760)]
assoc_as_is = [("A1","DataRaw","InputControl"),("A2","DataSpec","SelectSpec"),("A3","DataMeasurements","OutputControl"),("A4","DataDeviation","Correct"),("A5","Form","DataProtocol")]
automated = {"Validate","Assess","Form"}
build_bpmn("bpmn.bpmn", "Контроль производственной партии сухого картофельного пюре — AS-IS", lanes_as_is, nodes_as_is, flows_as_is, data_as_is, assoc_as_is, automated)
render_dot("bpmn.png", "Контроль производственной партии — AS-IS", lanes_as_is, nodes_as_is, flows_as_is, data_as_is, assoc_as_is, automated)

lanes_to_be = lanes_as_is + [("Lane_System", "Информационная система", 860, 240)]
nodes_to_be = [n for n in nodes_as_is if n[0] not in automated]
nodes_to_be += [
    ("Validate","Проверить измерения по нормативам","serviceTask","Lane_System",2190,915,205,80),
    ("Assess","Сопоставить параметры со спецификациями","serviceTask","Lane_System",2600,915,220,80),
    ("Form","Сформировать электронный протокол партии","serviceTask","Lane_System",2880,915,220,80),
]
build_bpmn("bpmn_to_be.bpmn", "Контроль производственной партии сухого картофельного пюре — TO-BE", lanes_to_be, nodes_to_be, flows_as_is, data_as_is, assoc_as_is, set())
render_dot("bpmn_to_be.png", "Контроль производственной партии — TO-BE", lanes_to_be, nodes_to_be, flows_as_is, data_as_is, assoc_as_is, set())

print("Generated BPMN AS-IS and TO-BE models")
