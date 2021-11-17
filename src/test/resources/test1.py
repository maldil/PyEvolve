import numpy as np
def function1(sentence):
    cc=0
    for count in sentence:
        cc = np.sum(count)
    return cc