import numpy as np
import tensorflow as tf

def log_thread_id():
        thread_id = threading.current_thread().ident
        thread_ids.add(thread_id)
        for i in range(2):
             yy = tf.io.gfile.GFile("someFile.txt")
             t = Task(CPUStream, compute=log_thread_id, finalize=None)
             yy.out_queues[i].get()
             out_queues[i].get()
             yy.close()
             in_queues[i].put(t)
        return Batch(())

