#define T_DIR  1   // Directory
#define T_FILE 2   // File
#define T_DEV  3   // Device

struct theader {
    int sz; // the size of the target process
    int sp_usr; // the user-level stack pointer
    int lr_usr; // the user-level link register (this is important!)
    int pc; // the user-level pc
    uint    r14_svc;    // r14_svc (r14_svc == pc if SWI)
    uint    spsr;
    uint    r0;
    uint    r1;
    uint    r2;
    uint    r3;
    uint    r4;
    uint    r5;
    uint    r6;
    uint    r7;
    uint    r8;
    uint    r9;
    uint    r10;
    uint    r11;
    uint    r12;
    char name[16];
    int tracing;
    int code_data_size; // needed to determine how much to restore in resume. This should be a multiple of 4KB
};
