#include "types.h"
#include "stat.h"
#include "user.h"

int
main(int argc, char *argv[])
{
    //int id;

    //id = fork();

    //if (id == 0) {
        /* we are in the child */
        resume(argv[1]);
        //printf(1, "Child: WE DON'T SEE THIS\n");
        exit();
    //} else {
        /* we are in the parent */
    //    id = wait();
    //    printf(1, "Parent: child terminated\n");
    //}

    //exit();
}
