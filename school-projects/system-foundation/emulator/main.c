#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>

#define NREGS 16
#define STACK_SIZE 1024
#define SP 13
#define LR 14
#define PC 15

/* Assembly functions to emulate */
int fib_rec_c(int n);
int fib_rec_s(int n);
int quadratic_c(int x, int a, int b, int c);
int quadratic_s(int x, int a, int b, int c);
int sum_array_c(int *array, int n);
int sum_array_s(int *array, int n);
int find_max_c(int *array, int len);
int find_max_s(int *array, int len);
int fib_iter_c(int n);
int fib_iter_s(int n);
int sort_c(int *array, int len);
int sort_s(int *array, int len);
int strlen_c(char *s);
int strlen_s(char *s);

/* The complete machine state */
struct dynamic_analysis {
    unsigned int num_inst, dp_inst, memo_inst, b_inst, b_taken, b_not_taken;
};


struct cache_entry {
    unsigned int valid, tag, data;
};

struct cache_analysis {
    unsigned int num_ref, num_hit, num_miss;
};

struct arm_state {
    unsigned int regs[NREGS];
    unsigned int cpsr;
    unsigned char stack[STACK_SIZE];
    
    struct dynamic_analysis analysis;

    struct cache_entry 
        dm8[8][1], dm32[32][1], dm128[128][1], dm1024[1024][1],
        fa8[1][8], fa32[1][32], fa128[1][128], fa1024[1][1024],
        sa8[2][4], sa32[8][4], sa128[32][4], sa1024[256][4];

    struct cache_analysis dm8_a, dm32_a, dm128_a, dm1024_a, 
        fa8_a, fa32_a, fa128_a, fa1024_a,
        sa8_a, sa32_a, sa128_a, sa1024_a;
};

void cache_init(int m, int n, struct cache_entry cache[m][n]) {
    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            cache[i][j].valid = 0;
            cache[i][j].tag = 0;
            cache[i][j].data = 0;
        }
    }
}

void cache_analysis_init(struct cache_analysis *cache_a) {
    cache_a->num_ref = 0;
    cache_a->num_hit = 0;
    cache_a->num_miss = 0;
}


/* Initialize an arm_state struct with a function pointer and arguments */
void arm_state_init(struct arm_state *as, unsigned int *func,
                    unsigned int arg0, unsigned int arg1,
                    unsigned int arg2, unsigned int arg3)
{
    int i;

    /* Zero out all registers */
    for (i = 0; i < NREGS; i++) {
        as->regs[i] = 0;
    }

    /* Zero out CPSR */
    as->cpsr = 0;

    /* Zero out the stack */
    for (i = 0; i < STACK_SIZE; i++) {
        as->stack[i] = 0;
    }

    /* Set the PC to point to the address of the function to emulate */
    as->regs[PC] = (unsigned int) func;

    /* Set the SP to the top of the stack (the stack grows down) */
    as->regs[SP] = (unsigned int) &as->stack[STACK_SIZE];

    /* Initialize LR to 0, this will be used to determine when the function has called bx lr */
    as->regs[LR] = 0;

    /* Initialize the first 4 arguments */
    as->regs[0] = arg0;
    as->regs[1] = arg1;
    as->regs[2] = arg2;
    as->regs[3] = arg3;

    /* Initialize arm analysis */
    as->analysis.num_inst = 0;
    as->analysis.dp_inst = 0;
    as->analysis.memo_inst = 0;
    as->analysis.b_inst = 0;
    as->analysis.b_taken = 0;
    as->analysis.b_not_taken = 0;

    /* Initialize caches and their analysis */
    cache_init(8, 1, as->dm8);
    cache_analysis_init(&(as->dm8_a));
    cache_init(32, 1, as->dm32);
    cache_analysis_init(&(as->dm32_a));
    cache_init(128, 1, as->dm128);
    cache_analysis_init(&(as->dm128_a));
    cache_init(1024, 1, as->dm1024);
    cache_analysis_init(&(as->dm1024_a));
    cache_init(1, 8, as->fa8);
    cache_analysis_init(&(as->fa8_a));
    cache_init(1, 32, as->fa32);
    cache_analysis_init(&(as->fa32_a));
    cache_init(1, 128, as->fa128);
    cache_analysis_init(&(as->fa128_a));
    cache_init(1, 1024, as->fa1024);
    cache_analysis_init(&(as->fa1024_a));
    cache_init(2, 4, as->sa8);
    cache_analysis_init(&(as->sa8_a));
    cache_init(8, 4, as->sa32);
    cache_analysis_init(&(as->sa32_a));
    cache_init(32, 4, as->sa128);
    cache_analysis_init(&(as->sa128_a));
    cache_init(256, 4, as->sa1024);
    cache_analysis_init(&(as->sa1024_a));
}

