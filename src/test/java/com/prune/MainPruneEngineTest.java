package com.prune;

import org.junit.jupiter.api.Test;
import org.python.core.PyObject;


class MainPruneEngineTest {

    @Test
    void getAllDataDependentNodes() {
        MainPruneEngine engine = new MainPruneEngine();
        engine.getAllDataDependentNodes(new PyObject(),"src/test/resources/test1.py");

    }
}