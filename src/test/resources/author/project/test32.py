import numpy as np
def test_compute_multithreading():
    """Task.compute should be executed on multiple threads."""
    thread_ids = set()

    def log_thread_id():
        thread_id = threading.current_thread().ident
        thread_ids.add(thread_id)
        return Batch(())

    with spawn_workers([fake_device() for _ in range(2)]) as (in_queues, out_queues):
        for i in range(2):
            t = Task(CPUStream, compute=log_thread_id, finalize=None)
            in_queues[i].put(t)
        for i in range(2):
            out_queues[i].get()
    print (a if b else 0)
    assert len(thread_ids) == 2