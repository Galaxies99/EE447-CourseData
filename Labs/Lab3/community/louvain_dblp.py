import pandas as pd
from graphx import WeightedUndirectedGraph
from community import Louvain
from utils import dblp_dataset

edge_list = dblp_dataset('data/dblp.txt')
graph = WeightedUndirectedGraph()
graph.add_edges_from_list(edge_list)
out_df = pd.DataFrame(edge_list, columns = ['source', 'target'])
out_df.to_csv('data/dblp_edge_list.csv', index = False)

louvain = Louvain()
partition, Q = louvain.fit(graph)

partition = partition.get_partition()
index, labels = [], []
for id, label in partition.items():
    index.append(id)
    labels.append(label)

out_df = pd.DataFrame(labels, columns = ['community'], index = index)
out_df.index.name = 'id'
out_df.to_csv('data/dblp_community.csv')
