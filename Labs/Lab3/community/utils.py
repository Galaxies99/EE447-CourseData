import json
import pandas as pd


def dblp_dataset(file_path):
    edge_list = []
    with open(file_path, 'r') as f:
        # Headers
        for line in f.readlines():
            line = line.lstrip(' \n\t').rstrip(' \n\t')
            if line == "" or line[0] == '#':
                continue
            edge = []
            cur = ""
            for ch in line:
                if ch == ' ' or ch == '\t':
                    if cur != "":
                        edge.append(int(cur))
                    cur = ""
                else:
                    cur = cur + ch
            if cur != "":
                edge.append(int(cur))
            if len(edge) != 2:
                raise ValueError('Invalid edge.')
            edge_list.append(edge)
    return edge_list


def starwars_dataset(file_path):
    with open(file_path, 'r') as f:
        dict = json.load(f)
    links = dict.get('links', [])
    edge_list = []
    for link in links:
        src = link['source']
        tgt = link['target']
        v = link['value']
        edge_list.append([src, tgt, v])
    return edge_list


def starwars_dataset_process_nodes(file_path, output_file_path):
    with open(file_path, 'r') as f:
        dict = json.load(f)
    nodes = dict.get('nodes', [])
    node_list = []
    index = []
    for i, node in enumerate(nodes):
        index.append(i)
        name = node['name']
        v = node['value']
        node_list.append([name, v])
    out_df = pd.DataFrame(node_list, columns = ['label', 'value'], index = index)
    out_df.index.name = 'id'
    out_df.to_csv(output_file_path)