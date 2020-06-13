#include "types.h"
#include "stat.h"
#include "user.h"


int
main(int argc, char **argv) 
{
    int id = fork();
    if(id == 0) {	
        suspend(atoi(argv[1]), argv[2]);
	exit();
    } else {
        id = wait();
    }

    exit();
}

