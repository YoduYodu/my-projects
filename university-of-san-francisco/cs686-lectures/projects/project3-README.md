# CS686 Project 3

### Errata / Corrections
 - (v0) Released on Feb 28th around 1:20am.
 - (v1) For `os` field in the http URL, it's actually not given but you should infer it from `ios_idfa` or `gps_adid` (see the changes below). I updated the starter code around 8:40pm on Feb 28th, but if you accepted the assignment before that, then you should read the following for the changed wording: (right before `getIdAndPurchaseEvent` in `LogParser.java`)
 ```
   /**
   * Parsing rules (of the log messages) are mostly the same as in Lab 05.
   *
   * Here are some "new" rules/differences:
   *
   * (1) Now you should return KV &lt; DeviceId, PurchaseEvent &gt; (not just PurchaseEvent proto) where DeviceId should
   * be parsed based on the query URL (this is the new thing you need to incorporate).
   *
   * "uuid" will be given as either "ios_idfa" parameter (for iOS) or "gps_adid" (for android). If both parameters are
   * non-empty or both are empty, then consider the input log invalid. Also, note that these two fields are
   * case-insensitive, and thus you should normalize them here.
   *
   * (2) When input log is invalid, return null (instead of throwing an exception).
   *
   * ------------------------------------------------------------------------------
   *
   * You can copy your LogParser class from Lab05, and make necessary changes.
   *
   * OR, you can use the instructor's sample LogParser class (from reference solutions), and make necessary changes.
   *
   * Either way is acceptable, but you still need to ensure that your code passes all unit tests of this project.
   */
```
 
## Do's and Don'ts

### Do's:
 - You can do whatever you want with the files under `java/dataflow` directory, including `build.gradle`.
 - For instance, if you find it useful to import some libraries, you can do that in `build.gradle`.
 - You can modify/add/remove any packages/files/classes/methods as you like.
 - You can (and should) add more unit tests (the more the better -- always consider 'corner cases') to `java/dataflow`.
 - Try to apply appropriate design patterns/principles (Effective Java by Joshua Bloch), if applicable. 

### Don'ts:
 - Do not change anything outside `java/dataflow` directory (if you have to, ask first on Piazza; in most cases, it'd be either (a) you're doing something unexpected or (b) the starter code's lacking some files/etc. in which case I'll fix it so that you don't have to)
 - Specifically, `java/judge/*`,`java/settings.gradle`,`.github/workflows/*` should NOT be touched at all.
 - Do not add/remove/modify any other files than `*.java` or `*.proto` (except for `java/dataflow/build.gradle`, if you must import some libraries).


### IDE setup / Code Style / Auto Formatting / Commenting
 - Some of you reported the issues with Eclipse/intelliJ where your IDE cannot recognize `OpenRtb.java` as a Java file (hence the references are broken because of that). This is because `OpenRtb.java` file is too large (> 3.5MB), and it makes your IDE think that it is not a Java file but something else (even if its file extension is Java).
 - You should check Lab 02 instructions and Piazza posts; sometimes you have to remove some "cache" set by your IDE (in intelliJ's case, you should remove the hidden directory called `.idea` under your repo's `java` directory).
 - If you're having issues, ask on Piazza. It's your responsibility to ask and get necessary help (if you don't ask, I can't help you).

### Questions or problems? Search / Ask on Piazza.


### If you don't follow these instructions, there may be a deduction.

## Assignment Details

### Goals

**Intro**

You will implement three big tasks.

**Task A** It's a repeat of Lab 05 in that you'll write a method that parses JSON log, and produces a proto message. 
Note that there'll be instructor's reference solution (for lab 05), which you can copy-and-modify. You can choose to use yours (it won't matter as much).

**Task B** You'll define a proto message that's used as "intermediate" data representation. I recommend you read about Task C before you define your proto message here.
Then, you'll do some "Combine" logic here (to aggregate things at user-level).

**Task C** This is to extract useful data from your intermediate data. See the instructions in the Java files.


**Proto Files**
 - `profile.proto` will be the most important. Read the comments carefully.

**IDE stuff**
 - intelliJ users: Always OPEN/IMPORT the `java` directory as the root (entry point) of your project, not your repository. That way, intelliJ understands better how this gradle project is confirued.

**Tasks**
In `Main.java` file, the three tasks are summarized and which files need to be implemented.

### Final Remarks
 - **Code Style** I will take a look at 1-2 commits of yours (probably the final commit), and try to leave useful comments. For this first project, don't worry too much about your code style; rather, focus on getting things right (correctness-wise), and then improve your code later.
 - Grading system will begin grading commits on Saturday or Sunday (Feb 29 or Mar 1).
 - When it begins grading, the status dashboard will become available and it will show how many unit tests (of each type) you passed.

## Commits & Testing
 - **Make incremental changes and commit your changes often**.
 - Grading system status: `https://www.cs.usfca.edu/~hlee84/cs686/proj3-status.html` (if the page is blank or not accessible, it means the grading system has not started grading yet OR it's gone down...)
 - URL for accessing your reports: You'll be able to download the zip files from the status dashboard above (it requires you login using Dons email account). Only you and the teaching staff can see your reports.
 - If your project does not build (due to compilation errors), then it cannot be graded; don't even push to remote repo. It is your responsibility to run unit tests locally to make sure that all of the provided tests (sample tests) pass.
 - If your commit is stuck at `pending` status for a while, then it probably means something went wrong and it crashed or never terminated. If such is the case, make another push to see if the issue persists. If so, email the instructor (include your repo and sha).


## Scoring (new rules for projects)
 - Number of sample/shareable/hidden tests (tentative): 19 / 19 / 6 (see https://github.com/cs-rocks/cs686-lectures/blob/master/projects/project3-README.md) 
 - **NOTE** Although it's unlikely that I'll change the number of test cases, it is still a possibility, and I'll try my best to keep you all posted in timely manner.
 - If your submission passes ALL of the sample tests (19 of them): your overall score will be: **75% shareable tests + 25% hidden tests** (this weight may vary from project to project).
 - If your submission only passes `x` sample tests out of 19, then your overall score will be: **`(x/19)*(x/19)*(70% shareable tests + 30% hidden tests)`** Notice that your score is penalized by a factor of `((x/19)*(x/19))`.
 - Your score for this project will be the maximum score you obtain before the deadline (This applies to all labs/projects).
 - There's no hard limit on how often / how many times you can submit as long as you do not harm anyone else's grading experiences.
 - **With that said, try not to overload the grading system by pushing commits more often than one per 10 minutes (or so).** 
 - **Try not to push to remote repository if your project does not compile locally as it won't be graded anyway.**

## After securing 100% (which most of you will)
 - Once you secured 100% for this assignment, try to add some tricky unit tests on your own (and feel free to share such test cases on Piazza, even before the deadline).
 - Also, try to `simplify` your code if that's possible; it does not necessarily mean you should do everything in a single line or something like that. Rather, try to cut out unnecessary blocks, logic, etc. to make better code.

## Honor Code
 - Do not ever share/show/post your code. That's an automatic F.

## Questions?
 - Please ask on Piazza.
 - For technical issues, you are allowed to share any error messages or the like.
