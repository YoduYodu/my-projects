# CS686 Project 2

# Big Notice
Many of you seem to "begin" working on labs/projects on the day they are due... which I can understand, but certainly not recommend. 

**I dare you try that for project 2.** :-)


### Errata / Corrections
 - (v1) Project 2 is live - around 6:06pm on Friday (Feb 14). Please report errors / ask questions on Piazza (and if you feel like your question can be public, please make it public!). 
 - (v2) **NOTE** Read the "scoring" section at the bottom as it has changed. Minor changes in wording.
 - (v3) In **DeviceProfilesLifetime** class, there was a typo for "case 3" condition: 
```
(case 3) If both PrevDayPC and TodayPC have a profile each, then merge the two profiles based on the same rule
  * as what we used for the daily pipeline. In this case, there is one condition to check (for consistency): If
  * previous day's "last_at" is no later than today's "first_at", then throw CorruptedDataException.
```
The statement above contradicts `testMerge03()` in `__TestDeviceProfilesLifetime`. The test is correct, and the statement should be `If previous day's last_at is no earlier than today's first_at` (instead of `no later than`). That is, if previous day's `last_at` is no earlier than today's `first_at`, then the data should be considered corrupted.

Coincidentally, there was one unit test that was missing in the "shareable" suite. Everyone's submission will be re-graded soon after this README file is updated (around 4pm on Feb 25, Tuesday). For your convenience, here's the new unit test:

```
  @Test
  public void __shareable__testExceptionHandling06() {
    try {
      DeviceId did1 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(__TestBase.UUID1).build();
      DeviceId did3 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(__TestBase.UUID1.toUpperCase()).build();

      GeoActivity.Builder geo1 = GeoActivity.newBuilder().setCountry("usa").setRegion("ca");
      GeoActivity.Builder geo2 = GeoActivity.newBuilder().setCountry("usa").setRegion("CA");

      AppActivity.Builder app1 =
          AppActivity.newBuilder().setBundle(__TestBase.Bundle1).putCountPerExchange(Exchange.INMOBI_VALUE, 4);
      AppActivity.Builder app2 = AppActivity.newBuilder().setBundle(__TestBase.Bundle1.toUpperCase())
          .putCountPerExchange(Exchange.MOPUB_VALUE, 2);

      PAssert.that(PCollectionList
          .of(tp.apply(Create.of(DeviceProfile.newBuilder().setDeviceId(did1).setFirstAt(2000L).setLastAt(2000L)
              .addGeo(geo1).setLatestGeo(geo1).addApp(app1.setFirstAt(2000L).setLastAt(2000L)).build())))
          .and(tp.apply(Create.of(DeviceProfile.newBuilder().setDeviceId(did3).setFirstAt(1000L).setLastAt(1000L)
              .addGeo(geo2).setLatestGeo(geo2).addApp(app2.setFirstAt(1000L).setLastAt(1000L)).build())))
          .apply(new UpdateLifetimeProfile())).satisfies(out -> {
            fail();
            return null;
          });

      tp.run();

      fail();
    } catch (PipelineExecutionException e) {
      assertTrue(e.getCause() instanceof CorruptedDataException);
    }
  }

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

As mentioned in Lecture 11 (Feb 14), "Mobile Ads and Real-time Bidding" is the theme of our course projects.
In this project, specifically, you will work on three major PTransforms.
 - `DeviceProfilesDaily`: Imagine this job running on a daily basis, by transforming 1-day's `BidLog`s to `DeviceProfile`s and aggregate them. 
 - `DeviceProfilesLifetime`: Perhaps we have this "lifetime" dataset of DeviceProfiles (as of yesterday), and we would like to merge today's "daily" dataset into it. This is similar to the daily pipeline, except that we have two PCollections of DeviceProfile to be merged. 
 - `AppProfiles`: After the two jobs above are done for "today", we would also like to produce some app-specific data (e.g., the number of unique users lifetime and daily -- the latter is usually referred to as DAU or daily active users).

**Proto Files**
 - Proto files have been updated, and you do not have to change anything. However, you are allowed to change (except for the ones marked as a definite no-no).

**IDE stuff**
 - intelliJ users: Always OPEN/IMPORT the `java` directory as the root (entry point) of your project, not your repository. That way, intelliJ understands better how this gradle project is confirued.

**Before making code changes**
 As soon as you check out the starter code, run unit tests. It should build/compile. If it does not, then I recommend you fix your IDE issues (see my comments above), before working on it. Otherwise, it'll be too inefficient for you.

**Tasks**
In `Main.java` file, the four tasks are summarized and which files need to be implemented.
In each of the files mentioned, you will find more detailed instructions (in JavaDoc style).
It's recommended that you view those instructions as JavaDoc (your IDE supports that) instead of the text as-is due to formatting.


### Final Remarks
 - **Code Style** I will take a look at 1-2 commits of yours (probably the final commit), and try to leave useful comments. For this first project, don't worry too much about your code style; rather, focus on getting things right (correctness-wise), and then improve your code later.
 - Grading system will begin grading commits starting on Monday (Feb 17) or sooner.
 - When it begins grading, the status dashboard will become available and it will show how many unit tests (of each type) you passed.

## Commits & Testing
 - **Make incremental changes and commit your changes often**.
 - Grading system status: `https://www.cs.usfca.edu/~hlee84/cs686/proj2-status.html` (if the page is blank or not accessible, it means the grading system has not started grading yet OR it's gone down...)
 - URL for accessing your reports: You'll be able to download the zip files from the status dashboard above (it requires you login using Dons email account). Only you and the teaching staff can see your reports.
 - If your project does not build (due to compilation errors), then it cannot be graded; don't even push to remote repo. It is your responsibility to run unit tests locally to make sure that all of the provided tests (sample tests) pass.
 - If your commit is stuck at `pending` status for a while, then it probably means something went wrong and it crashed or never terminated. If such is the case, make another push to see if the issue persists. If so, email the instructor (include your repo and sha).


## Scoring (new rules for projects)
 - Number of sample/shareable/hidden tests (tentative): 15 / 17 / 8 (see https://github.com/cs-rocks/cs686-lectures/blob/master/projects/project2-README.md) **NOTE** Although it's unlikely that I'll change the number of test cases, it is still a possibility, and I'll try my best to keep you all posted in time. --> Guess what, one more shareable test.
 - If your submission passes ALL of the sample tests (15 of them): your overall score will be: **70% shareable tests + 30% hidden tests** (this weight may vary from project to project).
 - If your submission only passes `x` sample tests out of 15, then your overall score will be: **`(x/15)*(x/15)*(70% shareable tests + 30% hidden tests)`** Notice that your score is penalized by a factor of `((x/15)*(x/15))`. For instance, if x=12, then you're receiving ~64% of the credit.
 - The starter code (as-is) would give you 43 points (out of 10,000) with 2/15, 6/17, and 0/8 being the number of tests it passes. (before the new shareable test was added, the default score was 46 points.)
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
