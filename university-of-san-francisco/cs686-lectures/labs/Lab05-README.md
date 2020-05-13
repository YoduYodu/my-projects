# CS686 Lab 05

### Errata / Corrections
 - refer to this file: https://github.com/cs-rocks/cs686-lectures/blob/master/labs/Lab05-README.md
 - (v1) Released around 9pm on Tuesday, Feb 18.
 - (v2) In `LogParser.java` comments, there's a typo: Line 65, `"IPA"` should be `"IAP"` (I guess I was thirsty when I typed that). In `__TestLogParser.java`, in line 35, `SAMPLE1`'s timestamp should be `2020-02-14T02:23:12.983002Z` but I typed `2020-02-14T02:23:72.983002Z` (72 seconds?!). Please fix these two before you start working on your lab (if you accepted the lab after I fixed the typos, then you don't have to fix anything).

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

In this lab, you will focus on parsing JSON logs (some of which may be uninteresting or corrupted for our job), and producing relevant output as proto messages.
In addition, one of the values in JSON logs is actually an HTTP URL, which also needs to be parsed (so we can extract useful information).

To give you some context why this type of data processing pipelines may be necessary:
 - Often, server-to-server integration involves http requests between two endpoints. 
 - In our hypothetical scenario, one endpoint is our own "event handling servers" (which may have load balancers that are responsible for routing incoming traffic evenly across our servers).
 - The JSON logs resemble **actual** logs from GCP Load Balancers (but simplified, for the sake of brevity).
 - Although our event handling servers may handle certain requests in real-time, we may also need to run batch processing jobs for more thorough analysis, etc.
 - In project 3, we will expand this to develop a more complex data processing pipeline, but for now let's focus on "data parsing and cleaning" in this lab.

**About Proto Files**
 1. Not much to do. Feel free to take a look at proto files, but you don't have to change anything.
  

### Task A: Implement `LogParser` class.
Imagine that load balancer logs are line-delimited text files where each line contains a JSON message (one sample is copied below for reference).
```
{
   "httpRequest":{
      "referer":"https://usfca.edu/",
      "remoteIp":"23.185.0.4",
      "requestMethod":"POST",
      "requestUrl":"https://cs.usfca.edu/event?source=dummy_mmp&event_id=123-abcd-efg&store=appstore&event_type=purchase&app=com.android.some_app&my_callback_data=abcd&amount=123",
      "status":200,
      "userAgent":"Mozilla/5.0 ..."
   },
   "insertId":"some_logging_id_1234",
   "jsonPayload":{
      "@type":"type.googleapis.com/google.cloud.loadbalancing.type.LoadBalancerLogEntry",
      "statusDetails":"response_sent_by_backend"
   },
   "resource":{
      "labels":{
         "backend_service_name":"event-backend-server",
         "forwarding_rule_name":"event-server-prod",
         "project_id":"some-gcp-project",
         "target_proxy_name":"event-server-proxy",
         "url_map_name":"event-server-prod-map",
         "zone":"global"
      },
      "type":"http_load_balancer"
   },
   "severity":"INFO",
   "spanId":"some-id",
   "timestamp":"2020-02-15T00:49:36.389054Z",
   "trace":"projects/some-gcp-project/traces/some-trace-id"
}
```
Implementation details are provided in the java file, but the JSON message above will be useful for you to refer to.

### Task B: Finish `GetPurchaseEvents` PTransform.



### Final Remarks
 - This lab is supposed to be rather easy because there's project 02 deadlinen in the same week.
 - Note that what you work on in this lab will be used in project 03, so try to write reusable code.
 - Lab 05 has no short-answer questions.
 - **READ** the comments/instructions in each java file and **CHECK** unit tests **BEFORE** you start coding; unit tests are intended as sample inputs and outputs, and by working on those examples by hand, you will be able to understand the requirements better and quicker.

## Commits & Testing
 - **Make incremental changes and commit your changes often**.
 - Grading system status: `https://www.cs.usfca.edu/~hlee84/cs686/lab05-status.html` (if the page is blank or not accessible, it means the grading system has not started grading yet.)
 - URL for accessing your reports: You'll be able to download the zip files from the status dashboard above (it requires you login using Dons email account). Only you and the teaching staff can see your reports.
 - If your project does not build (due to compilation errors), then it cannot be graded; don't even push to remote repo. It is your responsibility to run unit tests locally to make sure that all of the provided tests (sample tests) pass.
 - If your commit is stuck at `pending` status for a while, then it probably means something went wrong and it crashed or never terminated. If such is the case, make another push to see if the issue persists. If so, email the instructor (include your repo and sha).
 - **IMPORTANT** Your score may be reduced later if your code is found to violate the requirements. This will be manually checked.

## Starvation of Grading System
 - This lab's unit tests will take longer than previous labs' unit tests.
 - As such, if many of you push commits around the same time, you may experience an extended delay before your commit gets graded (it could be up to several minutes).
 - Since the lab is available for a week, and we have covered all of the topics before the lab was released, it is your responsibility to get it **done and graded** before the deadline.

## Scoring (read carefully)
 - Number of sample/shareable/hidden tests: 4 / 8 / 6
 - If your submission fails any one of the sample tests: Your score will be 0 (This applies to all labs/projects).
 - Otherwise, your overall score will be **60% shareable tests + 40% hidden tests** (this weight may vary from lab to lab).
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
