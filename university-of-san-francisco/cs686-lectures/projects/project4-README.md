# CS686 Project 4

### Errata / Corrections
 - (v0) Released on Mar 20 (Fri) around 5pm.
 - (v1) Mar 21 (Sat) aroudn 9:18am: Added "Sample" unit tests (24 -> 33). For those who accepted the Github assignment, I will email you so you can obtain the new files ([see here](https://github.com/cs-rocks/cs686-lectures/tree/master/projects/project4-data)). All submissions will be regraded shortly. 
 - (v2) Mar 21 (Sat) around 4:25pm: Clarified "Hint" right before `Task C` below (but that'll only be useful when you're nearly done with the project). 
 - (v3) Mar 31 (Tue) around 1:38pm: You do not have to submit your Dataflow Job IDs. I can fetch all Dataflow jobs & metrics, so I'll grade them and let you know the results via email. Note that this only applies to those who finish both Tasks A and B (9000/10000). If you pass all tests and run your job on GCP, you're very likely to receive a full credit for Task C as well (I will simply run some sanity-checks).


## BEFORE YOU BEGIN
 - The pipeline you are going to design in Project 4 is **identical** to that from Project 3 (in terms of the logic/input data).
 - We will focus on optimizing the pipeline in this project. 
 - If you have NOT completed Project 3, then you will need to complete it first (in order to complete Project 4); you'll receive an email from me about this.
 - If you have NOT setup your GCP project for Dataflow yet, then you should follow the instructions in Lecture 20 (L20) slides. This project requires you run an actual Dataflow job on GCP (and the said job will read input data files I will copy to your GCS bucket).

## Assignment Details

### Goals

 - In project 3, you only focused on getting "correct results" from your pipeline.
 - In project 4, on top of that, we will focus on how efficient your pipeline is.
 - Efficiency can be measured in many different ways, and we will focus on "the size of intermediate data" based on how you defined your `PurchaserProfile` proto message.
 - We will study **why** it is important to use compact representation of intermediate data (L26-L28); we discused this briefly in earlier lectures (L09-L10), and now we are going deeper into this topic (L26-L28).

### Task A (Preliminary Steps)
 1. First, make sure you followed all of the instructions in L20 about setting up your GCP project for Dataflow and granted owner access to me.
 1. Copy some of the files from project 3 to project 4. Specifically, `profile.proto`, `PurchaserProfiles.java`, and `ExtractData.java`. 
 1. I recommend you use the provided `LogParser.java` (because if you use your own parser, it may result in unexpected results later).
 1. Build your gradle project, and make sure some unit tests pass and some fail (which is expected).
 1. Most unit tests are the same as project 3, but `__TestWith05ByteSize` is new -- this will be explained later, so don't worry about it. **Make sure your code passes all the other unit tests**. If your code passes all other tests, then I think you're in good shape. It's time to move on to the next Task.

### Task B (Main Task)
 1. You're here if your code passes all unit tests except for the ones in `__TestsWith05ByteSize`.
 1. First, take a look at the implementation of `__testHelper` method.
 1. It simply calculates the sum of serialized size of your `PurchaserProfile` messages. In case you forgot, recall that we discussed/studied the size of a proto message (the length of a byte array) in [Lecture 02](https://github.com/cs-rocks/cs686-lectures/blob/master/L02/java/dataflow/src/main/java/edu/usfca/dataflow/Main.java#L12-L21) as well as in Lab 01.
 1. `__testHelper` method: If the sum of serialized size of **your** `PurchaserProfile` proto messages is below some threshold, then you would pass a unit test. 
 1. So what's the threshold? Before talking about the threshold, let's revisit the pipeline.
    1. To read JSON logs and parse them into `PurchaseEvent` protos 
    1. To create `PurchaserProfile` from `PurchaseEvent` protos and `DeviceId`s 
    1. To merge them (one per user) and 
    1. To extract useful data (IAP profiles, high spenders, and addicts).
 1. All unit tests in `__TestsWith05ByteSize` are measuring the size of the intermediate data (PCollection) right between step (ii) and step (iii), by simply adding up the serialized size of your `PurchaserProfile` proto messages (`PCollection<PurchaserProfile>`).
 1. Why? Because that's exactly the amount of data that'll be serialized/de-serialized between machines and/or threads. You want to minimize the size of it to make your pipeline run efficiently (of course, this is NOT the only step where such data transmission would occur, but it is the one step that's most inefficient in this specific pipeline). 
 1. Most of you chose to have `repeated PurchaseEvent xxx = x;` field in your `PurchaserProfile`. That makes things "easy" for aggregating the values, but it is super inefficient; you are holding on to (excessively large) `PurchaseEvent` protos when you do not have to. 
 1. Some of you chose to have one or more of `map<string, ???> xxx = x;`. This is actually a good starting point. Try to define your own proto message that concisely holds on to the data that you need (purchase amounts, timestamp values, etc.) per bundle.
 1. As such, these unit tests actually compare the total size of your `PurchaserProfile` protos against the size of `DeviceId` plus `repeated PurchaserEvent` protos; if yours is below 75% of the latter for all input cases, you'll pass all unit tests for the first four files (`FILE01` - `FILE04`).
 1. There are new data files (`java/dataflow/resources/test-*.txt`, `FILE05`-`FILE07`). For these the threshold is lower (your proto size should be less than 10% of `repeated PurchaseEvent` protos); these will be tested via hidden unit tests (but they are the same as shareable tests).

### So, what do you need to do for Task B?
 - First, instead of `repeated PurchaseEvent xxx = x;`, consider a different way to store the minimum amount of information you need in order to implement the three PTransforms in `ExtractData` class (try to do that one by one).
 - You can add many fields to `PurchaserProfile` and even inner-proto messages to it (if you find it useful).
 - In case it's not obvious: If you keep track of the purchase amount, the number of purchases, and the UNIX days (not millis) of purchases (all such per app bundle), then you can implement all PTransforms in `ExtractData`. With this, get rid of `repeated PurchaseEvent` field, and come up with more concise representation for `PurchaserProfile`.
 - When you change your proto message, you will also need to change your implementations in `ExtractData`, unfortunately. That is part of this project.
 - **NOTE** In Lectures L26-L28 (Mar 30- Apr 3), we will discuss how to design efficient pipelines and what we need to consider when we choose representations for intermediate data.
 - Here's hint:
<details><summary>Click for Hint</summary>
<p>

 - In your `PurchaserProfile` proto, besides the first two fields (that I provided), remove everything else you added.
 - Simply have one additional field: `map<string, MyProto> data = 3;`. (In case it's not obvious, the key string represents an app bundle.)
 - Now define your `MyProto` message based on what is needed in the 3 PTransforms in `ExtractData` class. 
 - `MyProto` should not have `PurchaseEvent` proto in it; in fact, it only needs integer-type fields (`repeated` is fine).
 - For instance, you should have fields that store the number of purchases and the total amount of purchases.
 - Note that you will actually **merge** `PurchaserProfile` protos per user, so `MyProto` should be able to represent aggregated values (such as sums or counts); this may help you come up with what you need.
 - With this approach, you should be able to pass most of the tests in `__TestWith05ByteSize` (probably except for the tests ending in `75`). 
 - Also, you may be so close to passing all `PostMerge` tests as well (check the console output when you run unit tests). See my answer in this [Piazza post](https://piazza.com/class/k5ad4g8m2jf6t6?cid=223) for hints.
 - To pass all tests, you need to apply one more optimization technique that we discussed in class. (If you're so close to `75%` but just a little bit over the threshold, then wait for L26-L28. Or, try to find a way to represent `map<A,B>` in a more concise manner. I'll discuss this in one of those lectures.)

</p>
</details> 

### Task C (Let's run it on GCP!)

**You should do this only if your code passes ALL unit tests (otherwise, this task will not make sense).**

**Note that on the grading dashboard, if you see `9000` (not `10000`) then you are passing all tests (yay).**

 - Copy `Main.java` from your Project 4 repo to your Project 3 repo, and change `OUTPUT_SPENDER_FILE` and `OUTPUT_ADDICT_FILE` to `project3` accordingly.
 - Run it (run `main()` method from your IDE or do `gradle run` from command-line under `java/dataflow`) once in your `cs686-proj3-xxx` repo and once in your `cs686-proj4-xxx` repo.
 - Check the Dataflow Web Console page, and compare the two jobs. Your job from project 3 may fail (that's OK and that's expected). Your job from project 4 should run smoothly. 
 - Once you are done running it, simply check the results (outputs) from `cs686-s06` and `cs686-09` steps. The former should produce six elements (Addicts) and the latter 1738 elements (High Spenders). You can check these from the Dataflow Web Console page (and this is one of the things I check when I grade your Dataflow jobs ).
 - This task is worth 10% of this project's grade (`1000/10000`); if your pipeline runs on GCP and produces the correct results, then you will get that 10%. This will be graded semi-manually (only after you secure 9000 points of 10000).
 - How to obtain the input data: If you have granted "owner access" to me successufully (according to L20), then you should expect to see data files in your GCS bucket(s) some time after March 20th. If you have not done that yet, there will be a delay until you get the data (recall that you are supposed to complete the setup by Mar 20 per the instructions from L20). If you do not see the data files, ask the teaching staff on Piazza. 
 - How will this be graded: You do not have to do anything after you run Dataflow jobs. Within a day or two, those jobs will be graded by instructor, and you'll receive an email.


**IDE stuff**
 - intelliJ users: Always OPEN/IMPORT the `java` directory as the root (entry point) of your project, not your repository. That way, intelliJ understands better how this gradle project is confirued.


### Final Remarks
 - This project is due in 3 weeks (not 2 weeks).
 - Use your time wisely. This project is worth 10% of your course grade, and you should allocate more time to projects than to labs (each lab is worth at most 2% of your course grade).
 
## Commits & Testing
 - **Make incremental changes and commit your changes often**.
 - Grading system status: `https://www.cs.usfca.edu/~hlee84/cs686/proj4-status.html` (if the page is blank or not accessible, it means the grading system has not started grading yet OR it's gone down...)
 - URL for accessing your reports: You'll be able to download the zip files from the status dashboard above (it requires you login using Dons email account). Only you and the teaching staff can see your reports.
 - If your project does not build (due to compilation errors), then it cannot be graded; don't even push to remote repo. It is your responsibility to run unit tests locally to make sure that all of the provided tests (sample tests) pass.
 - If your commit is stuck at `pending` status for a while, then it probably means something went wrong and it crashed or never terminated. If such is the case, make another push to see if the issue persists. If so, email the instructor (include your repo and sha).


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


## Scoring (new rules for projects)
 - Number of sample/shareable/hidden tests (tentative): 33 / 33 / 8
 - **NOTE** Although it's unlikely that I'll change the number of test cases, it is still a possibility, and I'll try my best to keep you all posted in timely manner.
 - If your submission passes ALL of the sample tests (33 of them): your overall score will be: **66% shareable tests + 24% hidden tests**. (The extra 10% comes from Task C.)
 - If your submission only passes `x` sample tests out of 33, then your overall score will be: **`(x/33)*(x/33)*(66% shareable tests + 24% hidden tests)`** Notice that your score is penalized by a factor of `((x/33)*(x/33))`.
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
