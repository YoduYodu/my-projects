# CS686 Project 1


### Errata / Corrections
 - (v1) Project 1 is live! 8:12pm-ish. Link is available on Canvas.
 - (v2) There are **20** "sample tests" under java/judge, not **27** (I counted it wrong again...). "shareable" and "hidden" tests are yet to be determined.
 - (v3) Four input files are added to this path to help you test your pipelines: https://github.com/cs-rocks/cs686-lectures/tree/master/projects/project1-data This will be useful when you are almost done with all tasks (so you can run your pipeline end-to-end).
 - (v4) As of Feb 1 at 5pm, the grading system began to grade project 1 submissions. ([Click for  status](https://www.cs.usfca.edu/~hlee84/cs686/proj1-status.html)) 
 - Note: There are 20 basic tests, 14 shareable tests, and 10 hidden tests. (Your score := 60% shareable + 40% hidden.)
 - (v5) Clarification regarding how you should convert `BidLog` proto to `DeviceProfile` proto: 
   - You should utilize `BidLog.BidRequest.Device.os()` and `BidLog.BidRequest.Device.ifa()` (uuid equivalent) for `DeviceId`.
   (I know that `BidLog` proto also has `DeviceId` field, which I should have removed it before releasing this assignment. Consider that field non-existent, for this assignment.)
   - You should utilize `BidLog.recevAt` (timestamp in milliseconds at which the request was received by our servers) when you set `firstAt/lastAt` of `DeviceProfile` and `AppActivity`.
   - You should utilize `BidLog.BidRequest.App.bundle` for `AppActivity`'s bundle (this is a unique ID for each app).
   - You should utilize `BidLog.exchange` for updating the count map in `AppActivity` (key being the enum value of `Exchange` proto).
   - Lastly, for `countToday` in `AppActivity`, consider each `BidLog` being one in-app activity event. Therefore, when we initially convert one `BidLog` to one `DeviceProfile`, then each counter (`countToday` and `countPerExchange`) all must have a value of 1 each (as there is one event being considered). Later, when we aggregate `DeviceProfile` protos for each user, we would naturally sum up the counters to get an accurate value. 
   - If you need further clarification, please ask on Piazza.
  - (v6) Note: `count_lifetime` field in `AppActivity` proto should be actually removed (and you shouldn't set its value at all). This was mentioned in class, but was not clearly included as a part of the instructions (and previous instruction was misleading), so here it goes: https://piazza.com/class/k5ad4g8m2jf6t6?cid=67 (Bottom-line: just remove the line `int64 count_lifetime = 4;` in your profile.proto.) 

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
 - By now you should know how to do this. Refer to Lab 01 instructions & Piazza posts.

### Questions or problems? Search / Ask on Piazza.


### If you don't follow these instructions, there may be a deduction.

## Assignment Details

### Goals

**Intro**
In this project, you will design your first major data processing pipeline using `Beam Java SDK`.
The project is divided into tasks so that you can work on each task separately (if you want to).
You can find `TODO: ...` comment in `main()` of `Main.java` under `java/dataflow` that describes what each task is.
Essentially, each task describes a small part of the larger pipeline.

Given input file(s) that contain Base64-encoded `BidLog` messages (assume that this is 1-day worth of data), your pipelie should produce multiple `PCollections`.
 - The main output is `PCollection<DeviceProfile>` that summarizes `DeviceProfile` message for each device (so, one `DeviceProfile` per `DeviceId`). 
 - Along the way, we want to also compute popular apps (as `PCollection<String>`) that are used by some number of unique users (based on DeviceIDs).
 - In addition, some input data may be corrupted or not useful, and we should take them out separately for debugging purposes.
 
More details about the proto messages and exactly what you need to do are described below.

**Proto Files**
 - Proto files are similar to the ones you saw in Lab 02.
 - **IMPORTANT** For `profile.proto`, if you have completed Lab 02, then re-use your `profile.proto` file after commenting out or removing the following line: `// int64 count_lifetime = 4;` (this field will not be used in this project, and it's better to remove it to avoid unexpected errors.)
 - If you have NOT completed Lab 02, then complete it first. I will upload `profile.proto` after the deadline for Lab 02 passes.
 - Descrptions of these proto messages were provided in Lab 02 README, so refer to them.

**IDE stuff**
 - intelliJ users: Always OPEN/IMPORT the `java` directory as the root (entry point) of your project, not your repository. That way, intelliJ understands better how this gradle project is confirued.

**Before making code changes**
 1. First, read `Main.java` carefully, before making any changes. I left many comments, and they serve as detailed instructions. Also, I recommend you do NOT change anything in `main()` method initially, and rather fix other parts of the project (by following the `TODO` comments).
 2. Run your Java program from command-line once and also from your IDE (so as to confirm things work as intended). 


**Tasks**
You do NOT have to work on these tasks in this order, so feel free to work on parts that look easier first.

 1. Implement `Helper.readFromFile()`. 
    - It takes `Pipeline` object and `String` (path to file), and it should return `PCollection<String>`. 
    - This can be done using `TextIO.read()`, and you should refer to Lecture 05 slides, Beam Programming Guide references (available from lecture slides), and/or Beam SDK source code. 
    - As this project is the first major assignment, you are given detailed instructions, but you will be given less, going forward. 
    - You need to figure out what references you find most useful for you - I sometimes find reading the source code easier but at other times I find the Java API doc or Beam Programming Guide easier.
 2. Implement `BidLogParser` class.
    - Its signature is already provided, along with its constructor.
    - Using that as a hint, you should implement your method (with `@ProcessElement` annotation).
    - There are many different styles for this (you may use `@Element` annotation (newer style) or `ProcessContext` object (older style)). Either way is totally fine.
    - Input data is `PCollection<String>` that supposedly contains Strings that are Base64-encoded, serialized messages of `BidLog` data. 
      - Now, if an input element (`String`) is incorrectly encoded, then `Base64.decoder` would complain (by throwing an exception). This should be caught and the input element should be emitted using `corruptTag` for further investigation.
      - Similarly, even though the input element can be decoded properly into a byte array, it may not be a valid `BidLog` proto message in which case `parseFrom` would throw an exception. This should be caught and also emitted using `corruptTag`.
      - I do not think you will need to handle other exceptions (to complete this project), but if you think there's an error in the grading system or tests I use, feel free to ask. Just make sure that your method will NEVER throw the said exceptions above (they must all be caught or your entire pipeline may fail).
    - See unit tests in `__TestParsers` class for example `BidLog`s and expected results.
  3. Implement `BidLog2DeviceProfile` class. 
     - Now, given that we have decoded the input data and obtained `BidLog` messages as `PCollection<BidLog>`, we want to transform each `BidLog` into a `DeviceProfile` proto. We will `merge/aggregate` these `DeviceProfile` protos in the next task so let's not worry about having multiple `DeviceProfile` protos for the same device/user in our PCollection at this point.
     - `BidLog` proto contains `BidRequest` proto (as a field), which in turn contains `Device` proto that contains `Os` (String) and `Ifa` (String). For this project, if the `Device.os` is `ios` or `android` (case-insensitive), then we'd accept it and create a `DeviceId` (see `common.proto`) out of it, using `ifa` as `uuid`. On the other hand, if `Device.os` is neither, then we will assume that `BidLog` is faulty, and emit it as side output. Hence, do use the provided `TupleTag`s.
     - As `BidRequest` proto is quite complex, I do not expect you to examine every single field. Instead, I am providing sample code that extract useful parts of it, so you should have no problem creating `DeviceProfile` (also, unit tests are the hints!). 
  4. Implement `Helper.getMergedProfiles` method.
     - Essentially, you'll need to implement two helper methods in `ProfileUtils` class. See the detailed instructions regarding the merge rule in this file. 
  5. Implement `Helper.getPopularApps` method.
     - Find detailed instructions provided in the code. 
     - My reference solution is rather short (< 10 lines), but this method can be implemented in many different ways, so try to find different correct solutions (it'll help you understand various PTransforms in Beam SDK).
  6. Implement `Helper.getExchangeStats` method. (Optional/extra credit for CS 486 students. Required for CS 686 students.)
     - This is similar to task 5 (PopularApps), but is slightly more complex.
     - Again, there are many many many different ways to implement this correctly, and I'd be happy to see some creative solutions.


### Final Remarks
 - **Code Style** I will take a look at 1-2 commits of yours (probably the final commit), and try to leave useful comments. For this first project, don't worry too much about your code style; rather, focus on getting things right (correctness-wise), and then improve your code later.
 - Grading system will begin grading commits starting on Monday (Feb 3); this is mainly because I need more time to prepare additional shareable/hidden tests.
 - When it begins grading, the status dashboard will become available and it will show how many unit tests (of each type) you passed. 
 - Note that, your score would be 0 if your code fails any of the provided sample tests, **but** you can still make progress by passing more shareable/hidden tests. As such, try to make incremental commits by working on one of the six tasks at a time.

## Commits & Testing
 - **Make incremental changes and commit your changes often**.
 - Grading system status: `https://www.cs.usfca.edu/~hlee84/cs686/proj1-status.html` (if the page is blank or not accessible, it means the grading system has not started grading yet OR it's gone down...)
 - URL for accessing your reports: You'll be able to download the zip files from the status dashboard above (it requires you login using Dons email account). Only you and the teaching staff can see your reports.
 - If your project does not build (due to compilation errors), then it cannot be graded; don't even push to remote repo. It is your responsibility to run unit tests locally to make sure that all of the provided tests (sample tests) pass.
 - If your commit is stuck at `pending` status for a while, then it probably means something went wrong and it crashed or never terminated. If such is the case, make another push to see if the issue persists. If so, email the instructor (include your repo and sha).

## Grading System
 - Normally, when a new lab assignment is released in the afternoon on Wednesdays, the previous lab is due on the same day at midnight. Thus, the system will prioritize grading the previous lab (that's due) until midnight. 
 - If you believe that the grading system is down, report on Piazza (the chances are, someone else may have reported the same). You should still try to work on the project and making commits until the deadline. Once the issue is resolved, the grading system will resume grading (since every commit has timestamp associated with it, as long as your work makes it to your remote repo on Git, it'll be graded fairly).
 - Note that the grading system is a convenient tool for you to check your score before the deadline, and even if it's down, it's affecting everyone in the same way and therefore deadlines will be extended -- it's your responsibility to get your code working before the deadline, regardless of the grading system. 
 - Also, if it ever goes down, it'll likely go down when there are a lot of simultaneous submissions, and therefore I suggest you begin working on your labs as soon as they are released.

## Scoring
 - Number of sample/shareable/hidden tests: 27 / 14 / 10 (see https://github.com/cs-rocks/cs686-lectures/blob/master/projects/project1-README.md)
 - If your submission fails any one of the sample tests: Your score will be 0 (This applies to all labs/projects).
 - Otherwise, your overall score will be **60% shareable tests + 40% hidden tests** (this weight may vary from project to project).
 - Your score for this project will be the maximum score you obtain before the deadline (This applies to all labs/projects).
 - There's no hard limit on how often / how many times you can submit as long as you do not harm anyone else's grading experiences.
 - **With that said, try not to overload the grading system by pushing commits more often than one per 10 minutes (or so).** 
 - **Try not to push to remote repository if your project does not compile locally as it won't be graded anyway.**

## After securing 100% (which all of you will)
 - Once you secured 100% for this assignment, try to add some tricky unit tests on your own (and feel free to share such test cases on Piazza, even before the deadline).
 - Also, try to `simplify` your code if that's possible; it does not necessarily mean you should do everything in a single line or something like that. Rather, try to cut out unnecessary blocks, logic, etc. to make better code.

## Honor Code
 - Do not ever share/show/post your code. That's an automatic F.

## Questions?
 - Please ask on Piazza.
 - For technical issues, you are allowed to share any error messages or the like.
