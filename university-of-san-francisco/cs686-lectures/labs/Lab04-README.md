# CS686 Lab 04

### Errata / Corrections
 - refer to this file: https://github.com/cs-rocks/cs686-lectures/blob/master/labs/Lab04-README.md

## Do's and Don'ts

### Do's:
 - You can do whatever you want with the files under `java/dataflow` directory, including `build.gradle`.
 - For instance, if you find it useful to import some libraries, you can do that in `build.gradle`.
 - You can modify/add/remove any packages/files/classes/methods as you like.
 - You can (and should) add more unit tests (the more the better -- always consider 'corner cases') to `java/dataflow`.
 - Try to apply appropriate design patterns/principles (Effective Java by Joshua Bloch), if applicable (this is more applicable to projects than to labs).

### Don'ts:
 - Do not change anything outside `java/dataflow` directory (if you have to, ask first on Piazza; in most cases, it'd be either (a) you're doing something unexpected or (b) the starter code's lacking some files/etc. in which case I'll fix it so that you don't have to)
 - Specifically, `java/judge/*`,`java/settings.gradle`,`.github/workflows/*` should NOT be touched at all.
 - Do not add/remove/modify any other files than `*.java` or `*.proto` (except for `java/dataflow/build.gradle`, if you must import some libraries).


## Assignment Details

### Main Tasks
In this lab, you will work with `PurchaseEvent` proto message defined in `dummy.proto`.
Other proto files will not be used in this lab.

There are four main tasks: (A) Combine (B) SideInput (C) CoGbk and (D) Compare.
  - (A) You are asked to implement a pipeline that calculates min/max/average(=arithmetic mean), in three different ways.
  - (B) You are asked to implement a pipeline that filters a dataset, based on a side input, in three different ways.
  - (C) You are asked to implement the same pipeline as A, but using `CoGroupByKey` AND pre-defined CombineFunctions (in Beam).
  - (D) You are asked to compare efficiency of these pipelines (but you do not have to submit anything through the lab). We will revisit this later in class (see the details below).

**About Proto Files**
 1. Read `dummy.proto` carefully. It defines `PurchaseEvent` message, and that's all we need in this lab. Try to understand what each field semantically means.
 2. Make sure you build your gradle project so that the proto library generates java files (specifically, `Dummy.java` under `dataflow/src/generated-sources/main.java.edu.usfca.protobuf`).
 3. If you want to define your own proto messages, you are encouraged to do so -- in some labs/projects, doing so will make your code much cleaner. Likewise, you can choose not to do so. As long as you do not break the existing code base (especially for the unit tests in `java/judge`), you are not limited to the provided proto messages.
 4. `amount` represents an integer amount of each purchase (which could be negative/zero/positive, as explained in the proto file). Note that it defined as a 32-bit integer (`int32`) in `dummy.proto`.
  
  
### Notice
 - Check out `edu.usfca.dataflow.utils.Common` class. `StrPrinter` class extends `PTransform`, not `DoFn`.
 - You can read more about Composite PTransforms here: https://beam.apache.org/documentation/programming-guide/#composite-transforms
 - In this lab, you will be implementing Composite PTransforms (using the provided starter code), which are more flexible and useful in practice (as opposed to writing a series of custom `DoFn`s).

### Task A: CombineJob
 - In `CombineJob.run()` you will find an example and explanation of what should be done.
 - In `CombineVia...` classes under `edu.usfca.dataflow.transforms` package, you will find more details instructions for each class.
 - Specifically, you will implement 3 PTransforms to compute min/max/average(=arithmetic mean) of purchase amounts, and produce the results as formatted Strings. In particular, you will need to use `GroupByKey` / `Combine.perKey (SerializableFunction)` / `Combine.perKey(CombineFn)`.
 - In each `CombineVia...` class, you must use one of the three methods above **exactly once** (in each class) and no other Combine-related PTransforms or `CoGroupByKey`. This restriction forces you to think of different ways to represent "intermediate data" before turning it into your final output.
 - **IMPORTANT** Even if your code passes unit tests, if your code does not meet the requirements listed in each class, then your score for those tests will be 0. This will be first verified by a simple script, and will also be manually examined.

### Task B: SideInputJob
 - In `SideInputJob.run()` you will find an example and explanation of what should be done.
 - In `FilterVia...` classes under `edu.usfca.dataflow.transforms` package, you will find more details instructions for each class.
 - Specifically, you will implement 3 PTransforms that creates a `PCollectionView` and uses it in a custom DoFn to filter a set of purchase IDs coming from popular store(s). 
 - You will need to use `View.asSingleton()` / `View.asList()` / `View.asMap()` **exactly once** in each Java file, and you are not allowed to use `CoGroupByKey`.
 - **IMPORTANT** Even if your code passes unit tests, if your code does not meet the requirements listed in each class, then your score for those tests will be 0. This will be first verified by a simple script, and will also be manually examined.

### Task C: CoGroupByKey for Combine
 - You will now use `CoGroupByKey` and other pre-defined Combine PTransforms to implement the same pipeline as in A.
 - Specifically, in `CoGbkCombine.java`, you are given much of the code to begin with, and you just need to ensure that the final output is correct. Note that the pre-defined PTransforms provided in this starter code **should NOT** be used in task A (because those are Combine-related PTransforms which you are not allowed to use in task A).
 - Also, I think that finishing Task C first may be wise (as it will help you finish Task A), but that's up to you.

### Task D: Compare the pipelines.
 - You do not have to submit anything for this part, but try to answer these questions.
 - For the combine pipelines (A and C), you essentially designed the same pipeline in four different ways. Which pipeline would you use for your large-scale application, and why? What if your combine logic changes -- would you change your decision?
 - For the filter pipelines (B), try to answer the same questions. Under what circumstances, would you use one pipeline over other pipelines?

### Final Remarks
 - This lab's instructions are rather short, and we've seen examples in lecture / sample code.
 - Lab 04 has no short-answer questions.
 - **READ** the comments/instructions in each java file and **CHECK** unit tests **BEFORE** you start coding; unit tests are intended as sample inputs and outputs, and by working on those examples by hand, you will be able to understand the requirements better and quicker.


## Commits & Testing
 - **Make incremental changes and commit your changes often**.
 - Grading system status: `https://www.cs.usfca.edu/~hlee84/cs686/lab04-status.html` (if the page is blank or not accessible, it means the grading system has not started grading yet.)
 - URL for accessing your reports: You'll be able to download the zip files from the status dashboard above (it requires you login using Dons email account). Only you and the teaching staff can see your reports.
 - If your project does not build (due to compilation errors), then it cannot be graded; don't even push to remote repo. It is your responsibility to run unit tests locally to make sure that all of the provided tests (sample tests) pass.
 - If your commit is stuck at `pending` status for a while, then it probably means something went wrong and it crashed or never terminated. If such is the case, make another push to see if the issue persists. If so, email the instructor (include your repo and sha).
 - **IMPORTANT** Your score may be reduced later if your code is found to violate the requirements. This will be manually checked.

## Starvation of Grading System
 - This lab's unit tests will take longer than previous labs' unit tests.
 - As such, if many of you push commits around the same time, you may experience an extended delay before your commit gets graded (it could be up to several minutes).
 - Since the lab is available for a week, and we have covered all of the topics before the lab was released, it is your responsibility to get it **done and graded** before the deadline.

## Scoring (read carefully)
 - Number of sample/shareable/hidden tests: 17 / 8 / 16
 - If your submission fails any one of the sample tests: Your score will be 0 (This applies to all labs/projects).
 - Otherwise, your overall score will be **60% shareable tests + 40% hidden tests** (this weight may vary from lab to lab).
 - Your score for this lab will be the maximum score you obtain before the deadline (This applies to all labs/projects).
 - **NOTE** Since this lab has requirements that cannot be easily verified by the grading system automatically, your commits will be manually examined by the TAs and instructor; as such, if your code violated some of the requirements, then your score for those unit tests will be 0.
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
