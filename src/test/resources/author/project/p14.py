# type actual : Any
# type old : Any
# type a : Any
# type a : Any
# import np : numpy
old = np.seterr()
try:
    :[l1]
finally:
    np.seterr(old)