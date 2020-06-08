#include<stdio.h>
#include <stdlib.h> 
#include<time.h>

int quadratic_c(int x, int a, int b, int c);
int quadratic_s(int x, int a, int b, int c);

int sum_array_c(int *array, int n);
int sum_array_s(int *array, int n);

int find_max_c(int *array, int len);
int find_max_s(int *array, int len);

int find_max_c(int *array, int len);
int find_max_s(int *array, int len);

int sort_c(int *array, int len);
int sort_s(int *array, int len);

int fib_iter_c(int n);
int fib_iter_s(int n);

int fib_rec_c(int n);
int fib_rec_s(int n);

int strlen_c(char *s);
int strlen_s(char *s);

void generate_array(int *array, int n)
{
    srand(time(0));
    for (int i = 0; i < n; i++)
    {
        array[i] = rand() % 57;
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

void copy_array(int *a1, int *a2, int len)
{
    int i;

    for (i = 0; i < len; i++) {
        a2[i] = a1[i];
    }
}

int main(int argc, char **argv)
{
    // Program 1: quadratic (equation)
    printf("\nTest Program 1\n");
    printf("(3 * (2 * 2)) + (4 * 2) + 5 = 25 | C %d | S %d\n", quadratic_c(2,3,4,5), quadratic_s(2,3,4,5));
    printf("(3 * (0 * 0)) + (4 * 0) + 5 = 5 | C %d | S %d\n", quadratic_c(0,3,4,5), quadratic_s(0,3,4,5));
    printf("(3 * (-2 * -2)) + (4 * -2) + 5 = 9 | C %d | S %d\n", quadratic_c(-2,3,4,5), quadratic_s(-2,3,4,5));
    printf("(-3 * (-2 * -2)) + (4 * -2) + (-5) = -25 | C %d | S %d\n", quadratic_c(-2,-3,4,-5), quadratic_s(-2,-3,4,-5));
    printf("(-3 * (2 * 2)) + (4 * 2) + (-5) = -9 | C %d | S %d\n", quadratic_c(2,-3,4,-5), quadratic_s(2,-3,4,-5));

    // Program 2: sum_array
    printf("\nTest Program 2\n");
    int test_array_1[] = {1};
    printf("{1} | C_sum %d | S_sum %d\n", sum_array_c(test_array_1, 1), sum_array_s(test_array_1, 1));

    int test_array_2[] = {-3};
    printf("{-3} | C_sum %d | S_sum %d\n", sum_array_c(test_array_2, 1), sum_array_s(test_array_2, 1));

    int test_array_3[] = {0};
    printf("{0} | C_sum %d | S_sum %d\n", sum_array_c(test_array_3, 1), sum_array_s(test_array_3, 1));

    int test_array_4[] = {1, 4, 2};
    printf("{1, 4, 2} | C_sum %d | S_sum %d\n", sum_array_c(test_array_4, 3), sum_array_s(test_array_4, 3));

    int test_array_5[] = {-3, -2, -6, 3, 9, -12};
    printf("{-3, -2, -6, 3, 9, -12} | C_sum %d | S_sum %d\n", sum_array_c(test_array_5, 6), sum_array_s(test_array_5, 6));

    int n = 1100;
    int test_array_6[n];
    generate_array(test_array_6, n);
    print_array(test_array_6, n);
    printf("C_sum %d | S_sum %d\n", sum_array_c(test_array_6, n), sum_array_s(test_array_6, n));

    // Program 3: find_max_c
    printf("\nTest Program 3\n");
    int test_find_max_array[4] = {1,2,3,0};
    print_array(test_find_max_array, 4);
    printf("C_max %d | S_max %d\n", find_max_c(test_find_max_array, 4), find_max_s(test_find_max_array, 4));
    printf("\n");

    int test_find_max_array_2[6] = {-5, -4, -6, -9, -4, -8};
    print_array(test_find_max_array_2, 6);
    printf("C_max %d | S_max %d\n", find_max_c(test_find_max_array_2, 6), find_max_s(test_find_max_array_2, 6));
    printf("\n");

    int test_find_max_array_3[6] = {1, 4, -8, 9, -1, 3};
    print_array(test_find_max_array_3, 6);
    printf("C_max %d | S_max %d\n", find_max_c(test_find_max_array_3, 6), find_max_s(test_find_max_array_3, 6));
    printf("\n");

    int test_find_max_array_4[n];
    generate_array(test_find_max_array_4, n);
    print_array(test_find_max_array_4, n);
    printf("C_max %d | S_max %d\n", find_max_c(test_find_max_array_4, n), find_max_s(test_find_max_array_4, n));
    printf("\n");

    // Program 4: Simple Sort (Descending)
    printf("\nTest Program 4\n");
    int test_sort_array_1[6] = {0, -6, -1, 2, -4, -8};
    int test_sort_array_1_dup[6] = {0, -6, -1, 2, -4, -8};
    printf("Original array: ");
    print_array(test_sort_array_1, 6);
    
    printf("Sorted array by C: ");
    sort_c(test_sort_array_1, 6);
    print_array(test_sort_array_1, 6);

    printf("Sorted array by S: ");
    sort_s(test_sort_array_1_dup, 6);
    print_array(test_sort_array_1_dup, 6);
    printf("\n");

    //
    int test_sort_array_2[3] = {1,2,3};
    int test_sort_array_2_dup[3] = {1,2,3};
    printf("Original array: ");
    print_array(test_sort_array_2, 3);
    
    printf("Sorted array by C: ");
    sort_c(test_sort_array_2, 3);
    print_array(test_sort_array_2, 3);

    printf("Sorted array by S: ");
    sort_s(test_sort_array_2_dup, 3);
    print_array(test_sort_array_2_dup, 3);
    printf("\n");

    //
    int test_sort_array_3[3] = {1,2,-3};
    int test_sort_array_3_dup[3] = {1,2,-3};
    printf("Original array: ");
    print_array(test_sort_array_3, 3);
    
    printf("Sorted array by C: ");
    sort_c(test_sort_array_3, 3);
    print_array(test_sort_array_3, 3);

    printf("Sorted array by S: ");
    sort_s(test_sort_array_3_dup, 3);
    print_array(test_sort_array_3_dup, 3);
    printf("\n");

    //
    int test_sort_array_4[n];
    int test_sort_array_4_dup[n];

    generate_array(test_sort_array_4, n);
    copy_array(test_sort_array_4, test_sort_array_4_dup, n);
    printf("Original array: ");
    print_array(test_sort_array_4, n);
    
    sort_c(test_sort_array_4, n);
    printf("Sorted array by C: ");
    print_array(test_sort_array_4, n);

    sort_s(test_sort_array_4_dup, n);
    printf("Sorted array by S: ");
    print_array(test_sort_array_4_dup, n);
    printf("\n");

    // Program 5: fib_iter
    printf("\nTest Program 5\n");
    printf("First 20 by C: ");
    for (int i = 1; i <= 20; i++)
    {
        printf("%d ", fib_iter_c(i));
    }
    printf("\n");
    printf("First 20 by S: ");
    for (int i = 1; i <= 20; i++)
    {
        printf("%d ", fib_iter_s(i));
    }
    printf("\n");

    // Program 6: fib_rec
    printf("\nTest Program 6\n");
    printf("First 20 by C: ");
    for (int i = 1; i <= 20; i++)
    {
        printf("%d ", fib_rec_c(i));
    }
    printf("\n");
    printf("First 20 by S: ");
    for (int i = 1; i <= 20; i++)
    {
        printf("%d ", fib_rec_s(i));
    }
    printf("\n");


    // Program 7: strlen
    printf("\nTest Program 7\n");
    char strlen_1[] = "abc";
    char strlen_2[] = "1234";
    char strlen_3[] = "a2b4c";
    char strlen_4[] = "      ";
    char strlen_5[] = "";
    char strlen_6[] = " ";
    char strlen_7[] = "=-_@!";

    printf("%s, C length: %d, S length: %d\n", strlen_c(strlen_1), strlen_s(strlen_1));
    printf("%s, C length: %d, S length: %d\n", strlen_c(strlen_2), strlen_s(strlen_2));
    printf("%s, C length: %d, S length: %d\n", strlen_c(strlen_3), strlen_s(strlen_3));
    printf("%s, C length: %d, S length: %d\n", strlen_c(strlen_4), strlen_s(strlen_4));
    printf("%s, C length: %d, S length: %d\n", strlen_c(strlen_5), strlen_s(strlen_5));
    printf("%s, C length: %d, S length: %d\n", strlen_c(strlen_6), strlen_s(strlen_6));
    printf("%s, C length: %d, S length: %d\n", strlen_c(strlen_7), strlen_s(strlen_7));

    return 0;
}