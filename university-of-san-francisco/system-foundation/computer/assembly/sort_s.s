main:
  mov sp, #128
  mov r0, #32
  mul sp, r0, sp
  sub sp, sp, #24
  mov r0, #1
  str r0, [sp]
  mov r0, #4
  str r0, [sp, #4]
  mov r0, #7
  str r0, [sp, #8]
  mov r0, #9
  str r0, [sp, #12]
  mov r0, #8
  str r0, [sp, #16]
  mov r0, #2
  str r0, [sp, #20]
  mov r0, sp
  mov r1, #6
  bl sort_s

end:
  b end

/*
    int *array     => r0
    int n          => r1
    int i          => r2
    int temp_n     => r3
*/

sort_s:
    mov r2, #0
    sub sp, sp, #24
    str lr, [sp]
    str r1, [sp, #4]
    mov r3, r1

sort_loop:
    cmp r1, r2
    beq return_sort

    str r0, [sp, #8]
    mov r1, r3
    str r2, [sp, #16]
    str r3, [sp, #12]

    bl find_max_index_s

    /* Exchange */
    mov r12, #4
    mul r0, r12, r0
    mov r12, r0
    ldr r0, [sp, #8]
    add r12, r0, r12

    /* r1, r2 for temp exchange */
    ldr r1, [r0]
    ldr r2, [r12]
    str r1, [r12]
    str r2, [r0]

    /* Clean up */
    add r0, r0, #4

    ldr r1, [sp, #4]

    ldr r2, [sp, #16]
    add r2, r2, #1

    ldr r3, [sp, #12]
    sub r3, r3, #1
    b sort_loop

return_sort:
    ldr lr, [sp]
    ldr r0, [sp, #4]
    add sp, sp, #24
    bx lr


/*
    int *array    => r0
    int n         => r1
    int max_value => r2
    int i         => r3
*/

find_max_index_s:
    sub sp, sp, #4
    mov r3, #0
    str r3, [sp]
    ldr r2, [r0]
loop:
    add r0, r0, #4
    add r3, r3, #1

    cmp r3, r1
    beq return_value

    ldr r12, [r0]
    cmp r2, r12
    bge loop

    mov r2, r12
    str r3, [sp]
    b loop
return_value:
    ldr r0, [sp]
    add sp, sp, #4
    bx lr
