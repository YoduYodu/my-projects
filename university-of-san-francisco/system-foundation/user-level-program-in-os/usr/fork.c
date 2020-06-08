#include "types.h"
#include "stat.h"
#include "user.h"

int
main(void)
{
    int id;
    int x = 3;

    printf(1, "Parent: x = %d\n", x);

    id = fork();

    if (id == 0) {
        /* we are in the child */
        printf(1, "Child: id = %d\n", id);
        printf(1, "Child PID = %d\n", getpid());
        printf(1, "Child: x = %d\n", x);
        x = 99;
        printf(1, "Child: x = %d\n", x);
        printf(1, "Child: &x = %x\n", &x);
        exit();
    } else {
        /* we are in the parent */
        printf(1, "Parent PID = %d\n", getpid());
        printf(1, "Parent: id = %d\n", id);
        printf(1, "Parent: wait for child\n");
        id = wait();
        printf(1, "Parent: child terminated pid = %d\n", id);
        printf(1, "Parent: x = %d\n", x);
        printf(1, "Parent: &x = %x\n", &x);
    }

    exit();
}
