#include "types.h"
#include "stat.h"
#include "user.h"

int
main(int argc, char *argv[])
{
    printf(1, "Free memory pages in kernel: %d\n", memfree());
    exit();
}
