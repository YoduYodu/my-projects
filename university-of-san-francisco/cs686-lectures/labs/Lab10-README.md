
### Errata / Corrections
 - (v0) To be released at 4pm on Canvas (Apr 15).

### NOTICE (specific to this lab)

- This lab only has short-answer questions.
- For each Java class, you will need to change ONE line (only one line) to fix a bug or optimize the code. 
- Then, you will copy and paste that line you changed (which is your "answer") to appropriate methods in Main class.

### Objectives
 - When you become a swe, you'll be reading someone else's code (or your own code) more than you'll be writing new code.
 - The code you'll be reading... will not always be clean / correct / reasonable.
 - In this lab, I'm giving you some sample code that's slightly incorrect/inefficient, and I am asking you to change just one line (in each case) to fix the issue.
 - One of the four exercises in this lab will be pretty useful when you try to optimize your pipeline(s) in Project 5. 
 - After the deadline, in Lecture 39, I will go over some of the most important topics/concepts that werer covered by all 10 labs.

### Short Answer Questions 

 - Note: All answers (excluding whitespaces) are less than 30 chars. If your answer is longer than 30 chars, it won't be accepted. If there are many correct answers, try to find short ones.
 - See the `getShortAnswer1` method in `Main` class. 
   - You will need to fix a bug in `JsonParsers` class by changing one line.
   - This is relevant to serializability of DoFn instances.
 - See the `getShortAnswer2` method in `Main` class.
   - You will need to fix a bug in `Combiners` class by changing one line.
   - This is relevant to using Combiners in Beam SDK, and spotting a silly bug.
 - See the `getShortAnswer3` method in `Main` class.
   - You will need to fix a bug in `DoFnWithPCV` class by changing one line.
   - Technically, it's not really a bug (in terms of correctness), but rather an inefficiency.
   - This is relevant to general optimizations and subtle "implementation details" of Beam SDK.
 - See the `getShortAnswer4` method in `Main` class.
   - You will need to fix a bug in `CoGbkViaCombine` class by changing one line (for `mergePCs2` method).
   - This asks you to understand how we can convert `String` and `Integer` into `Dummy` protos (see `profile.proto`), and use `Combine.perKey` to achieve what we would be able to do using `CoGroupByKey`. 
   - This example seems a bit contrived, but this could be useful in cases where `Combine.perKey` boosts performances, compared to `CoGroupByKey` (which is essentially `GroupByKey`). 
   - To fix the bug, you will need to first identify the bug, and then figure out how you can "fix" it by using one of the available methods from proto builder objects.
   - (Not sure where to begin? This [link](https://developers.google.com/protocol-buffers/docs/javatutorial#standard-message-methods) may help you, but try to find the answer yourself first! To be fair, this was briefly mentioned in class a while ago.)
   - (Note that this last exercise is a bit "contrived" as I had to simplify a few things, but I wanted to illustrate how one can circumvent CoGBK by using Combine.perKey, at least in theory.)

## Commits & Testing
 - **Make incremental changes and commit your changes often**.
 - Grading system status: `https://www.cs.usfca.edu/~hlee84/cs686/lab10-status.html` (if the page is blank or not accessible, it means the grading system has not started grading yet; grading usually begins within a day after the assignment is released.)
 - URL for accessing your reports: You'll be able to download the zip files from the status dashboard above (it requires you login using Dons email account). Only you and the teaching staff can see your reports.
 - If your project does not build (due to compilation errors), then it cannot be graded; don't even push to remote repo. It is your responsibility to run unit tests locally to make sure that all of the provided tests (sample tests) pass.
 - If your commit is stuck at `pending` status for a while, then it probably means something went wrong and it crashed or never terminated. If such is the case, make another push to see if the issue persists. If so, email the instructor (include your repo and sha).
 - **IMPORTANT** Your score may be reduced later if your code is found to violate the requirements. This will be manually checked.

## Starvation of Grading System
 - This lab's unit tests will take longer than previous labs' unit tests.
 - As such, if many of you push commits around the same time, you may experience an extended delay before your commit gets graded (it could be up to several minutes).
 - Since the lab is available for a week, and we have covered all of the topics before the lab was released, it is your responsibility to get it **done and graded** before the deadline.

## Scoring (read carefully)
 - Number of sample/shareable/hidden tests: 1 / 1 / 5
 - Your overall score will be **0.05% shareable tests + 99.95% hidden tests** (this weight may vary from lab to lab), penalized by the number of failed sample tests.
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
