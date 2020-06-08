PROGS = main
OBJS = fib_rec_c.o fib_rec_s.o quadratic_c.o quadratic_s.o\
sum_array_c.o sum_array_s.o find_max_s.o find_max_c.o\
fib_iter_c.o fib_iter_s.o sort_c.o sort_s.o strlen_c.o strlen_s.o\


CFLAGS = -g

# Auxiliary
%.o : %.c
	gcc ${CFLAGS} -c -o $@ $<

%.o : %.s
	as -o $@ $<

all : ${PROGS}

main : main.c ${OBJS} 
	gcc ${CFLAGS} -o $@ $^

clean :
	rm -rf ${PROGS} ${OBJS} *~

run :
	./${PROGS}