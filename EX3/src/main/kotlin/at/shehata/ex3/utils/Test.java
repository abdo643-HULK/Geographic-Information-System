package at.shehata.ex3.utils;

import java.awt.*;
import java.util.Arrays;

public class Test {
    static void main() {
        final var x = new Polygon(new int[]{0}, new int[]{0}, 1);
        final var s = new GeoObject("0", 2, x, Arrays.asList(new Polygon[2]));
        s.getId();
    }
}