void arm_analysis_print(struct arm_state *state) {
    printf("\n---- Metrics ----\n");
    printf("total_inst, %d\n", state->analysis.num_inst);
    printf("dp_inst, %d(%.2f%)\n", state->analysis.dp_inst, state->analysis.num_inst != 0 ? (100 * state->analysis.dp_inst / ((double)state->analysis.num_inst)) : 0);
    printf("memo_inst, %d(%.2f%)\n", state->analysis.memo_inst, state->analysis.num_inst != 0 ? (100 * state->analysis.memo_inst / ((double)state->analysis.num_inst)) : 0);
    printf("b_inst, %d(%.2f%)\n", state->analysis.b_inst, state->analysis.num_inst != 0 ? (100 * state->analysis.b_inst / ((double)state->analysis.num_inst)) : 0);
    printf("\n");
    printf("b_taken, %d(%.2f%)\n", state->analysis.b_taken, (100 * state->analysis.b_taken / (double) state->analysis.b_inst));
    printf("b_not_taken, %d(%.2f%)\n", state->analysis.b_not_taken, state->analysis.b_inst != 0 ? (100 * state->analysis.b_not_taken / (double) state->analysis.b_inst) : 0);

    printf("\n---- Cache ----\n");
    printf("dm8 total: %d hit: %d(%.2f%) miss: %d(%.2f%)\n", state->dm8_a.num_ref, state->dm8_a.num_hit, state->dm8_a.num_ref != 0 ? (100 * (double)state->dm8_a.num_hit / state->dm8_a.num_ref) : 0, state->dm8_a.num_miss, state->dm8_a.num_ref != 0 ? (100 * (double)state->dm8_a.num_miss / state->dm8_a.num_ref) : 0);
    printf("dm32 total: %d hit: %d(%.2f%) miss: %d(%.2f%)\n", state->dm32_a.num_ref, state->dm32_a.num_hit, state->dm32_a.num_ref != 0 ? (100 * (double)state->dm32_a.num_hit / state->dm32_a.num_ref) : 0, state->dm32_a.num_miss, state->dm32_a.num_ref != 0 ? (100 * (double)state->dm32_a.num_miss / state->dm32_a.num_ref) : 0);
    printf("dm128 total: %d hit: %d(%.2f%) miss: %d(%.2f%)\n", state->dm128_a.num_ref, state->dm128_a.num_hit, state->dm128_a.num_ref != 0 ? (100 * (double)state->dm128_a.num_hit / state->dm128_a.num_ref) : 0, state->dm128_a.num_miss, state->dm128_a.num_ref != 0 ? (100 * (double)state->dm128_a.num_miss / state->dm128_a.num_ref) : 0);
    printf("dm1024 total: %d hit: %d(%.2f%) miss: %d(%.2f%)\n", state->dm1024_a.num_ref, state->dm1024_a.num_hit, state->dm1024_a.num_ref != 0 ? (100 * (double)state->dm1024_a.num_hit / state->dm1024_a.num_ref) : 0, state->dm1024_a.num_miss, state->dm1024_a.num_ref != 0 ? (100 * (double)state->dm1024_a.num_miss / state->dm1024_a.num_ref) : 0);
    printf("----\n");
    printf("fa8 total: %d hit: %d(%.2f%) miss: %d(%.2f%)\n", state->fa8_a.num_ref, state->fa8_a.num_hit, state->fa8_a.num_ref != 0 ? (100 * (double)state->fa8_a.num_hit / state->fa8_a.num_ref) : 0, state->fa8_a.num_miss, state->fa8_a.num_ref != 0 ? (100 * (double)state->fa8_a.num_miss / state->fa8_a.num_ref) : 0);
    printf("fa32 total: %d hit: %d(%.2f%) miss: %d(%.2f%)\n", state->fa32_a.num_ref, state->fa32_a.num_hit, state->fa32_a.num_ref != 0 ? (100 * (double)state->fa32_a.num_hit / state->fa32_a.num_ref) : 0, state->fa32_a.num_miss, state->fa32_a.num_ref != 0 ? (100 * (double)state->fa32_a.num_miss / state->fa32_a.num_ref) : 0);
    printf("fa128 total: %d hit: %d(%.2f%) miss: %d(%.2f%)\n", state->fa128_a.num_ref, state->fa128_a.num_hit, state->fa128_a.num_ref != 0 ? (100 * (double)state->fa128_a.num_hit / state->fa128_a.num_ref) : 0, state->fa128_a.num_miss, state->fa128_a.num_ref != 0 ? (100 * (double)state->fa128_a.num_miss / state->fa128_a.num_ref) : 0);
    printf("fa1024 total: %d hit: %d(%.2f%) miss: %d(%.2f%)\n", state->fa1024_a.num_ref, state->fa1024_a.num_hit, state->fa1024_a.num_ref != 0 ? (100 * (double)state->fa1024_a.num_hit / state->fa1024_a.num_ref) : 0, state->fa1024_a.num_miss, state->fa1024_a.num_ref != 0 ? (100 * (double)state->fa1024_a.num_miss / state->fa1024_a.num_ref) : 0);
    printf("----\n");
    printf("sa8 total: %d hit: %d(%.2f%) miss: %d(%.2f%)\n", state->sa8_a.num_ref, state->sa8_a.num_hit, state->sa8_a.num_ref != 0 ? (100 * (double)state->sa8_a.num_hit / state->sa8_a.num_ref) : 0, state->sa8_a.num_miss, state->sa8_a.num_ref != 0 ? (100 * (double)state->sa8_a.num_miss / state->sa8_a.num_ref) : 0);
    printf("sa32 total: %d hit: %d(%.2f%) miss: %d(%.2f%)\n", state->sa32_a.num_ref, state->sa32_a.num_hit, state->sa32_a.num_ref != 0 ? (100 * (double)state->sa32_a.num_hit / state->sa32_a.num_ref) : 0, state->sa32_a.num_miss, state->sa32_a.num_ref != 0 ? (100 * (double)state->sa32_a.num_miss / state->sa32_a.num_ref) : 0);
    printf("sa128 total: %d hit: %d(%.2f%) miss: %d(%.2f%)\n", state->sa128_a.num_ref, state->sa128_a.num_hit, state->sa128_a.num_ref != 0 ? (100 * (double)state->sa128_a.num_hit / state->sa128_a.num_ref) : 0, state->sa128_a.num_miss, state->sa128_a.num_ref != 0 ? (100 * (double)state->sa128_a.num_miss / state->sa128_a.num_ref) : 0);
    printf("sa1024 total: %d hit: %d(%.2f%) miss: %d(%.2f%)\n", state->sa1024_a.num_ref, state->sa1024_a.num_hit, state->sa1024_a.num_ref != 0 ? (100 * (double)state->sa1024_a.num_hit / state->sa1024_a.num_ref) : 0, state->sa1024_a.num_miss, state->sa1024_a.num_ref != 0 ? (100 * (double)state->sa1024_a.num_miss / state->sa1024_a.num_ref) : 0);
}

