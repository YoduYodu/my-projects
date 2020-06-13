.global sum_array_s
.func sum_array_s
/*
    int *array   => r0
    int n        => r1
    int i        => r2
    int sum      => r3
*/ 

sum_array_s:
    mov r2, #0
    mov r3, #0
loop:
    cmp r2, r1
    beq return_value
    ldr r12, [r0]
    add r3, r3, r12
    add r2, r2, #1
    add r0, r0, #4
    b loop
return_value:
    mov r0, r3
    bx lr
.endfunc
