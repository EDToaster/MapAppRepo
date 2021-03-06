package tech.edt.MapApp.feature;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import tech.edt.MapApp.Hours;
import tech.edt.MapApp.util.Util;

import static tech.edt.MapApp.MapsActivity.*;

/**
 * A University object with 3 campus objects
 */
public class University {
    private HashMap<String, Campus> campuses;
    private Campus current_selected;

    private static final LatLng UTSGLL = new LatLng(43.6644, -79.3923);
    private static final LatLng UTMLL = new LatLng(43.5479, -79.6609);
    private static final LatLng UTSCLL = new LatLng(43.7841, -79.1868);
    private Context context;

    public University(Context context, String... campusNames) {
        campuses = new HashMap<>();

        campuses.put("UTSG", new Campus(UTSGLL, campusNames[0], T_UTSG));
        campuses.put("UTM", new Campus(UTMLL, campusNames[1], T_UTM));
        campuses.put("UTSC", new Campus(UTSCLL, campusNames[2], T_UTSC));
        this.context = context;


        Comparator cmp = new Comparator<Feature>() {
            public int compare(Feature f1, Feature f2) {
                return f1.toString().compareTo(f2.toString());
            }
        };

        for (Campus i : campuses.values())
            Collections.sort(i.getFeatures(), cmp);
    }

    private HashMap<String, Campus> getCampuses() {
        return campuses;
    }

    public boolean setCurrentSelected(String campus) {
        Campus c = this.campuses.get(campus);
        if (c == null)
            return false;
        this.current_selected = c;
        return true;
    }

    public Campus getCurrentSelected() {
        return current_selected;
    }

    public ArrayList<Feature> getAllFeatures() {
        ArrayList<Feature> to_return = new ArrayList<>();
        for (Campus i : campuses.values())
            to_return.addAll(i.getFeatures());
        return to_return;
    }

    public University setUpFeatures(AssetManager assetManager) {
        try {
            setUpBuildings(assetManager);
            setUpFood(assetManager);
            setUpBikes(assetManager);
            setUpCars(assetManager);
            setUpGreenSpaces(assetManager);
            setUpStudentServices(assetManager);
        } catch (Exception e) {
            Log.e("setUpFeatures", "Exception", e);
            System.exit(1);
        }
        return this;
    }

