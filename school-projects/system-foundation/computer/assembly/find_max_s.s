main:
  mov sp, #128
  mov r0, #32
  mul sp, r0, sp
  sub sp, sp, #24
  mov r0, #3
  str r0, [sp]
  mov r0, #6
  str r0, [sp, #4]
  mov r0, #1
  str r0, [sp, #8]
  mov r0, #8
  str r0, [sp, #12]
  mov r0, #4
  str r0, [sp, #16]
  mov r0, sp
  mov r1, #5
  bl find_max_s

end:
  b end

/*
    int *array   => r0
    int n        => r1
    int max      => r2
    int i        => r3
*/

find_max_s:
    mov r3, #0
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
    b loop
return_value:
    mov r0, r2
    bx lr
