main:
  mov r0, #19
  bl fib_iter_s

end:
  b end
/*
    int n     => r0
    int i     => r1
    int prev  => r2
    int curr  => r3
*/


fib_iter_s:
    cmp r0, #0
    beq set_result_zero

    mov r1, #1
    mov r2, #0
    mov r3, #1

loop_fib_iter_s:
    cmp r1, r0
    beq return_fib_iter

    add r12, r2, r3
    mov r2, r3
    mov r3, r12

    add r1, r1, #1
    b loop_fib_iter_s

set_result_zero:
    mov r3, #0

return_fib_iter:
    mov r0, r3
    bx lr
