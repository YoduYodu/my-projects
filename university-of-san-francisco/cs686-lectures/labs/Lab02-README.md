## Version History
 - v01: The original version that was released Tuesday night.
 - v02: Revised on Jan 29 (Wed) around 9:50 AM, after someone reported errors/typos in the instructions (see commit history to see what was changed).
 - v03: Revised right after v02 aroudn 9:52 AM. For the `shortAnswer()` method, you should copy-and-paste **the seventh line** not the sixth; note that there is only one line that is exactly 43 characters long anyway, hopefully you can get it right. Sorry about that!

# CS686 Lab 02

### Read the instructions carefully as they differ from previous lab.
 - In case of errors/typos, corrections will be announced on Piazza - please do check Piazza frequently.
 - Many questions you may have can be answered by reading this README file, checking proto3/Beam references, and the sample code from Lecture 02.

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
 - intelliJ: **always** import/open project using `<repo>/java` directory because it contains `settings.gradle` file that tells intelliJ how to understand the project structure. If you import `<repo>` root directory, intelliJ won't understand.
 

### Questions or problems? Search / Ask on Piazza.


## Assignment Details

### Goals
In this lab, you will work with `Protocol Buffers` and `Beam Java SDK`. 

Using the theme of `Mobile Ads`, this lab includes proto files that define interesting (but much simplified) entities relevant to mobile ads. Labs and projects will continue to use this theme to build end-to-end pipelines.

