![example workflow](https://github.com/ameyaKetkar/InferRules/actions/workflows/gradle.yml/badge.svg)

Table of Contents
=================

   * [General info](#general-info)
   * [How to build PyEvolve](#how-to-build-pyevolve)
   * [Research](#research)
      * [How to cite PyEvolve](#research)
   * [API usage guidelines](#api-usage-guidelines)
   * [Running PyEvolve in VirtualBox Image](#running-pyevolve-in-virtualbox-image)
   
# General info 
PyEvolve automates the frequently repeated code changes in Python systems. We build a pipeline for mining and automating best code evolution practices, ensuring that the your project does not fall behind. The following is a high-level overview of the pipeline.

![h](https://github.com/maldil/PyEvolve/blob/cpatminer/workflow.jpg)


- **Phase 1:** We use [R-CPATMiner](https://github.com/maldil/R-CPATMiner) to mine best practices from version history of Python systems. You can find details on executing R-CPATMiner in its project description. 
- **Phase 2:** We use [InferRules](https://github.com/ameyaKetkar/InferRules) to infer the initial transformation rules for the patterns detected by R-CPATMiner.
- **Phase 3:** PyEvolve use rule as input to identify the potential sites to apply the patterns in the target codes. 
- **Phase 4:** PyEvolve infer final adapted rules to tranplant the identified pattern.


# How to build PyEvolve
To have a fully built PyEvolve, you have to install the following components.
- We use [RulePharser](https://github.com/maldil/RulePharser) to generate an AST for Comby templates that includes both Python and Comby syntaxes. Follow the steps in [RulePharser](https://github.com/maldil/RulePharser) to build it locally and add it to your local maven repository.  
- We use [ComBy](https://comby.dev/docs/get-started#install) as a backend tool to rewrite code. Please follow the steps in their [documentation](https://comby.dev/docs/get-started#install) to install it on your PC.
- We use [wala.ml](https://github.com/wala/ML). To install it, follow the instructions on [wala.ml](https://github.com/wala/ML). 

After completing the above steps, run `./gradlew` build from the root directory to build the project. This will build the project and execute the test cases. If you want to build the project without running the tests, use the command `./gradlew build -x test`.

# API usage guidelines
## Using PyEvolve to identify code usages
In this example, we identify two usages of the following pattern in the project [PatternTest](https://github.com/pythonInfer/PatternTest)

Pattern : 
```python
# type :[l3] : List[int]" 
# type :[[l1]] : int" 
# type :[[l2]] : int" 
:[[l1]] = 0" 
for :[[l2]] in :[l3]: 
   :[[l1]]=:[[l1]]+:[[l2]]
```  
1. Clone the project [PyEvolve](https://github.com/pythonInfer/PyEvolve.git)
1. Update `Configurations.PROJECT_REPOSITORY` and `Configurations.TYPE_REPOSITORY` configurations with valid paths. 
2. Clone the project [PatternTest](https://github.com/pythonInfer/PatternTest) to the directory `Configurations.PROJECT_REPOSITORY + "/pythonInfer/"`
3. Execute the script [type_infer.py](https://github.com/pythonInfer/PyEvolve/blob/master/type_infer.py) to infer type information of the project [PatternTest](https://github.com/pythonInfer/PatternTest)
4. Copy the generated `json` files into the folder `Configurations.TYPE_REPOSITORY + "/pythonInfer/PatternTest/"`
5. Execute the following code to get all the usages of the Pattern

```java
import com.utils.Utils;
import org.junit.jupiter.api.Test;

import static com.matching.fgpdg.Configurations.PROJECT_REPOSITORY;  // This must be set to root directory of the folder "pythonInfer"


public class DetectPattern {

    @Test
    void testPattern() throws Exception {
        String pattern = "# type number : Any\n" +
                "# type :[l3] : List[int]\n" +
                "# type :[[l1]] : int\n" +
                "# type :[[l2]] : int\n" +
                ":[[l1]] = 0\n" +
                "for :[[l2]] in :[l3]:\n" +
                "   :[[l1]]=:[[l1]]+:[[l2]]";
        String outPath = "./OUTPUT/"; 
        String projectPath =  PROJECT_REPOSITORY +"pythonInfer/PatternTest";
        System.out.println(pattern);
        Utils.processProjectForPattern(projectPath,pattern,outPath);
    }
}
```

## Using PyEvolve for transplanting a rule
### To a file
PyEvolve can assist you if you have a rule encoded in ComBy syntax and want to  transplant it to a Python file.

```java
    void transplantPatternToFile() {
         Configurations.TYPE_REPOSITORY = ""; // this should the path to your type repository 
         Configurations.PROJECT_REPOSITORY = "" // this should be the path to your projects repository 
         String projectFile = ""; // this should be the path to your file, this path should be the relative path to Configurations.PROJECT_REPOSITORY
         String LHS = ""; // path to the LHS of the rule file
         String RHS = "";  // path to the RHS of the rule file
         String adaptedFile = MainAdaptor.transplantPatternToFile(projectFile, LHS, RHS);   // adaptedFile is the adapted file
    }
```
### To a function
PyEvolve can assist you if you have a rule encoded in ComBy syntax and want to  transplant it to a perticular Python function.
```java
    void transplantPatternToFunction() {
        Configurations.TYPE_REPOSITORY = ""; // this should the path to your type repository 
        Configurations.PROJECT_REPOSITORY = "" // this should be the path to your projects repository 
        String projectFile = " "; // this should be the path to your file, this path should be the relative path to Configurations.PROJECT_REPOSITOR
        String LHS = ""; // path to the LHS of the rule file
        String RHS = "";  // path to the RHS of the rule file
        Module codeModule = com.utils.Utils.getPythonModule(projectFile);
        stmt stmt = codeModule.getInternalBody().get(i); // i must be the statment number of the function.   
        List<org.python.antlr.base.stmt> imports = codeModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList());
        String adaptedFunction = MainAdaptor.transplantPatternToFunction(projectFile, (FunctionDef) stmt,imports,LHS, RHS);
    }
```



## Using PyEvolve for automation
We will discuss the APIs that can be used for code automation, using the following code example.

The following is a best code evolution practice discovered by [R-CPATMiner](https://github.com/maldil/R-CPATMiner).
```python
res = 0
for elem in elems:
  res = res + elem
``` 
==>
```python
res = np.sum(elems)
```

Our goal is to transplant the above recommended practice to the target code listed below.

```python
def getSum()
  n_diff = 0
  to_eval = getNumber()
  for dif in to_eval.getDiff():
    total = n_diff + dif
    n_diff = total
return n_diff    
```

We will now describe the APIs that can be used for above modification. 

### 1. With pattern and target function as input
```java
MainAdaptor adaptor = new MainAdaptor();
Module codeModule = getPythonModule("/pathToFunction.py"); // pathToFunction is the String value of the file path which contains the above target code.
Module lpatternModule = getPythonModuleForTemplate("/pathToLPattern.py");// pathToLPattern is the string value of the file path which has the LHS of the pattern.
Module rpatternModule = getPythonModuleForTemplate("/pathToRPattern.py"); // pathToRFunction is the string value of the file path which has the RHS of the pattern.
List<stmt> imports = lpatternModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList());
Guards guards = new Guards("/pathToLPattern.py",lpatternModule);
String adaptedCode = adaptor.adaptFunction(imports, guards, lpatternModule, rpatternModule, codeModule); // adaptedCode is the final adapted code
```

# Running PyEvolve in VirtualBox Image

# Research
We will add citation information as soon as possible.

If you are using R-CPATMiner in your research, please cite the following papers:

Malinda Dilhara, Danny Dig, and Ameya Ketkar, PyEvolve: Automating Frequent Code Changes in Python ML Systems," 45th International Conference on Software Engineering (ICSE 2022), Melbourne, Australia.

@inproceedings{Dilhara:ICSE:2023:PyEvolve,
author = {Dilhara, Malinda and Dig, Danny and Ketkar, Ameya},
title = {PyEvolve: Automating Frequent Code Changes in Python ML Systems},
booktitle = {Proceedings of the 45th International Conference on Software Engineering},
series = {ICSE '23},
year = {2023},
location = {Melbourne, Australia},
publisher = {ACM},
address = {New York, NY, USA},
}


# License
All software provided in this repository is subject to the [Apache License Version 2.0](LICENSE).

