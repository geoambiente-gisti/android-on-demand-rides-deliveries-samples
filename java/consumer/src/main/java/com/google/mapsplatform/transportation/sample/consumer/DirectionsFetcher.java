package com.google.mapsplatform.transportation.sample.consumer;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;

public class DirectionsFetcher extends AsyncTask<Void, Void, DirectionsResult> {
    private static final String TAG = "DirectionsFetcher";
    private static final String API_KEY = BuildConfig.MAPS_API_KEY;

    private LatLng origin;
    private List<LatLng> destinations;
    private OnDirectionsTaskCompleted listener;

    public DirectionsFetcher(LatLng origin, List<LatLng> destinations, OnDirectionsTaskCompleted listener) {
        this.origin = origin;
        this.destinations = destinations;
        this.listener = listener;
    }

    @Override
    protected DirectionsResult doInBackground(Void... voids) {
        try {
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(API_KEY)
                    .build();

            DirectionsApiRequest request = DirectionsApi.newRequest(context)
                    .mode(TravelMode.DRIVING)
                    .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude));

            for (LatLng destination : destinations) {
                request = request.waypoints(new com.google.maps.model.LatLng(destination.latitude, destination.longitude));
            }

            request = request.destination(new com.google.maps.model.LatLng(destinations.get(destinations.size() - 1).latitude,
                    destinations.get(destinations.size() - 1).longitude));

            DirectionsResult result = request.await();

            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching directions: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(DirectionsResult directionsResult) {
        if (directionsResult != null) {
            List<LatLng> decodedPath = decodePolyline(directionsResult.routes[0].overviewPolyline.getEncodedPath());

            if (listener != null) {
                listener.onDirectionsTaskCompleted(decodedPath);
            }
        }
    }

    private List<LatLng> decodePolyline(String encodedPath) {
        List<LatLng> decodedPath = new ArrayList<>();
        int index = 0;
        int len = encodedPath.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;

            do {
                b = encodedPath.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;

            do {
                b = encodedPath.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            double latitude = lat / 1e5;
            double longitude = lng / 1e5;
            LatLng point = new LatLng(latitude, longitude);
            decodedPath.add(point);
        }

        return decodedPath;
    }

    public interface OnDirectionsTaskCompleted {
        void onDirectionsTaskCompleted(List<LatLng> points);
    }
}
