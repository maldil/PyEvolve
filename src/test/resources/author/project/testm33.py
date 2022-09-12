def getSum()
n_diff = 0
to_eval = getNumber()
for dif in to_eval.getDiff():
        total = n_diff + dif
        n_diff = total
return n_diff