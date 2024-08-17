package dev.tw1t1.a10234_final_project;

public class LocationData {
    public double latitude;
    public double longitude;
    public long timestamp;

    public LocationData() {
        // Default constructor required for Firebase
    }

    public LocationData(double latitude, double longitude, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
}
