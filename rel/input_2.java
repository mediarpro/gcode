public class Rectangle
{

    final double width;
    final double height;

    public Rectangle(final double width, final double height)
    {
        this.width = width;
        this.height = height
    }

    public double getArea()
    {
        return width*height;
    }

    public double getPerimeter()
    {
        return 2*(width+height);
    }

    public Triangle getEqTriangleSameArea()
    {
        final double area = getArea();
        final double side = area * 2.30;

        return new Triangle(side, side, side);
    }

}