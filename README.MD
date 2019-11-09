# TU-Darmstadt FOP HA Tester (TDFT)
## General idea
The idea of this tool is to easily write simple tests to test your exercise.  
The main goal is to develop tests which could be easily applied using the CLI or the GUI version of the tool.  
An example output from the CLI could be:  
```

INFO Adding plugin/plugin directory tests/h3/target/
INFO Adding plugin/plugin directory tests/h4/target/
INFO Loading project at /home/wolverindev/Downloads/H04_Hadenfeldt_Markus.zip
INFO Compiling project.
INFO  Loading tests...
INFO  Plugin Test H4 scheduled 1 test units with 8 tests
INFO  Plugin Test H3 scheduled 0 test units with 0 tests
INFO  Executing 8 tests.
INFO Found 1 test units with 8 tests. Start testing....
INFO [H1 MatrixOperation] Executing 8 tests in unit H1 MatrixOperation.
INFO [H1 MatrixOperation] > Executing test suite "test for function matrixCrossMultiplication"
...
INFO [H1 MatrixOperation] > Executing test suite "test 07"
INFO [H1 MatrixOperation] => Test passed
INFO  Executed 8 tests in 116ms.
Test summery:
  Test units executed:   1 of   3  (33.3%)
  Tests executed     :   8 of   8 (100.0%)
    Succeeded        :   8        (100.0%)
    Failed           :   0          (0.0%)
    Skipped          :   0          (0.0%)

All tests have been passed. Great!
Do you have any more ideas to test? May consider to contribute :)
```
  
The tool might look like this:  
![https://img.did.science/Screenshot_00-07-42.png](https://img.did.science/Screenshot_00-07-42.png)

## Current supported exercises
| Exercise    | Sub category  | Supported | Notes |
| ------------| ------------- | --------- | ----- |
| H00 | | :heavy_multiplication_x: | |
| H01 | | :heavy_multiplication_x: | |
| H02 | | :heavy_multiplication_x: | |
| H03 | Pascals Triangle | :heavy_check_mark: | |
|  | Dog heritage | :heavy_check_mark: | |
|  | Dog functionality | :heavy_check_mark: | |
| H04 | Matrix Operations | :heavy_check_mark: | |
|  | Pizza factory structure | :heavy_check_mark: | |
|  | Pizza factory functionality | :heavy_plus_sign: | |
|  | Pizza factory (with cheese) | :heavy_multiplication_x: | |
  
:heavy_check_mark: := Fully supported  
:heavy_plus_sign: := Work in progress  
:heavy_multiplication_x: := Currently not supported  

## Planned features
- MacOS support (Currently partial supported)  
  Frameworks like the FUPBot will not work properly