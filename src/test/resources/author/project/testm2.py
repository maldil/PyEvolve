import numpy as np

def function1(sentence,intArray):
    hhh=0

    for xx in range(10):
        for yy in range(100):
            hhh+=sentence[xx,yy]
            print("some message")
    add = 0
    print("ff")
    for z in intArray:
       print("ff")
       add=add+z
    return add

if __name__ == "__main__":
    function1([[1,2][3,4]],[1,2,3])