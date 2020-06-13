#include "types.h"
#include "stat.h"
#include "user.h"
#include "pinfo.h"

int main(void)
{
    struct pinfo ptable;

    getps(&ptable);
    int i;
    printf(1, "PID\tMEM\tNAME\n");
    for (i = 0; i < ptable.pcount; i++) {
    	printf(1, "%d\t", ptable.pd[i].pid);
    	printf(1, "%d\t", ptable.pd[i].mem);
    	printf(1, "%s\n", ptable.pd[i].name);
    }
    exit();
}

