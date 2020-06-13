#include "types.h"
#include "stat.h"
#include "user.h"

int
main(void)
{
    int id;
    char *argv[4];
    argv[0] = "hello_xv6";
    argv[1] = 0;

    id = fork();

    if (id == 0) {
        /* we are in the child */
        exec(argv[0], argv);
        printf(1, "Child: WE DON'T SEE THIS\n");
        exit();
    } else {
        /* we are in the parent */
        id = wait();
        printf(1, "Parent: child terminated\n");
    }

    exit();
}

