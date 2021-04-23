import numpy as np
from scipy.special import comb
from math import pow
from sklearn.linear_model import LinearRegression
import matplotlib.pyplot as plt
from tqdm import tqdm

p = 0.51
c = 1
w = 100
LIMIT = 20

def calc_expected_cost(k, p, c, epsilon = 0.1):
    '''
    Calculate the numerical value of expected cost when given k, p and c.

    Parameters
    ----------
    k, p, c: the given parameters, explained previously in the solution.
    
    Returns
    -------
    The numerical value of expected cost.
    '''
    E = 0
    P = []
    D = []
    x = 0
    while True:
        Px = comb(k + x + x + 2, x) * pow(p, k + x + 2) * pow(1 - p, x)
        if x > 0:
            for y in range(x - 1):
                Px = Px - P[y] * comb(2 * (x - y), (x - y)) * pow(p, x - y) * pow(1 - p, x - y)
        if Px == np.inf:
            break
        P.append(Px)
        delta = Px * (k + x + x + 2) * c
        D.append(delta)
        E += delta
        x += 1
    
    reg = LinearRegression()
    reg.fit(np.array(range(x - 100, x)).reshape(-1, 1), D[len(D) - 100:])

    while True:
        delta = reg.predict(np.array([x]).reshape(1, -1))[0]
        if delta < 0:
            break
        E += delta
        x += 1
    return E 

E_value = []
for k in tqdm(range(LIMIT)):
    E_value.append(calc_expected_cost(k, p, c))

plt.scatter(range(LIMIT), E_value)
plt.plot(range(LIMIT), [100] * LIMIT, color='r', linestyle='--')
plt.xlabel('k')
plt.xlim(0, 19)
plt.xticks(range(0, 20, 5))
plt.ylabel('expected cost (unit: $10,000)')
plt.savefig('2.png')