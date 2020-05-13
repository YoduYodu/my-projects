
### Errata / Corrections
 - refer to this file: https://github.com/cs-rocks/cs686-lectures/blob/master/labs/Lab08-README.md
 - (v0) To be released on Mar 17 (Tuesday) afternoon. 
 
## Note that this lab will be much easier if you first complete the ungraded quiz/homework in L22 and L23, respectively.

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

This lab focuses on Windowing in Beam SDK through hands-on exercises (it's more of "problem solving" than coding, because it's about understanding the concepts and definitions).


### Main Tasks

**About Proto Files**
 1. Review the `BidLog` message in `bid.proto`. From `BidLog` messages, we'll build `ExchangeProfile` for each exchange.
 2. `ExchangeProfile` message is new in `profile.proto`.
    1. The first field (`exchange`) is `Exchange` enum.
    2. The second field (`count_per_bid_request`) is a map from `BidResult` enum (the enum's integer value) to the number of such cases.
    3. The third field (`total_bid_price_per_bid_request`) is a map from `BidResult` enum (the enum's integer value) to the total of bid prices (technically, if you remember from earlier assignments, bid price should be `0` if `BidResult` is NOT `BID`. Forgive me for this inconsistency, but I wanted to have a map instead of just an `int64` in this assignment).
  

### Task A: Implement `ExchangeProfileJob` class.
 - Given what we have done in the past, this should be straightforward.
 - `ApplyReceivedAtTimestamp` is already provided (but commented).
 - `ConvertBidLogToProfile` is also provided. Notice that we're only using a few field from `BidLog` when we are building a new `ExchangeProfile` message (why am I emphasizing this? Because this'll be useful in one of the future labs, ha!).
 - Finish implementing `BidLogsToExchangeProfiles` PTransform. It's got "suggested code" in it, which you can ignore if you want to. If you want to utilize it, finish implementing `Combiner` as well.


### Task B: Finish `BidLogData` class.
 - Reverse-engineer the unit tests, and finish `getDataB01` method. 
 - Reverse-engineer the unit tests, and finish `getDataB02` method.
 - Reviewing L22 and L23 (lectures) will help you; especially the "ungraded homework" on the last slide of L23.


### Final Remarks
 - This lab is supposed to be rather easy, for obvious reasons.
 - **READ** the comments/instructions in each java file and **CHECK** unit tests **BEFORE** you start coding; unit tests are intended as sample inputs and outputs, and by working on those examples by hand, you will be able to understand the requirements better and quicker.

## Commits & Testing
 - **Make incremental changes and commit your changes often**.
 - Grading system status: `https://www.cs.usfca.edu/~hlee84/cs686/lab08-status.html` (if the page is blank or not accessible, it means the grading system has not started grading yet; grading usually begins within a day after the assignment is released.)
 - URL for accessing your reports: You'll be able to download the zip files from the status dashboard above (it requires you login using Dons email account). Only you and the teaching staff can see your reports.
 - If your project does not build (due to compilation errors), then it cannot be graded; don't even push to remote repo. It is your responsibility to run unit tests locally to make sure that all of the provided tests (sample tests) pass.
 - If your commit is stuck at `pending` status for a while, then it probably means something went wrong and it crashed or never terminated. If such is the case, make another push to see if the issue persists. If so, email the instructor (include your repo and sha).
 - **IMPORTANT** Your score may be reduced later if your code is found to violate the requirements. This will be manually checked.

## Starvation of Grading System
 - This lab's unit tests will take longer than previous labs' unit tests.
 - As such, if many of you push commits around the same time, you may experience an extended delay before your commit gets graded (it could be up to several minutes).
 - Since the lab is available for a week, and we have covered all of the topics before the lab was released, it is your responsibility to get it **done and graded** before the deadline.

## Scoring (read carefully)
 - Number of sample/shareable/hidden tests: 7 / 3 / 1
 - Your overall score will be **90% shareable tests + 10% hidden tests** (this weight may vary from lab to lab), penalized by the number of failed sample tests.
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
