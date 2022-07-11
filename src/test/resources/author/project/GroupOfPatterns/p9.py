# type dataset : Any
# import tf : tensorflow
# type batch_size : Any
dataset.apply(tf.contrib.data.batch_and_drop_remainder(batch_size))