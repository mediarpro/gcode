public static void main(final String[] args)
{
    int a = 0;
    final int b = 5;

    for (int i=0; i<b; i++)
    {
        a = a + i*b;
    }

    if (a > 40)
    {
        System.out.println(a);
    }
    else
    {
        System.out.println(b);
    }
}