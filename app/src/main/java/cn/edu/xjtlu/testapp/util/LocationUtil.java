package cn.edu.xjtlu.testapp.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import cn.edu.xjtlu.testapp.domain.Point;

public class LocationUtil implements LocationListener{
    private LocationManager locationManager;
    private String locationProvider;
    private MyLocationListener mListener;

    private LocationUtil() {}

    public LocationUtil(Context context, MyLocationListener listener) {
        mListener = listener;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        List<String> providers = locationManager.getProviders(true);

        LogUtil.d("LocationUtil", "LocationUtil: " + providers.toString());
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }
    }

    @SuppressLint("MissingPermission")
    public Location getLocation() {
        Location location = locationManager.getLastKnownLocation(locationProvider);
        return location;
    }

    public boolean canGetLocation() {
        return locationProvider != null;
    }

    @SuppressLint("MissingPermission")
    public void requestLocationUpdates() {
        if (locationProvider == null || mListener == null) return;
        locationManager.requestLocationUpdates(locationProvider, 3000, 1, this);
    }

    public void removeUpdates() {
        if (mListener == null) return;
        locationManager.removeUpdates(this);
    }

    public static Point imageToGeo(PointF point) {
        SamplePoint p1 = SamplePoint.P1;
        SamplePoint p2 = SamplePoint.P2;

        double ratioX = (p2.getGeoPoint().getY() - p1.getGeoPoint().getY()) / (p2.getImagePoint().x - p1.getImagePoint().x);
        double ratioY = (p2.getGeoPoint().getX() - p1.getGeoPoint().getX()) / (p2.getImagePoint().y - p1.getImagePoint().y);

        return new Point(p1.getGeoPoint().getX() + (point.y - p1.getImagePoint().y) * ratioY, p1.getGeoPoint().getY() + (point.x - p1.getImagePoint().x) * ratioX);
    }

    public static PointF geoToImage(Point point) {
        SamplePoint p1 = SamplePoint.P1;
        SamplePoint p2 = SamplePoint.P2;

        double ratioX = (p2.getGeoPoint().getY() - p1.getGeoPoint().getY()) / (p2.getImagePoint().x - p1.getImagePoint().x);
        double ratioY = (p2.getGeoPoint().getX() - p1.getGeoPoint().getX()) / (p2.getImagePoint().y - p1.getImagePoint().y);

        Point origin = new Point(p1.getGeoPoint().getX() - p1.getImagePoint().y * ratioY, p1.getGeoPoint().getY() - p1.getImagePoint().x * ratioX);

        return new PointF((float) Math.floor((point.getY() - origin.getY()) / ratioX), (float) Math.floor((point.getX() - origin.getX()) / ratioY));
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mListener.onLocationChanged(location);
    }

    public interface MyLocationListener {
        void onLocationChanged(@NonNull Location location);
    }

    enum SamplePoint {
        P1(new PointF(108, 78), new Point(31.277379, 120.731379)),
        P2(new PointF(604, 1333), new Point(31.269947, 120.734879));

        private PointF imagePoint;
        private Point geoPoint;

        SamplePoint(PointF imagePoint, Point geoPoint) {
            this.imagePoint = imagePoint;
            this.geoPoint = geoPoint;
        }

        public PointF getImagePoint() {
            return imagePoint;
        }

        public Point getGeoPoint() {
            return geoPoint;
        }
    }
}
