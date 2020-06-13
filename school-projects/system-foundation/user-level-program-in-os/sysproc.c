#include "types.h"
#include "arm.h"
#include "defs.h"
#include "param.h"
#include "memlayout.h"
#include "mmu.h"
#include "proc.h"

int sys_fork(void)
{
    if(proc->tracing) {
        cprintf("[%d] sys_fork()\n", proc->pid);
    }
    return fork();
}

int sys_exit(void)
{
    if(proc->tracing == 1) {
        cprintf("[%d] sys_exit()\n", proc->pid);
    }
    exit();
    return 0;  // not reached
}

int sys_wait(void)
{
    if(proc->tracing == 1) {
        cprintf("[%d] sys_wait()\n", proc->pid);
    }
    return wait();
}

int sys_kill(void)
{
    int pid;

    int rv = argint(0, &pid);

    if(proc->tracing == 1) {
        cprintf("[%d] sys_kill(%d)\n", proc->pid, pid);
    }

    if(rv < 0) {
        return -1;
    }

    return kill(pid);
}

int sys_getpid(void)
{
    if(proc->tracing == 1) {
        cprintf("[%d] sys_getpid()\n", proc->pid);
    }
    return proc->pid;
}

int sys_sbrk(void)
{
    int addr;
    int n;

    int rv = argint(0, &n);

    if(proc->tracing == 1) {
        cprintf("[%d] sys_sbrkt(%d)\n", proc->pid, n);
    }

    if(rv < 0) {
        return -1;
    }

    addr = proc->sz;

    if(growproc(n) < 0) {
        return -1;
    }

    return addr;
}

int sys_sleep(void)
{
    int n;
    uint ticks0;

    int rv = argint(0, &n);

    if(proc->tracing == 1) {
        cprintf("[%d] sys_sleep(%d)\n", proc->pid, n);
    }

    if(rv < 0) {
        return -1;
    }

    acquire(&tickslock);

    ticks0 = ticks;

    while(ticks - ticks0 < n){
        if(proc->killed){
            release(&tickslock);
            return -1;
        }

        sleep(&ticks, &tickslock);
    }

    release(&tickslock);
    return 0;
}

// return how many clock tick interrupts have occurred
// since start.
int sys_uptime(void)
{
    uint xticks;

    if(proc->tracing == 1){
        cprintf("[%d] sys_uptime()\n", proc->pid);
    }

    acquire(&tickslock);
    xticks = ticks;
    release(&tickslock);

    return xticks;
}

int sys_memfree(void)
{
  if(proc->tracing == 1){
        cprintf("[%d] sys_memfree()\n", proc->pid);
  }
  return kmemfree();
}

void sys_traceon(void)
{
    proc->tracing = 1;
}

int sys_suspend(void)
{
  int pid;
  char *file;
  argint(0, &pid);
  argstr(1, &file);
  suspend(pid, file);
  return 0;
}

int sys_resume(void)
{
    char *path;
    argstr(0, &path);
    return resume(path);
}