void arm_state_print(struct arm_state *as)
{
    int i;

    for (i = 0; i < NREGS; i++) {
        printf("reg[%d] = %d\n", i, as->regs[i]);
    }
    printf("cpsr = %X\n", as->cpsr);
}

bool is_bx_inst(unsigned int iw)
{
    return (((iw >> 4) & 0x00FFFFFF) == 0b000100101111111111110001);
}

void armemu_bx(struct arm_state *state, unsigned int iw)
{
    state->analysis.b_taken += 1;
    unsigned int rn = iw & 0b1111;
    // printf("bx\n");
    state->regs[PC] = state->regs[rn];
}

void cmp_set_cpsr(struct arm_state *state, unsigned int r1, unsigned int r2) {
    int r1s = (int) r1;
    int r2s = (int) r2;

    // Set N
    if (r1s - r2s < 0) {
        state->cpsr |= 0b10000000000000000000000000000000;
    } else {
        state->cpsr &= 0b01111111111111111111111111111111;
    }
    // Set Z
    if (r1 == r2) {
        state->cpsr |= 0b01000000000000000000000000000000;
    } else {
        state->cpsr &= 0b10111111111111111111111111111111;
    }
    // Set C
    if (r1 < r2) {
        state->cpsr |= 0b00100000000000000000000000000000;
    } else {
        state->cpsr &= 0b11011111111111111111111111111111;
    }
    // Set V
    if (((r1s > 0) && (r2s < 0) && (r1s - r2s <= 0)) || ((r1s < 0) && (r2s > 0) &&(r1s - r2s >= 0))) {
        state->cpsr |= 0b00010000000000000000000000000000;
    } else {
        state->cpsr &= 0b11101111111111111111111111111111;
    }
}

