# TU-Darmstadt FOP HA Tester (TDFT)
## General idea
The idea of this tool is to easily write simple tests to test your exercise.  
The main goal is to develop tests which could be easily applied using the CLI of the tool.  
An example output could be:  
```
INFO Loading project at /home/wolverindev/Downloads/H03_Hadenfeldt_Markus.zip
INFO Compiling project.
INFO Found 1 test units. Start testing....
INFO [PascalsTriangle] Executing 2 tests in this unit.
INFO [PascalsTriangle] > Executing test suite "class methods available"
INFO [PascalsTriangle] => Test passed
INFO [PascalsTriangle] > Executing test suite "faculty algo test"
INFO [PascalsTriangle] => Test passed
All tests have been passed. Great!
Do you have any more ideas to test? May consider to contribute :)
```
  
## Current supported exercises
| Exercise    | Sub category  | Supported | Notes |
| ------------| ------------- | --------- | ----- |
| H00 | | :heavy_multiplication_x: | |
| H01 | | :heavy_multiplication_x: | |
| H02 | | :heavy_multiplication_x: | |
| H03 | Pascals Triangle | :heavy_plus_sign: | |
|  | Heritage Tests | :heavy_multiplication_x: | |
| H04 | | :heavy_multiplication_x: | |
  
:heavy_check_mark: := Fully supported  
:heavy_plus_sign: := Work in progress  
:heavy_multiplication_x: := Currently not supported  

## Features to come
[ ] GUI
[ ] MacOS and Windows support (Currently not supported due to the `cd` command)
[ ] Fully implement H03
[ ] Releases