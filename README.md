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
PyEvolve automates the frequently repeated code changes in Python systems. This tool presents a complete pipeline for mining and automating best code evolution practices, ensuring that the your project does not fall behind.
We use [R-CPATMiner](https://github.com/maldil/R-CPATMiner) to mine frequently repeated code evolution practices in Python systems and then automate them.





# How to build PyEvolve
To have fully build PyEvolve, you have to build following component locally and install it to your local maven repository.
- We use [RulePharser](https://github.com/maldil/RulePharser) to generate an AST for Comby templates that includes both Python and Comby syntaxes. Follow the steps in [RulePharser](https://github.com/maldil/RulePharser) to build it locally and add it to your local maven repository.  

After completing the above steps, `run./gradlew` build from the root directory to build the project. This will build the project and execute the test cases. If you want to build the project without running the tests, use the command `./gradlew build -x test`.

# API usage guidelines

# Running PyEvolve from the command line

# Research