bool is_dp_inst(unsigned int iw) { return (((iw >> 26) & 0b11) == 0b00); }

void armemu_dp(struct arm_state *state, unsigned int iw) {
    unsigned int rd, rn, rm, i, imm;
    rd = (iw >> 12) & 0xF;
    rn = (iw >> 16) & 0xF;
    rm = iw & 0xF;
    i = (iw >> 25) & 0b1;
    imm = iw & 0xFF;
    
    switch ((iw >> 21) & 0b1111) { // opcode
        case 0b1101: // mov
            state->regs[rd] = ((i == 0) ? state->regs[rm] : imm);
            break;
        case 0b0100: // add
            state->regs[rd] = state->regs[rn] + ((i == 0) ? state->regs[rm] : imm);
            break;
        case 0b1010: // cmp
            cmp_set_cpsr(state, state->regs[rn], ((i == 0) ? state->regs[rm] : imm));
            break;
        case 0b0010: // sub
            state->regs[rd] = state->regs[rn] - ((i == 0) ? state->regs[rm] : imm);
            break;
    }

    if (rd != PC) {
        state->regs[PC] = state->regs[PC] + 4;
    }
}

bool is_b_bl_inst(unsigned int iw) { return (((iw >> 25) & 0b111) == 0b101); }

bool cpsr_check_cond(unsigned int cpsr, unsigned int cond) {
    unsigned int N, Z, C, V;
    N = (cpsr >> 31) & 0b1;
    Z = (cpsr >> 30) & 0b1;
    C = (cpsr >> 29) & 0b1;
    V = (cpsr >> 28) & 0b1;

    if 
    (
        (cond == 0b1110) // b
        || (cond == 0b0000 && Z == 0b1) // beq
        || (cond == 0b1011 && (N != V)) // blt
        || (cond == 0b1101 && (Z == 0b1 || (N != V))) // ble
    )
    {
        return true;
    }

    return false;
}

