# CS686 Lab 03

### Errata / Corrections
 - (v1) Released on Feb 4 (Tue) around 7:40am.

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


### IDE setup / Code Style / Auto Formatting / Commenting
 - By now you should know how to do this. Refer to Lab 01 instructions & Piazza posts.

### Questions or problems? Search / Ask on Piazza.


### If you don't follow these instructions, there may be a deduction.

## Assignment Details

### Goals
In this lab, you will work with `AppActivity` proto message defined in `profile.proto`.

Our goal is to **aggregate** AppActivity messages properly, using `GroupByKey` and `Combine.perKey`.

For most parts, you only need to modify `SampleJob.java` and refer to `__TestSampleJob.java`.

There are three "mini tasks" for which you need to modify `SampleJob2.java` (these are super simple).

**About Proto Files**
 1. Files are the same as Lab 02 / Project 01. You'll need to copy over your `profile.proto` from Lab 02 to make this lab compilable.
 

### Part 1 - Understanding the business logic
 - Read the comments above `mergeApps()` method in `SampleJob.java` carefully. 
 - As you implement `mergeApps()`, you will want to make sure your code passes all relevant unit tests.
 
### Part 2 - Inline questions/tasks
 - There are seven "Homework" questions/tasks that I left as inline comments in `SampleJob.java`.
 - These will not be graded per se, but you are expected to complete those tasks and answer the questions (this will help you use Beam SDK for more complex pipelines in the future).
 - Future labs and projects will assume that you know how to complete these tasks (namely, tasks given as Homework (1)-(5)), and if you are not able to complete them, then ask on Piazza / during office hours. It's your responsibility to get these completed so that you can refer to your own code in the future (be prepared for longer projects with less instructions!).

### Part 3 - SampleJob2
 - Here, you can practice writing code using CoGroupByKey, Partition, and Flatten. 
 - These are super simple, yet you should get yourself familiar with how they can be used by writing your own code.

### Final Remarks
 - This lab's instructions are rather short, and I suppose it would only take 2-6 hours.
 - Lab 03 has no short-answer questions.
 - **DO** Homework (1)-(5); Homework (6) will be covered in lecture later and we'll come back to Homework (7) (which is optional), but I'm going to assume (for future labs/projects) you all know how to do (1)-(5), once Lab 03 is completed. 
 - Once you finish fixing bugs / implementing methods, the expected output (when you run this as Java program from `java/dataflow`) is as follows (the order of the 8 lines containing AppActivity proto messages can be arbitrary):
 ```
...
WARNING: The following transforms do not have stable unique names: ParDo(Printer), Values
[groupedApps2] {"bundle":"bundle_04","first_at":"1580507100199","last_at":"1580507100199","count_lifetime":"1","count_today":"1","count_per_exchange":{"1":1}}
[groupedApps2] {"bundle":"bundle_02","first_at":"1580466600999","last_at":"1580477400776","count_lifetime":"3","count_today":"3","count_per_exchange":{"2":3}}
[groupedApps1] {"bundle":"bundle_01","first_at":"1580464800123","last_at":"1580486400678","count_lifetime":"6","count_today":"6","count_per_exchange":{"2":1,"3":3,"1":2}}
[groupedApps1] {"bundle":"bundle_04","first_at":"1580507100199","last_at":"1580507100199","count_lifetime":"1","count_today":"1","count_per_exchange":{"1":1}}
[groupedApps2] {"bundle":"bundle_03","first_at":"1580501700991","last_at":"1580501700991","count_lifetime":"2","count_today":"2","count_per_exchange":{"3":2}}
[groupedApps1] {"bundle":"bundle_02","first_at":"1580466600999","last_at":"1580477400776","count_lifetime":"3","count_today":"3","count_per_exchange":{"2":3}}
[groupedApps2] {"bundle":"bundle_01","first_at":"1580464800123","last_at":"1580486400678","count_lifetime":"6","count_today":"6","count_per_exchange":{"1":2,"2":1,"3":3}}
[groupedApps1] {"bundle":"bundle_03","first_at":"1580501700991","last_at":"1580501700991","count_lifetime":"2","count_today":"2","count_per_exchange":{"3":2}}

Process finished with exit code 0
```

## Commits & Testing
 - **Make incremental changes and commit your changes often**.
 - Grading system status: `https://www.cs.usfca.edu/~hlee84/cs686/lab03-status.html` (if the page is blank or not accessible, it means the grading system has not started grading yet.)
 - URL for accessing your reports: You'll be able to download the zip files from the status dashboard above (it requires you login using Dons email account). Only you and the teaching staff can see your reports.
 - If your project does not build (due to compilation errors), then it cannot be graded; don't even push to remote repo. It is your responsibility to run unit tests locally to make sure that all of the provided tests (sample tests) pass.
 - If your commit is stuck at `pending` status for a while, then it probably means something went wrong and it crashed or never terminated. If such is the case, make another push to see if the issue persists. If so, email the instructor (include your repo and sha).

## Grading System
 - Normally, when a new lab assignment is released in the afternoon on Wednesdays, the previous lab is due on the same day at midnight. Thus, the system will prioritize grading the previous lab (that's due) until midnight. 
 - If you believe that the grading system is down, report on Piazza (the chances are, someone else may have reported the same). You should still try to work on the project and making commits until the deadline. Once the issue is resolved, the grading system will resume grading (since every commit has timestamp associated with it, as long as your work makes it to your remote repo on Git, it'll be graded fairly).
 - Note that the grading system is a convenient tool for you to check your score before the deadline, and even if it's down, it's affecting everyone in the same way and therefore deadlines will be extended -- it's your responsibility to get your code working before the deadline, regardless of the grading system. 
 - Also, if it ever goes down, it'll likely go down when there are a lot of simultaneous submissions, and therefore I suggest you begin working on your labs as soon as they are released.

## Scoring
 - Number of sample/shareable/hidden tests: 17 / 11 / 8
 - If your submission fails any one of the sample tests: Your score will be 0 (This applies to all labs/projects).
 - Otherwise, your overall score will be **30% shareable tests + 70% hidden tests** (this weight may vary from lab to lab).
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
