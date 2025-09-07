import java.io.Serializable;
import java.util.Objects;

public class Point implements Serializable {
    private final int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return p.x == x && p.y == y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
