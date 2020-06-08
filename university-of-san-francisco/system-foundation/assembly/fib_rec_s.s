.global fib_rec_s

/*
    int n   => r0
*/ 

.func fib_rec_s
fib_rec_s:
    cmp r0, #1
    ble return_n

    sub sp, sp, #16
    str lr, [sp]
    str r0, [sp, #4]

    sub r0, r0, #1
    bl fib_rec_s
    str r0, [sp, #8]

    ldr r0, [sp, #4]
    sub r0, r0, #2
    bl fib_rec_s
    
    ldr r1, [sp, #8]
    add r0, r0, r1

    ldr lr, [sp]
    add sp, sp, #16
return_n:
    bx lr
.endfunc