    /**
     * Parses JSON asset files and creates Building objects in the appropriate campus
     *
     * @param assetManager the assetManager from the activity
     * @throws Exception
     */
    private void setUpBuildings(AssetManager assetManager) throws Exception {

        JSONArray arr = Util.getBaseObj(assetManager, "buildings.json")
                .getJSONArray("buildings");

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject ij = arr.getJSONObject(i);

                double lat = ij.getDouble("lat");
                double lng = ij.getDouble("lng");
                String name = ij.getString("name");
                String code = ij.getString("code");
                String short_name = ij.getString("short_name");
                ArrayList<LatLng> polygon = new ArrayList<>();
                JSONArray json_polygon = ij.getJSONArray("polygon");
                if (json_polygon != null) {
                    int len = json_polygon.length();
                    for (int j = 0; j < len; j++) {
                        JSONArray temp = json_polygon.getJSONArray(j);
                        try {
                            LatLng cords = new LatLng(temp.getDouble(0), temp.getDouble(1));
                            polygon.add(cords);
                        } catch (Exception e) {
                            Log.e("setUpBuildings", "problem adding a polygon to a " +
                                    "building", e);

                        }
                    }
                }

                JSONObject address = ij.getJSONObject("address");
                String street = address.getString("street");
                String s = street + "\n" +
                        address.getString("city") + " " +
                        address.getString("province") + " " +
                        address.getString("country") + "\n" +
                        address.getString("postal");

                Building b = new Building(lat, lng, name, code, street, s, short_name, polygon);

                getCampuses().get(ij.getString("campus")).addFeature(b);
            } catch (JSONException e) {
                Log.e("setUpBuildings", "BUILDING EXCEPTION", e);
            }
        }
    }

    /**
     * Parses JSON asset files and creates Food objects in the appropriate campus
     *
     * @param assetManager the assetManager from the activity
     * @throws Exception
     */
    private void setUpFood(AssetManager assetManager) throws Exception {


        JSONArray arr = Util.getBaseObj(assetManager, "food.json")
                .getJSONArray("food");

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject ij = arr.getJSONObject(i);
                double lat = ij.getDouble("lat");
                double lng = ij.getDouble("lng");
                String name = ij.getString("name");
                String short_name = ij.getString("short_name");
                String address = ij.getString("address");
                String url = ij.getString("url");
                String imageURL = ij.getString("image");
                String desc = ij.getString("description");
                JSONObject h = ij.getJSONObject("hours");

                Hours hours = new Hours(h, context);
                String[] tags = Util.toStringArray(ij.getJSONArray("tags"));

                Food f = new Food(lat, lng, name, address, short_name, url, imageURL, desc, hours,
                        tags);
                getCampuses().get(ij.getString("campus")).addFeature(f);
            } catch (JSONException e) {
                Log.e("setUpFood", "FOOD EXCEPTION", e);
            }
        }
    }

    /**
     * Parses JSON asset files and creates BikePark objects in the appropriate campus
     *
     * @param assetManager the assetManager from the activity
     * @throws Exception
     */
    private void setUpBikes(AssetManager assetManager) throws Exception {
        JSONArray arr = Util.getBaseObj(assetManager, "bicycle-racks.json")
                .getJSONArray("markers");

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject ij = arr.getJSONObject(i);
                double lat = ij.getDouble("lat");
                double lng = ij.getDouble("lng");
                String name = ij.getString("title");
                String buildingCode = ij.getString("buildingCode");
                String desc = ij.getString("desc");
                BikePark b = new BikePark(lat, lng, name, buildingCode, desc);
                if (!name.contains("BIXI"))  //get rid of bikeshare, at least for now.
                    getCampuses().get("UTSG").addFeature(b);

            } catch (JSONException e) {
                Log.e("setUpBikes", "BIKE EXCEPTION", e);
            }
        }
    }

    /**
     * Parses JSON asset files and creates CarPark objects in the appropriate campus
     *
     * @param assetManager the assetManager from the activity
     * @throws Exception
     */
    private void setUpCars(AssetManager assetManager) throws Exception {
        JSONArray arr = Util.getBaseObj(assetManager, "parking-lots.json")
                .getJSONArray("markers");

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject ij = arr.getJSONObject(i);
                double lat = ij.getDouble("lat");
                double lng = ij.getDouble("lng");
                String name = ij.getString("title");
                String address = ij.getString("address");
                String buildingCode = ij.getString("buildingCode");
                String phone = ij.getString("phone");

                String desc = ij.getString("desc");

                CarPark c = new CarPark(lat, lng, name, buildingCode, address, desc, phone);

                getCampuses().get("UTSG").addFeature(c);

            } catch (JSONException e) {
                Log.e("setUpCars", "CAR EXCEPTION", e);
            }
        }
    }

    /**
     * Parses JSON asset files and creates StudentService objects in the appropriate campus
     *
     * @param assetManager the assetManager from the activity
     * @throws Exception
     */
    private void setUpStudentServices(AssetManager assetManager) throws Exception {
        JSONArray arr = Util.getBaseObj(assetManager, "student-services.json")
                .getJSONArray("markers");

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject ij = arr.getJSONObject(i);
                double lat = ij.getDouble("lat");
                double lng = ij.getDouble("lng");
                String name = ij.getString("title");
                String url = ij.getString("url");
                String address = ij.getString("address");
                String desc = ij.getString("desc");
                String phone;
                try {
                    phone = ij.getString("phone");
                } catch (Exception e) {
                    phone = "";
                }
                StudentService b = new StudentService(new LatLng(lat, lng), name,
                        address, phone, url, desc);

                getCampuses().get("UTSG").addFeature(b);
            } catch (JSONException e) {
                Log.e("setUpStudentServices", "SS_EXCEPTION", e);
            }
        }
    }

    //TODO: IMPLEMENT ME!

    /**
     * Parses JSON asset files and creates MiscSafety and EmergencyPhone
     * objects in the appropriate campus
     *
     * @param assetManager the assetManager from the activity
     * @throws Exception
     */
    private void setUpSafety(AssetManager assetManager) throws Exception {
        JSONArray arr = Util.getBaseObj(assetManager, "safety.json")
                .getJSONArray("markers");

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject ij = arr.getJSONObject(i);
                double lat = ij.getDouble("lat");
                double lng = ij.getDouble("lng");
                String name = ij.getString("title");
                String url = ij.getString("url");
                String address = ij.getString("address");
                String desc = ij.getString("desc");
                String phone;
                try {
                    phone = ij.getString("phone");
                } catch (Exception e) {
                    phone = "";
                }
                //TODO: Figure out the plan for the safety thing first
//                Safety b = new SafetyMisc(new LatLng(lat, lng), name,
//                        address, phone, url, desc);


            } catch (JSONException e) {
                Log.e("setUpSafety", "SAFETY_EXCEPTION", e);
            }
        }
    }

    /**
     * Parses JSON asset files and creates GreenSpace objects in the appropriate campus
     *
     * @param assetManager  the assetManager from the activity
     * @throws Exception
     */
    private void setUpGreenSpaces(AssetManager assetManager) throws Exception {
        JSONArray arr = Util.getBaseObj(assetManager, "green-u-of-t.json")
                .getJSONArray("markers");

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject ij = arr.getJSONObject(i);
                JSONArray type = ij.getJSONArray("sublayers");
                if (type.getInt(0) == 23) {
                    double lat = ij.getDouble("lat");
                    double lng = ij.getDouble("lng");
                    String name = ij.getString("title");
                    String address = ij.getString("address");
                    String desc = ij.getString("desc");

                    GreenSpace b = new GreenSpace(new LatLng(lat, lng), name,
                            address, desc);

                    getCampuses().get("UTSG").addFeature(b);


                }
            } catch (JSONException e) {
                Log.e("setUpGreenSpaces", "GREEN_EXCEPTION", e);
            }
        }
    }


}
