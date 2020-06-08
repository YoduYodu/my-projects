PROGS = project03_main
OBJS = quadratic_c.o quadratic_s.o sum_array_c.o sum_array_s.o find_max_c.o find_max_s.o sort_c.o sort_s.o fib_iter_c.o fib_iter_s.o fib_rec_c.o fib_rec_s.o strlen_c.o strlen_s.o

CFLGAS = -g

%.o : %.c
	gcc ${CFLAGS} -c -o $@ $<

%.o : %.s
	as -o $@ $<

all : ${PROGS}

project03_main : project03_main.c quadratic_c.o quadratic_s.o sum_array_c.o sum_array_s.o find_max_c.o find_max_s.o sort_c.o sort_s.o fib_iter_c.o fib_iter_s.o fib_rec_c.o fib_rec_s.o strlen_c.o strlen_s.o
	gcc ${CFLAGS} -o $@ $^

clean :
	rm -rf ${PROGS} ${OBJS} *~
