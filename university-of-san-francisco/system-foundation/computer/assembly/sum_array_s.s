main:
  mov sp, #128
  mov r0, #32
  mul sp, r0, sp
  sub sp, sp, #24
  mov r0, #1
  str r0, [sp]
  mov r0, #2
  str r0, [sp, #4]
  mov r0, #3
  str r0, [sp, #8]
  mov r0, #4
  str r0, [sp, #12]
  mov r0, #6
  str r0, [sp, #16]
  mov r0, sp
  mov r1, #5
  bl sum_array_s

end:
  b end
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
