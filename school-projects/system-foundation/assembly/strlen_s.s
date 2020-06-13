.global strlen_s
.func strlen_s


/*
    int *array     => r0
    int i          => r1
*/ 

strlen_s:
    mov r1, #0
strlen_loop:
    ldrb r2, [r0]
    cmp r2, #0
    beq strlen_end

    add r1, r1, #1
    add r0, r0, #1
    b strlen_loop
strlen_end:
    mov r0, r1
    bx lr
.endfunc
