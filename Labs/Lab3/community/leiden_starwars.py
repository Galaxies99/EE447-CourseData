import pandas as pd
from graphx import WeightedUndirectedGraph
from community import Leiden
from utils import starwars_dataset, starwars_dataset_process_nodes

starwars_dataset_process_nodes('data/starwars-full-interactions-allCharacters-merged.json', 'data/nodes.csv')
edge_list = starwars_dataset('data/starwars-full-interactions-allCharacters-merged.json')
graph = WeightedUndirectedGraph()
graph.add_edges_from_list(edge_list)
out_df = pd.DataFrame(edge_list, columns = ['source', 'target', 'weight'])
out_df.to_csv('data/starwars_edge_list.csv', index = False)

leiden = Leiden()
partition, Q = leiden.fit(graph)

partition = partition.get_partition()
index, labels = [], []
for id, label in partition.items():
    index.append(id)
    labels.append(label)

out_df = pd.DataFrame(labels, columns = ['community'], index = index)
out_df.index.name = 'id'
out_df.to_csv('data/starwars_community.csv')
