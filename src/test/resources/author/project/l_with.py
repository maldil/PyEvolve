# type :[[l1]] : Any
# type :[[l2]] : Any
# type :[[l3]] : Any
:[[l1]] = open(:[[l2]], "r")
:[l4] = :[[l1]].readlines()
:[[l1]].close()