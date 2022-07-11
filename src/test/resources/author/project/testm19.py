import tensorflow as tf
import numpy as np

def fit(self, hp, data):
       print(inputs)
       input_node = layer_utils.format_inputs(inputs, self.name, num=1)[0]
       inputs = nest.flatten(inputs)
       intArray = [1,2,3]
       xxx = sum(intArray)
       yyy = len(intArray)

       numbers = data.reduce(zz1, lambda y, _: y + 1)
       bbb= xxx/yyy
       utils.validate_num_inputs()
       total_sum = data.reduce(zz2, func) / numbers

       input_node = inputs[0]
       output_node = input_node
       output_node = hyper_block.Flatten().build(hp, output_node)
       _mean = tf.reduce_mean(total_sum, axis=axis)
       output_node = tf.keras.layers.Dense(self.output_shape[-1])(output_node)



       all_sum_square = data.reduce(zz3, sum_up_square) / numbers
       square_mean = tf.reduce_mean(all_sum_square, axis=zz3)
       stdiv = tf.sqrt(square_mean - tf.square(_mean))

       return stdiv*output_node