void armemu_b_bl(struct arm_state *state, unsigned int iw) {
    unsigned int cond, link;
    cond = (iw >> 28) & 0b1111;
    link = (iw >> 24) & 0b1;
    int offset = ((int)(iw & 0xFFFFFF)) << 2;
    if (((offset >> 25) & 0b1) == 0b1) {
        offset |= 0b11111100000000000000000000000000;
    } else {
        offset &= 0b00000011111111111111111111111111;
    }
    offset += 8;

    if (!cpsr_check_cond(state->cpsr, cond)) {
        state->analysis.b_not_taken += 1;
        state->regs[PC] += 4;
    } else {
        state->analysis.b_taken += 1;
        if (link == 0b1) {
            state->regs[LR] = state->regs[PC] + 4;
        }
        state->regs[PC] += offset;
    }
}

bool is_sdt_inst(unsigned int iw) { return (((iw >> 26) & 0b11) == 0b01); }

void armemu_sdt(struct arm_state *state, unsigned int iw) {
    unsigned int rd = (iw >> 12) & 0xF;
    unsigned int rn = (iw >> 16) & 0xF;
    unsigned int offset = iw & 0xFFF;
    unsigned int U = (iw >> 23) & 0b1;
    unsigned int L = (iw >> 20) & 0b1;
    unsigned int B = (iw >> 22) & 0b1;

    unsigned int *addr;
    if (U == 0b1) {
        addr = (unsigned int *)(state->regs[rn] + offset);
    } else {
        addr = (unsigned int *)(state->regs[rn] - offset);
    }

    if (L == 0b1) { 
        if (B == 0b1) {
            state->regs[rd] = (*addr & 0xFF);
        } else {
            state->regs[rd] = *addr;
        }
    } else { 
        *addr = state->regs[rd];
    }

    if (rd != PC) {
        state->regs[PC] = state->regs[PC] + 4;
    }
} 

bool is_mul_inst(unsigned int iw) { return (((iw >> 4) & 0b1111) == 0b1001) && (((iw >> 22) & 0b111111) == 0b000000); }

void armemu_mul(struct arm_state *state, unsigned int iw) {
    unsigned int rd = (iw >> 16) & 0b1111;
    unsigned int rm = iw & 0b1111;
    unsigned int rs = (iw >> 8) & 0b1111;
    state->regs[rd] = state->regs[rm] * state->regs[rs];
    if (rd != PC) {
        state->regs[PC] = state->regs[PC] + 4;
    }
}

void print_array(int *array, int len)
{
    int i;
    printf("{");
    for (i = 0; i < len; i++) {
        printf("%d", array[i]);
        if (i != len-1) {
            printf(", ");
        }
    }
    printf("}\n");
}

void lookup(unsigned int * addr, int m, int n, int index_bit, struct cache_entry cache[m][n], struct cache_analysis *stat) {
    unsigned int local_addr = (unsigned int) addr;
    unsigned int index_flag = 0b1;
    for (int i = 0; i < index_bit - 1; i++) {
        index_flag = (index_flag << 1) + 1;
    }

    unsigned int index = index_bit == 0 ? 0 : (local_addr >> 2) & index_flag;
    unsigned int tag = local_addr >> (2 + index_bit);

    stat->num_ref++;
    for (int j = 0; j < n; j++) {
        if ((cache[index][j].valid == 0b1) && (cache[index][j].tag == tag)) {
            stat->num_hit++;
            // For instructor: 
            // I should return value here, but see description in cache_lookup().
            // i.e.:
            // return cache[index][j].data;
            return;
        }
    }
    stat->num_miss++;
    struct cache_entry entry = {1, tag, *addr};
    cache[index][rand() % n] = entry;
}

