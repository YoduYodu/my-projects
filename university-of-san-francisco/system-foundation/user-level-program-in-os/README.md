# Process Tools for xv6 in Qemu
Mulitple user-level programs and kernel supports to trace the systems calls made by a process.
### A System Call Tracer
You are going to write a user-level program and kernel support to trace the systems calls made by a process. For example, if you have helloworld:

$ strace helloworld
[4] sys_write(1, "Hello, World!\n", 13)
Hello, World!
[4] sys_exit()
$

The number in the brackets is the PID of the process you are tracing. You need to show each system call invoked with the arguments passed in.

To implement strace, you will need a way to turn tracing on for a process, then turn it off when the process exits. You can do this after you call fork() to create the process to run the target program and before you call exec. You need to add a new system call, sys_traceon(), that will turn on tracing for the current process.

In the kernel, you need to figure out how system calls work and where to add code to show the trace information.

### ps for xv6
For this part you will write version of ps (process status) for xv6. For each process your ps command should show the process PID, memory used, and command name. Something like this:

$ ps
PID      MEM      NAME
1          12K        init
2          12K        sh
$

You need to a way to transfer the the process information from the kernel to the user-level ps program for display. That is, you cannot print the ps information from the kernel (this is not how real ps programs work).

Also, you should come up with a way to write one or more test programs that take up more memory to make your ps output more interesting.
### Process Hibernation
The goal of this feature is to provide a command-line program that can suspend a running process and write it's state to disk. Then at a later time you can resume execution of the hibernated process. For example:

$ suspend 11 foo.proc

This will suspend the process with PID 11 and write it to disk as a file called foo.proc.

Then you can resume foo.proc:

$ resume foo.proc

When you resume a hibernated process it should resume from where it left off just before the suspend, however it may have a new PID number.

Write a test program that shows how you can stop a computation, save its state, and resume it. Something like recursive fibonacci would be interesting to save and resume.

