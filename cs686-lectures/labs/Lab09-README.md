
### Errata / Corrections
 - (v0) Released on Apr 1 around 8:15pm.
 - (v1) Updated on Apr 10 aroudn 10:17am:
 In the main method, you should change the GCS destination to point to your own bucket, not mine, as you do not have access to my bucket...
```
 p.apply(TextIO.read().from(GCS_BUCKET + "/project4-perf/data00.txt")).apply(ParDo.of(new GsonParser6()))
  .apply(TextIO.write().to("gs://usf-cs686-sp20/lab09-output/test")); // <- change this to 
                                                                      // GCS_BUCKET + "/lab09-output/test".
```


### NOTICE

- This lab only has short-answer questions. 
- You'll need to download some large files, run the code locally and also on GCP, and then answer the short-answer questions.
- Read the detailed comments in each Java file to make sure you understand what's to be done.

### Short Answer Questions 

 - See the `getShortAnswer1` method in `Main` class. 
   - Follow the instructions provided in the comments.
   - This is relevant to Lecture 27.
 - See the `getShortAnswer2` method in `Main` class.
   - Follow the instructions provided in the comments.
   - This is relevant to Lecture 28.
 - See `JsonParsers.java` and examine the six `GsonParser` DoFns. 
   - Notice the differences in terms of where `GsonParser` is instantiated.
   - This is relevant to Lecture 10 and Lecture 30.
   - Take a look at `TestJsonParsers` class (download the sample data files from Canvas by following the instructions in this file).
   - Run unit tests, and answer both `getShortAnswer3` and `getShortAnswer4` in `Main`.
   - Discuss on Piazza (see my inlined questions in those classes).
   - Execute your java program by executing `Main.main()` in `java/dataflow`. This will make your pipeline run on GCP, using `GsonParser6`; examine the job through the web console page, and answer `getShortAnswer6` (this is relevant to Lecture 26). 
 - See `TestMnistModel.java` and read the instructions. This is somewhat relevant to Lecture 35, but for now don't worry about the exact details. We'll come back to that later.
   - Answer `getShortAnswer5` in `Main`.
   - Just to give you some context:
     - Perry (one of your TAs) built this TensorFlow model, and its source code is available [here](https://github.com/PerrySong/mnist-tensorflow-java/).
     - The trained model was saved as `SavedModelBundle` (which is a proto message defined in TF library), which you can find in the zip file on Canvas (`.pbtxt` file where `pb` stands for protocol buffers).
     - In this lab, the starter code simply "loads" the model into memory. It then uses 15 sample input data (defined in `TestMnistModelHelper` class -- this class give you further context about the data & why you'd be doing this) to make predictions (which digit each image contains). You can actually check the images yourself in `dataflow/resources/mnist-data` directory (this is not used in the project, but provided for your convenience).

### Ungraded Extra Homework
 - Visit Perry's repository [here](https://github.com/PerrySong/mnist-tensorflow-java/).
 - Understand the input/output specifications of the model being trained.
 - Make (small) changes to the `testPredictions` method in `TestMnistModel.java` so that you can actually make "batch predictions" (instead of feeding a single `28*28` floats into it, making it possible to feed `k*28*28` floats so that you can make predictions (classifications) for `k` images at a time).
 - Run "local performance tests" to compare the speed of making single predictions (one at a time) using `testPredictions()` and your new `testBatchPredictions()` method for 100,000 images (simply re-use the 15 images in the Helper class), and by using batch size from {10, 100, 1000}. What do you observe? 
 - For this part, feel free to share your code & observations on Piazza (as this is not graded, it's not considered cheating); I recommend creating your own public repository (by forking Perry's) if you intend to work on this.
 - This will require a bit of reading into TF APIs, but the amount of changes you need to make is relatively small (5-10 lines of edits. 
 - Here is a link to [Piazza post](https://piazza.com/class/k5ad4g8m2jf6t6?cid=247) where you can begin discussing this specific part.


## Commits & Testing
 - **Make incremental changes and commit your changes often**.
 - Grading system status: `https://www.cs.usfca.edu/~hlee84/cs686/lab09-status.html` (if the page is blank or not accessible, it means the grading system has not started grading yet; grading usually begins within a day after the assignment is released.)
 - URL for accessing your reports: You'll be able to download the zip files from the status dashboard above (it requires you login using Dons email account). Only you and the teaching staff can see your reports.
 - If your project does not build (due to compilation errors), then it cannot be graded; don't even push to remote repo. It is your responsibility to run unit tests locally to make sure that all of the provided tests (sample tests) pass.
 - If your commit is stuck at `pending` status for a while, then it probably means something went wrong and it crashed or never terminated. If such is the case, make another push to see if the issue persists. If so, email the instructor (include your repo and sha).
 - **IMPORTANT** Your score may be reduced later if your code is found to violate the requirements. This will be manually checked.

## Starvation of Grading System
 - This lab's unit tests will take longer than previous labs' unit tests.
 - As such, if many of you push commits around the same time, you may experience an extended delay before your commit gets graded (it could be up to several minutes).
 - Since the lab is available for a week, and we have covered all of the topics before the lab was released, it is your responsibility to get it **done and graded** before the deadline.

## Scoring (read carefully)
 - Number of sample/shareable/hidden tests: 3 / 4 / 4
 - Your overall score will be **4% shareable tests + 96% hidden tests** (this weight may vary from lab to lab), penalized by the number of failed sample tests.
 - Your score for this lab will be the maximum score you obtain before the deadline (This applies to all labs/projects).
 - There's no hard limit on how often / how many times you can submit as long as you do not harm anyone else's grading experiences. 
 - **With that said, try not to overload the grading system by pushing commits more often than one per 10 minutes (or so).** 
 - **Try not to push to remote repository if your project does not compile locally as it won't be graded anyway.**

## After securing 100% (which all of you will) for this lab
 - Once you secured 100% for this assignment, try to add some tricky unit tests on your own (and feel free to share such test cases on Piazza, even before the deadline).
 - Also, try to `simplify` your code if that's possible; it does not necessarily mean you should do everything in a single line or something like that. Rather, try to cut out unnecessary blocks, logic, etc. to make better code.

## Honor Code
 - Do not ever share/show/post your code. That's an automatic F.

## Questions?
 - Please ask on Piazza.
 - For technical issues, you are allowed to share any error messages or the like.