unsigned int cache_lookup(struct arm_state *state, unsigned int * addr) {
    lookup(addr, 8, 1, 3, state->dm8, &state->dm8_a);
    lookup(addr, 32, 1, 5, state->dm32, &state->dm32_a);
    lookup(addr, 128, 1, 7, state->dm128, &state->dm128_a);
    lookup(addr, 1024, 1, 10, state->dm1024, &state->dm1024_a);

    lookup(addr, 1, 8, 0, state->fa8, &state->fa8_a);
    lookup(addr, 1, 32, 0, state->fa32, &state->fa32_a);
    lookup(addr, 1, 128, 0, state->fa128, &state->fa128_a);
    lookup(addr, 1, 1024, 0, state->fa1024, &state->fa1024_a);

    lookup(addr, 2, 4, 1, state->sa8, &state->sa8_a);
    lookup(addr, 8, 4, 3, state->sa32, &state->sa32_a);
    lookup(addr, 32, 4, 5, state->sa128, &state->sa128_a);
    lookup(addr, 256, 4, 8, state->sa1024, &state->sa1024_a);
    
    // For instructor: I understand this is not correct way to return value, 
    // but since I hit 12 caches simultaneously, this might be a compromise
    return *addr;
}

unsigned int armemu(struct arm_state *state)
{
    while (state->regs[PC] != 0) {
        unsigned int iw = cache_lookup(state, (unsigned int *) state->regs[PC]);
        state->analysis.num_inst += 1;
        if (is_bx_inst(iw)) {
            state->analysis.b_inst += 1;
            armemu_bx(state, iw);
        } else if (is_mul_inst(iw)) {
            state->analysis.dp_inst += 1;
            armemu_mul(state, iw);
        } else if (is_b_bl_inst(iw)) {
            state->analysis.b_inst += 1;
            armemu_b_bl(state, iw);
        } else if (is_dp_inst(iw)) {
            state->analysis.dp_inst += 1;
            armemu_dp(state, iw);
        } else if (is_sdt_inst(iw)) {
            state->analysis.memo_inst += 1;
            armemu_sdt(state, iw);
        }
    }

    return state->regs[0];
}

void test_quadratic(struct arm_state *state, int a, int b, int c, int d) {
    printf("\n******** Quadratic Test ********\n");
    printf("quadratic(%d,%d,%d,%d) | C %d | S %d\n",a,b,c,d, quadratic_c(a,b,c,d), quadratic_s(a,b,c,d));
    arm_state_init(state, (unsigned int *) quadratic_s,a,b,c,d);
    printf("armemu(quadratic_s(%d,%d,%d,%d)) = %d\n",a,b,c,d, armemu(state));
    arm_analysis_print(state);
    printf("\n");
}

void test_sum_array(struct arm_state *state, int len, int array[len]) {
    printf("\n******** sum_array Test ********\n");
    printf("sum_array | C %d | S %d\n", sum_array_c(array, len), sum_array_s(array, len));
    arm_state_init(state, (unsigned int *) sum_array_s, (unsigned int)array, len, 0, 0);
    printf("armemu(sum_array_s) = %d\n", armemu(state));
    arm_analysis_print(state);
    printf("\n");
}

void test_find_max(struct arm_state *state, int len, int array[len]) {
    printf("\n******** find max Test ********\n");
    printf("find_max | C %d | S %d\n", find_max_c(array, len), find_max_s(array, len));
    arm_state_init(state, (unsigned int *) find_max_s, (unsigned int)array, len, 0, 0);
    printf("armemu(find_max_s) = %d\n", armemu(state));
    arm_analysis_print(state);
    printf("\n");
}

void test_fib_iter(struct arm_state *state, int iter) {
    printf("\n******** fib_iter Test ********\n");
    printf("fib_iter | C %d | S %d\n", fib_iter_c(iter), fib_iter_s(iter));
    arm_state_init(state, (unsigned int *) fib_iter_s, iter, 0, 0, 0);
    printf("armemu(fib_iter) = %d\n", armemu(state));
    arm_analysis_print(state);
    printf("\n");
}

