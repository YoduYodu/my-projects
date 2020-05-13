Here, I'm providing 4 input files (.gz files) that can be used to run your pipeline.

## data-err-small.gz
This file contains some corrupted data, so you should expect some output in the debug-corrupted file.
Here's the command I used (note that the threshold is set at 5 users):
```
gradle -p dataflow run -Pargs="--pathToInputFile=/Users/haden/Downloads/proj1/data-err-small.gz --pathToOutputDir=/Users/haden/Downloads/proj1/output-err-small --threshold=5"
```

All other files have no corrupted data (for ease of testing).

## data-small.gz
Sample command:
```
gradle -p dataflow run -Pargs="--pathToInputFile=/Users/haden/Downloads/proj1/data-small.gz --pathToOutputDir=/Users/haden/Downloads/proj1/output-small --threshold=5"
```

## data-medium.gz
Sample command:
```
gradle -p dataflow run -Pargs="--pathToInputFile=/Users/haden/Downloads/proj1/data-medium.gz --pathToOutputDir=/Users/haden/Downloads/proj1/output-medium --threshold=10"
```

## data-large.gz
Sample command:
```
gradle -p dataflow run -Pargs="--pathToInputFile=/Users/haden/Downloads/proj1/data-large.gz --pathToOutputDir=/Users/haden/Downloads/proj1/output-large --threshold=50"
```

Feel free to share your outputs on Piazza (that's NOT code-sharing, so that's OK), and discuss.

