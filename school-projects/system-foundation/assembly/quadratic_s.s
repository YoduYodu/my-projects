.global quadratic_s
.func quadratic_s

/*
    int x    => r0
    int a    => r1
    int b    => r2
    int c    => r3
*/ 

quadratic_s:
    mul r2, r0, r2
    mov r12, r0
    mul r0, r12, r0
    mul r0, r1, r0
    add r0, r2, r0
    add r0, r3, r0
    bx lr
.endfunc
