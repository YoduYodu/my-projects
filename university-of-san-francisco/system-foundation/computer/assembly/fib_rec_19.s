main:
  mov sp, #128
  mov r0, #32
  mul sp, r0, sp
  mov r0, #19
  bl fib_rec_s
end:
  b end

/*
    int n   => r0
*/

fib_rec_s:
    mov r3, #1
    cmp r3, r0
    bge return_n

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
