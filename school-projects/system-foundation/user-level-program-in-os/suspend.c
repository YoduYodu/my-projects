#include "types.h"
#include "param.h"
#include "defs.h"
#include "memlayout.h"
#include "mmu.h"
#include "proc.h"
#include "elf.h"
#include "arm.h"
#include "fcntl.h"
#include "fs.h"
#include "spinlock.h"
#include "file.h"
#include "theader.h"

static struct inode* create(char *path, short type, short major, short minor)
{
    uint off;
    struct inode *ip, *dp;
    char name[DIRSIZ];

    if((dp = nameiparent(path, name)) == 0) {
        return 0;
    }

    ilock(dp);

    if((ip = dirlookup(dp, name, &off)) != 0){
        iunlockput(dp);
        ilock(ip);

        if(type == T_FILE && ip->type == T_FILE) {
            return ip;
        }

        iunlockput(ip);

        return 0;
    }

    if((ip = ialloc(dp->dev, type)) == 0) {
        panic("create: ialloc");
    }

    ilock(ip);
    ip->major = major;
    ip->minor = minor;
    ip->nlink = 1;
    iupdate(ip);

    if(type == T_DIR){ 
        dp->nlink++;  
        iupdate(dp);

        if(dirlink(ip, ".", ip->inum) < 0 || dirlink(ip, "..", dp->inum) < 0) {
            panic("create dots");
        }
    }

    if(dirlink(dp, name, ip->inum) < 0) {
        panic("create: dirlink");
    }

    iunlockput(dp);

    return ip;
}

int suspend(int pid, char *path)
{
    struct proc *p = get_proc(pid);
    struct theader hdr;
    int omode;
    struct file *f;
    struct inode *ip;

    argstr(1, &path);
    omode = O_CREATE;

    if(omode & O_CREATE){
        begin_trans();
        ip = create(path, T_FILE, 0, 0);
        commit_trans();

        if(ip == 0) {
            return -1;
        }

    }

    if((f = filealloc()) == 0){
        if(f) {
            fileclose(f);
        }

        iunlockput(ip);
        return -1;
    }

    iunlock(ip);

    f->type = FD_INODE;
    f->ip = ip;
    f->off = 0;
    f->readable = 0;
    f->writable = 1;

    hdr.sz = p->sz;
    hdr.sp_usr = p->tf->sp_usr;
    hdr.lr_usr = p->tf->lr_usr;
    hdr.pc = p->tf->pc;

    hdr.r14_svc = p->tf->r14_svc;
    hdr.spsr = p->tf->spsr;
    hdr.r0 = p->tf->r0;
    hdr.r1 = p->tf->r1;
    hdr.r2 = p->tf->r2;
    hdr.r3 = p->tf->r3;
    hdr.r4 = p->tf->r4;
    hdr.r5 = p->tf->r5;
    hdr.r6 = p->tf->r6;
    hdr.r7 = p->tf->r7;
    hdr.r8 = p->tf->r8;
    hdr.r9 = p->tf->r9;
    hdr.r10 = p->tf->r10;
    hdr.r11 = p->tf->r11;
    hdr.r12 = p->tf->r12;

    hdr.tracing = p->tracing;
    hdr.code_data_size = p->sz - 8192;
    strncpy(hdr.name, p->name, 16);

    switchuvm(p);
    filewrite(f, (char *) &hdr, sizeof(hdr));
    filewrite(f, (char *) 0, hdr.code_data_size);
    filewrite(f, (char *) (hdr.sz - 4096), 4096);
    switchuvm(proc);
    fileclose(f);
    kill(pid);

    return 0;
}

int resume(char *path)
{
    struct theader hdr;
    struct inode *ip;
    pde_t *pgdir, *oldpgdir;
    uint sz;

    ip = namei(path);

    ilock(ip);
    readi(ip, (char*) &hdr, 0, sizeof(hdr)); 

    pgdir = 0;
    pgdir = kpt_alloc();

    sz = 0;
    sz = allocuvm(pgdir, sz, hdr.code_data_size);

    loaduvm(pgdir, (char*) 0, ip, sizeof(hdr), hdr.code_data_size);

    sz = align_up(sz, 4096);
    sz = allocuvm(pgdir, sz, sz + 2 * 4096); 

    clearpteu(pgdir, (char*) (sz - 2 * 4096));
    loaduvm(pgdir, (char*) (sz - 4096), ip, sizeof(hdr) + hdr.code_data_size, 4096);

    oldpgdir = proc->pgdir;

    proc->pgdir = pgdir;
    proc->sz = hdr.sz;
    proc->tf->sp_usr = hdr.sp_usr;
    proc->tf->lr_usr = hdr.lr_usr;
    proc->tf->pc = hdr.pc;
    proc->tracing = hdr.tracing;

    proc->tf->r14_svc = hdr.r14_svc;
    proc->tf->spsr = hdr.spsr;
    proc->tf->r0 = hdr.r0;
    proc->tf->r1 = hdr.r1;
    proc->tf->r2 = hdr.r2;
    proc->tf->r3 = hdr.r3;
    proc->tf->r4 = hdr.r4;
    proc->tf->r5 = hdr.r5;
    proc->tf->r6 = hdr.r6;
    proc->tf->r7 = hdr.r7;
    proc->tf->r8 = hdr.r8;
    proc->tf->r9 = hdr.r9;
    proc->tf->r10 = hdr.r10;
    proc->tf->r11 = hdr.r11;
    proc->tf->r12 = hdr.r12;
    strncpy(proc->name, hdr.name, 16);

    switchuvm(proc);
    freevm(oldpgdir);

    if (ip) {
        iunlockput(ip);
    }

    return 0;
}
