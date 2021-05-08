package cn.edu.xjtlu.testapp.graphic;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GridIndex {
    public static final String TAG = GridIndex.class.getSimpleName();

    private static float width;
    private static float height;

    private static int xCellCount;
    private static int yCellCount;
    private static float xScale;
    private static float yScale;

    private static final List<Pair<String, BShape>> shapeList = new ArrayList<>();

    private static List<Integer>[] cellList;

    public static void init(float width, float height, int cellSize) {
        GridIndex.width = width;
        GridIndex.height = height;
        xCellCount = (int) Math.ceil(width / cellSize);
        yCellCount = (int) Math.ceil(height / cellSize);
        xScale = xCellCount / width;
        yScale = yCellCount / height;
        shapeList.clear();
        cellList = new List[xCellCount * yCellCount];

//        Log.d(TAG, "GridIndex: " + xCellCount + " " + yCellCount);
    }

    private static int convertToXCellCoord(float x) {
        return (int) Math.max(0, Math.min(xCellCount - 1, Math.floor(x * xScale)));
    }

    private static int convertToYCellCoord(float y) {
        return (int) Math.max(0, Math.min(yCellCount - 1, Math.floor(y * yScale)));
    }

    private static boolean completeIntersection(BShape shape) {
        return shape.minX >= 0 && shape.minY >= 0 && shape.maxX <= width && shape.maxY <= height;
    }

    private static boolean collide(BShape o1, BShape o2) {
        if (o1.getClass().equals(o2.getClass())) {
            return o1.getClass().equals(BBox.class) ? boxesCollide((BBox) o1, (BBox) o2): circlesCollide((BCircle) o1, (BCircle) o2);
        } else {
            return o1.getClass().equals(BBox.class) ? circleAndBoxCollide((BBox) o1, (BCircle) o2): circleAndBoxCollide((BBox) o2, (BCircle) o1);
        }
    }

    private static boolean boxesCollide(BBox o1, BBox o2) {
        return o1.minX <= o2.maxX &&
            o1.minY <= o2.maxY &&
            o1.maxX >= o2.minX &&
            o1.maxY >= o2.minY;
    }

    private static boolean circlesCollide(BCircle o1, BCircle o2) {
        float dx = o2.centerX - o1.centerX;
        float dy = o2.centerY - o1.centerY;
        float bothRadii = o1.radius + o2.radius;
        return (bothRadii * bothRadii) > (dx * dx + dy * dy);
    }

    private static boolean circleAndBoxCollide(BBox box, BCircle circle) {
        float halfRectWidth = (box.maxX - box.minX) / 2;
        float distX = Math.abs(circle.centerX - (box.minX + halfRectWidth));
        if (distX > (halfRectWidth + circle.radius)) {
            return false;
        }

        float halfRectHeight = (box.maxY - box.minY) / 2;
        float distY = Math.abs(circle.centerY - (box.minY + halfRectHeight));
        if (distY > (halfRectHeight + circle.radius)) {
            return false;
        }

        if (distX <= halfRectWidth || distY <= halfRectHeight) {
            return true;
        }

        float dx = distX - halfRectWidth;
        float dy = distY - halfRectHeight;
        return (dx * dx + dy * dy) <= (circle.radius * circle.radius);
    }

    public static int insert(Pair<String, BShape> pair, boolean checkCollision) {
        BShape shape = pair.second;
        if (!completeIntersection(shape)) {
//            Log.d(TAG, "!completeIntersection");
            return -1;
        }

        int cx1 = convertToXCellCoord(shape.minX);
        int cy1 = convertToYCellCoord(shape.minY);
        int cx2 = convertToXCellCoord(shape.maxX);
        int cy2 = convertToYCellCoord(shape.maxY);
//        Log.d(TAG, String.format("%d~%d %d~%d", cx1, cx2, cy1, cy2));
//        if (BBox.class.equals(shape.getClass())) {
//            BBox temp = (BBox) shape;
//            cx1 = convertToXCellCoord(temp.minX);
//            cy1 = convertToYCellCoord(temp.minY);
//            cx2 = convertToXCellCoord(temp.maxX);
//            cy2 = convertToYCellCoord(temp.maxY);
//        } else if (BBox.class.equals(shape.getClass())) {
//            BCircle temp = (BCircle) shape;
//            cx1 = convertToXCellCoord(temp.centerX - temp.radius);
//            cy1 = convertToYCellCoord(temp.centerY - temp.radius);
//            cx2 = convertToXCellCoord(temp.centerX + temp.radius);
//            cy2 = convertToYCellCoord(temp.centerY + temp.radius);
//        }

        int cellIndex;

        if (checkCollision) {
            for (int x = cx1; x <= cx2; ++x) {
                for (int y = cy1; y <= cy2; ++y) {
                    cellIndex = xCellCount * y + x;
                    if (cellList[cellIndex] == null) continue;
                    for (int i = 0; i < cellList[cellIndex].size(); i++) {
                        Integer index = cellList[cellIndex].get(i);
                        Pair<String, BShape> element = shapeList.get(index);
                        if (pair.first.equals(element.first)) continue;
                        if (collide(shape, element.second)) {
//                            Log.d(TAG, "insert: " + shape + " " + index + " " + shapeList.get(index));
//                            Log.d(TAG, "collide");
                            return -1;
                        }
                    }
                }
            }
        }

//        Log.d(TAG, "insert: " + shape);

        int index = shapeList.size();
        for (int x = cx1; x <= cx2; ++x) {
            for (int y = cy1; y <= cy2; ++y) {
                cellIndex = xCellCount * y + x;
                if (cellList[cellIndex] == null) {
                    cellList[cellIndex] = new ArrayList<>();
                }
                cellList[cellIndex].add(index);
            }
        }

        shapeList.add(pair);
        return shapeList.size();
    }
}
