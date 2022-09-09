![example workflow](https://github.com/ameyaKetkar/InferRules/actions/workflows/gradle.yml/badge.svg)

Table of Contents
=================

   * [General info](#general-info)
   * [How to build PyEvolve](#how-to-build-pyevolve)
   * [Research](#research)
      * [How to cite PyEvolve](#research)
   * [API usage guidelines](#api-usage-guidelines)
   * [Running PyEvolve from the command line](#running-pyevolve-from-the-command-line)
   
# General info 
PyEvolve automates the frequently repeated code changes in Python systems. This tool presents a complete pipeline for mining and automating best code evolution practices, ensuring that the your project does not fall behind. The following is a high-level overview of the pipeline.

![h](https://github.com/maldil/PyEvolve/blob/cpatminer/workflow.jpg)


- **Phase 1:** We use [R-CPATMiner](https://github.com/maldil/R-CPATMiner) to mine best practices from version history of Python systems. You can find details on executing R-CPATMiner in its project description. 
- **Phase 2:**
- **Phase 3:**
- **Phase 4:**

# How to build PyEvolve
To have fully build PyEvolve, you have to build following component locally and install it to your local maven repository.
- We use [RulePharser](https://github.com/maldil/RulePharser) to generate an AST for Comby templates that includes both Python and Comby syntaxes. Follow the steps in [RulePharser](https://github.com/maldil/RulePharser) to build it locally and add it to your local maven repository.  

After completing the above steps, run `./gradlew` build from the root directory to build the project. This will build the project and execute the test cases. If you want to build the project without running the tests, use the command `./gradlew build -x test`.

# API usage guidelines
We will discuss the APIs that can be used for code automation, using the following code example.

The following is a best code evolution practice discovered by [R-CPATMiner](https://github.com/maldil/R-CPATMiner)
```
res = 0
for elem in elems:
  res = res + elem
``` 
==>
```
res = np.sum(elems)
```

Our goal is to transplant the above recommended practice to the target code listed below.

```
def getSum()
  n_diff = 0
  to_eval = getNumber()
  for dif in to_eval.getDiff():
    total = n_diff + dif
    n_diff = total
return n_diff    
```

We will now describe the APIs that can be used for above modification. 



# Running PyEvolve from the command line

# Research

# License
All software provided in this repository is subject to the [Apache License Version 2.0](LICENSE).
