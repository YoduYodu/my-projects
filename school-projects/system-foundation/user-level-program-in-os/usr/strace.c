#include "types.h"
#include "stat.h"
#include "user.h"

int main(int argc, char **argv)
{
    char *params[4];
    int id;
    
    params[0] = argv[1];
    params[1] = 0;
    id = fork();
    
    if (id == 0) {
      traceon();
      //child
      exec(params[0], params);
      exit();
    } else {
      //parent
      id = wait();
    }
    exit();
}

