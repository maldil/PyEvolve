# type :[l3] : Any
# type :[[l1]] : int
# type :[[l2]] : int
:[[l2]] = 0
for :[[l1]] in :[l3].values():
    :[[l2]] = :[[l2]] + :[[l1]]