**About Proto Files**
 1. `proto/vendor/openrtb.proto`: 
    * This is one of the standard messages that are used for real-time bidding (RTB) on many ad exchanges. You are not supposed to change this (as this is being used by external clients/partners).
    * We will not really go into the details of this proto in this lab, but it'll be used more in future labs/projects.
    * In Part 0 below, you will find that this proto file leads to a large-size Java file, which may cause a problem for your IDE. Hence, you'll fix that in this lab (so that you won't have to do that for future labs/projects).
 
 2. `proto/common.proto`: 
    * This defines `OsType` and `DeviceId` (I changed `UserId` from Lab 01 to `DeviceId` as I think it's more natural). `DeviceId` is assumed to uniquely identify a mobile device.
    * Don't worry about `uuid` and `webid` for now -- just consider those being some strings representing a device's (unique) id.
 
 3. `proto/bid.proto`: 
    * Imagine that we have live servers that participate in bidding for ads. Our servers will likely to store bidding history as logs (hence, the `BidLog` proto message). 
    * `BidLog` contains `BidRequest` (from `openrtb.proto` above) as one of its fields, in addition to other info such as `Exchange` (enum that's defined a few lines later) and `DeviceId` (which is defined in `common.proto`). 
 
 4. `proto/profile.proto`: This is *the proto file* that you need to modify.
    * Each `??` should be replaced by a keyword (such as `scalar value types` that proto3 supports or map,repeated,etc.). You need to figure out how to fix them (see Part 1 below). 
    * Once fixed, you will be able to build your project via `<repo>/java $ gradle clean; gradle test`. 
    * After that, you'll work on Part 2 to ensure that it also passes all unit tests). 
    * `DeviceProfile` describes a specific mobile device (which is uniquely identified by `DeviceId`) in terms of its in-app activities. 
    * For instance, it contains `AppActivity` messages where each `AppActivity` message would describe this user's activity within a specific app (uniquely identified by `bundle`).

Note: Likely in Lecture 05 or Lecture 06, we will briefly go over these proto messages to understand why it makes sense to define these messages in an application like ad buyers (the demand-side platforms or DSPs).


### Part 0 - intelliJ Issue
 - intelliJ (by default) ignores large files even if they are valid java files.
 - Go to `Help` -> `Edit Custom Properties` which will open up a blank file (if nothing's there) or some properties.
 - Add the following two lines and save:
```
## custom IntelliJ IDEA properties
idea.max.intellisense.filesize=9999999
```
 - The reason this is needed is because one of the proto files (namely, `proto/vendor/openrtb.proto`) will lead to a java file that is as large as 3.5 MB.
 ```
 $ ls -lh java/dataflow/src/generated-sources/main/java/com/google/openrtb/OpenRtb.java
-rw-r--r--  1 haden  staff   3.5M Jan 23 07:00 java/dataflow/src/generated-sources/main/java/com/google/openrtb/OpenRtb.java
```
 - For Eclipse, I'm not sure if something needs to be done or not (please discuss/report on Piazza).

### Part 1 - Fix `profile.proto` and `Main.java`
 - Run `<your repo>/java $ gradle clean; gradle test --continue`; it should fail.
 - You need to fix `dataflow/src/proto/profile.proto` first (other proto files need not and should not be changed).
 - Read the comments with **`TODO`** in the said proto file, and replace the blanks (`??`) by appropriate keywords/types.
 - Note that the kind of types we use in this course will be mostly `string`,`int32`,`int64`, and `map`, besides other messages (that are defined in our proto files). Hence, that's all you need to know to fix this proto. 
 - However, you should refer to proto3 references (see Lecture 02 slides for links) and/or rely on your search skills (remember, you are allowed to search the Internet as much as you want to in this course as long as you do not copy someone's code).
 - Because you are NOT allowed to change anything under `java/judge` (specifically, any unit tests found in `__*.java` files), those unit tests give you a hint (in terms of what the type of each field in proto messages should be). 
 - Pay attention to the error messages you see when gradle test fails (those error messages would give you some type-mistmatch errors).
 - If your code does not build (i.e., `gradle test` fails due to compilation errors, not unit test failures), then your commit will not even be graded (because unit tests can't be run), so do not push your code to remote repo unless it compiles/builds.

### Part 2 - Fix MyOptions.java
 - See the comments with **`TODO`** in `MyOptions` interface.
 - There are four properties: `course`, `directRunner`, `job`, and `debug`.
 - `course` is missing a setter, so you should add it (see the other three setters for reference).
 - Each of the other properties needs one line of change (namely, you need to add some Java annotation to replace the line with `// TODO: ...`).
 - If you pay attention to the unit tests (`__TestBaseOptions.java`; I forgot to change the name of this file/class to `__TestMyOptions.java`), you will be able to figure out what needs to be done.
 - You are welcome to search the Internet as much as you want to, but it's not really necessary.
 - If you've never used the Annotations before, this is a good time to study the basics (we will not go into the details, but we will need to use some annotations as Beam SDK relies on them).

### Part 3 - Implement two new methods in ProtoUtils
 - In class, we saw how a proto message is serialized to a byte array (`byte[]` in Java).
 - We can use the standard Base64 encoding/decoding to represent this `byte[]` (binary) data -- one advantage of doing so is that it's no longer binary (so we can easily read it off the screen) but in plain-text.
 - As such, it'll make sense to implement utility methods that would work for any proto messages, interms of encoding and decoding. These utility methods will be used in future labs/projects.

### Final Remarks
 - See the intelliJ issue (due to the default limit on Java file size) mentioned in Part 0.
 - You can work on Parts 1-3 in any order you'd like, but it's probably the best if you work on Part 1 first.
 - Lab 02 has one short-answer question (see the clarification above, for v03).
 - I believe you can complete this lab pretty easily (but I have been proven wrong in the past, so...), with the help of the sample code from Lecture 02 and the proto3 reference (specifically, `Language Guide (proto3)`). 
 - If you find it too callenging, please share on Piazza (i) where you are stuck and (ii) what you have tried; you can be anonymous.

## Commits & Testing
 - **Make incremental changes and commit your changes often**.
 - Grading system status: `https://www.cs.usfca.edu/~hlee84/cs686/lab02-status.html` (if the page is blank or not accessible, it means the grading system has not started grading yet.)
 - URL for accessing your reports: You'll be able to download the zip files from the status dashboard above (it requires you login using Dons email account). Only you and the teaching staff can see your reports.
 - If your project does not build (due to compilation errors), then it cannot be graded; don't even push to remote repo. It is your responsibility to run unit tests locally to make sure that all of the provided tests (sample tests) pass.
 - If your commit is stuck at `pending` status for a while, then it probably means something went wrong and it crashed or never terminated. If such is the case, make another push to see if the issue persists. If so, email the instructor (include your repo and sha).

## Grading System
 - Normally, when a new lab assignment is released in the afternoon on Wednesdays, the previous lab is due on the same day at midnight. Thus, the system will prioritize grading the previous lab (that's due) until midnight. 
 - If you believe that the grading system is down, report on Piazza (the chances are, someone else may have reported the same). You should still try to work on the project and making commits until the deadline. Once the issue is resolved, the grading system will resume grading (since every commit has timestamp associated with it, as long as your work makes it to your remote repo on Git, it'll be graded fairly).
 - Note that the grading system is a convenient tool for you to check your score before the deadline, and even if it's down, it's affecting everyone in the same way and therefore deadlines will be extended -- it's your responsibility to get your code working before the deadline, regardless of the grading system. 
 - Also, if it ever goes down, it'll likely go down when there are a lot of simultaneous submissions, and therefore I suggest you begin working on your labs as soon as they are released.

## Scoring
 - Number of sample/shareable/hidden tests: 10 / 6 / 7
 - If your submission fails any one of the sample tests: Your score will be 0 (This applies to all labs/projects).
 - Otherwise, your overall score will be **50% shareable tests + 50% hidden tests** (this weight may vary from lab to lab).
 - Your score for this lab will be the maximum score you obtain before the deadline (This applies to all labs/projects).
 - There's no hard limit on how often / how many times you can submit as long as you do not harm anyone else's grading experiences.
 - **With that said, try not to overload the grading system by pushing too many commits (one per 5 minutes should be fine).** 
 - **Try not to push to remote repository if your project does not compile locally, as it won't be graded anyway.**

## After securing 10,000 points = 100% for this lab (which all of you will!)
 - Once you secure 100% for this assignment, try to add some tricky unit tests on your own (and feel free to share such test cases on Piazza, even before the deadline).
 - Also, try to `simplify` your code if that's possible; it does not necessarily mean you should do everything in a single line or something like that. Rather, try to cut out unnecessary blocks, logic, etc. to make better code.

## Honor Code
 - Do not ever share/show/post your code. That's an automatic F.

## Questions?
 - Please ask on Piazza.
 - For technical issues, you are allowed to share any error messages or the like.