void test_fib_rec(struct arm_state *state, int rec) {
    printf("\n******** fib_rec Test ********\n");
    printf("fib_rec | C %d | S %d\n", fib_rec_c(rec), fib_rec_s(rec));
    arm_state_init(state, (unsigned int *) fib_rec_s, rec, 0, 0, 0);
    printf("armemu(fib_rec) = %d\n", armemu(state));
    arm_analysis_print(state);
    printf("\n");
}

void copy_array(int *a1, int *a2, int len)
{
    int i;

    for (i = 0; i < len; i++) {
        a2[i] = a1[i];
    }
}

void test_sort(struct arm_state *state, int len, int array[len]) {
    int sort_c_arr[len];
    int sort_s_arr[len];
    int sort_emu_arr[len];

    copy_array(array, sort_c_arr, len);
    copy_array(array, sort_s_arr, len);
    copy_array(array, sort_emu_arr, len);

    printf("\n******** Sort Test ********\nOriginal: ");
    print_array(sort_c_arr, len);

    printf("C sort: ");
    sort_c(sort_c_arr, len);
    print_array(sort_c_arr, len);

    printf("S sort: ");
    sort_s(sort_s_arr, len);
    print_array(sort_s_arr, len);

    arm_state_init(state, (unsigned int *) sort_s, (unsigned int)sort_emu_arr, len, 0, 0);
    armemu(state);
    printf("EMU sort: ");
    print_array(sort_emu_arr, len);
    arm_analysis_print(state);
    printf("\n");
}

void generate_array(int *array, int n)
{
    for (int i = 0; i < n; i++)
    {
        array[i] = rand() % 57;
    }
}

void test_strlen(struct arm_state *state, char string[]) {
    printf("\n\n******** strlen TEST ********\n%s, C: %d, S: %d ", string, strlen_c(string), strlen_s(string));
    arm_state_init(state, (unsigned int *) strlen_s, (unsigned int)string, 0, 0, 0);
    printf("EMU: %d\n", armemu(state));
    arm_analysis_print(state);
    printf("\n");
}
  
int main(int argc, char **argv)
{
    struct arm_state state;
    int len_1 = 3;
    int array_1[] = {100, 2, 3};
    int len_2 = 6;
    int array_2[] = {3, -2, 6, 0, 9, -9};
    int len_3 = 10;
    int array_3[] = {1, 2, 3, 4, 5, 6, 7, 0, 8, 9};
    int len_4 = 1000;
    int array_4[len_4];
    generate_array(array_4, len_4);

    test_quadratic(&state, 2, 3, 4, 5);
    test_quadratic(&state, 0, -4, 0, 5);
    test_quadratic(&state, -2, 3, -4, 5);

    test_sum_array(&state, len_1, array_1);
    test_sum_array(&state, len_2, array_2);
    test_sum_array(&state, len_3, array_3);
    test_sum_array(&state, len_4, array_4);

    test_find_max(&state, len_1, array_1);
    test_find_max(&state, len_2, array_2);
    test_find_max(&state, len_3, array_3);
    test_find_max(&state, len_4, array_4);
    
    test_fib_iter(&state, 3);
    test_fib_iter(&state, 6);
    test_fib_iter(&state, 9);
    test_fib_iter(&state, 12);

    test_fib_rec(&state, 3);
    test_fib_rec(&state, 6);
    test_fib_rec(&state, 9);
    test_fib_rec(&state, 12);

    test_sort(&state, len_1, array_1);
    test_sort(&state, len_2, array_2);
    test_sort(&state, len_3, array_3);
    test_sort(&state, len_4, array_4);

    test_strlen(&state, "TellMeDoYouHateMe");
    test_strlen(&state, "1234");
    test_strlen(&state, "      ");
    test_strlen(&state, "");
    test_strlen(&state, " ");
    test_strlen(&state, "=-_@!");

    return 0;
}