#ifndef INCLUDE_PINFO_H
#define INCLUDE_PINFO_H

struct pdata { 
    int pid;
    int mem;
    char name[16];
};

struct pinfo {
    struct pdata pd[64];
    int pcount;
};

#endif
