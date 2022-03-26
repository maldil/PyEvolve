import tensorflow as tf
import numpy as np
num_instance = data.reduce(zz1, lambda x, _: x + 1)
total_sum = data.reduce(zz2, func) / num_instance
mean = tf.reduce_mean(total_sum, axis=axis)
total_sum_square = data.reduce(zz3, sum_up_square) / num_instance
square_mean = tf.reduce_mean(total_sum_square, axis=zz3)
std = tf.sqrt(square_mean - tf.square(mean))