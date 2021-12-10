package com.prune;

import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.cast.python.loader.PythonLoaderFactory;
import com.ibm.wala.cast.python.ml.analysis.TensorTypeAnalysis;
import com.ibm.wala.cast.python.ml.client.PythonTensorAnalysisEngine;
import com.ibm.wala.cast.python.util.PythonInterpreter;
import com.ibm.wala.classLoader.SourceURLModule;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.HashSetFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public class WALAEngine {
    static {
        try {
            Class<?> j3 = Class.forName("com.ibm.wala.cast.python.loader.Python3LoaderFactory");
            PythonAnalysisEngine.setLoaderFactory((Class<? extends PythonLoaderFactory>) j3);
            Class<?> i3 = Class.forName("com.ibm.wala.cast.python.util.Python3Interpreter");
            PythonInterpreter.setInterpreter((PythonInterpreter)i3.newInstance());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            try {
                Class<?> j2 = Class.forName("com.ibm.wala.cast.python.loader.Python2LoaderFactory");
                PythonAnalysisEngine.setLoaderFactory((Class<? extends PythonLoaderFactory>) j2);
                Class<?> i2 = Class.forName("com.ibm.wala.cast.python.util.Python2Interpreter");
                PythonInterpreter.setInterpreter((PythonInterpreter)i2.newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e1) {
                assert false : e.getMessage() + ", then " + e1.getMessage();
            }
        }
    }

    protected PythonAnalysisEngine<TensorTypeAnalysis> makeEngine(String... name) throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<TensorTypeAnalysis> engine = new PythonTensorAnalysisEngine();
        Set<Module> modules = HashSetFactory.make();
        for(String n : name) {
            modules.add(getScript(n));
        }
        engine.setModuleFiles(modules);
        return engine;
    }

    protected SourceURLModule getScript(String name) throws IOException {
        try {
            File f = new File(name);
            if (f.exists()) {
                return new SourceURLModule(f.toURI().toURL());
            } else {
                URL url = new URL(name);
                return new SourceURLModule(url);
            }
        } catch (MalformedURLException e) {
            return new SourceURLModule(getClass().getClassLoader().getResource(name));
        }
    }
}
