package com.mdb_software.helena.travelmemories3.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mdb_software.helena.travelmemories3.Activities.PlaceDetailActivity;
import com.mdb_software.helena.travelmemories3.R;
import com.mdb_software.helena.travelmemories3.model.ListViewPlaces;

import java.util.List;



public class MapDrawerFragment extends android.support.v4.app.Fragment {

    private View containerView;
    private DrawerLayout mDrawerLayout;
    private GoogleMap googleMap;
    private LatLngBounds.Builder bld = new LatLngBounds.Builder();

    public MapDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_map_drawer, container, true);

        return layout;
    }

    @Override
    public void onPause() {
        super.onPause();
       getActivity().getFragmentManager().popBackStack();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.map:
                mDrawerLayout.openDrawer(containerView);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Postavlja drawer
     * @param fragment_id
     * @param drawerLayout
     * @param toolbar
     * @param listaMjesta
     */
    public void setUp(int fragment_id, DrawerLayout drawerLayout, Toolbar toolbar, List<ListViewPlaces> listaMjesta) {

        containerView = getActivity().findViewById(fragment_id);
        mDrawerLayout = drawerLayout;
        setGoogleMaps(drawerLayout, listaMjesta);





    }

    /**
     * Postavlja markere na mapu
     * @param listaMjesta
     */
    private void addMarkersToMap(List<ListViewPlaces> listaMjesta) {
        ListViewPlaces item;
        googleMap.clear();
        for (int i = 0; i < listaMjesta.size(); i++) {
            item = listaMjesta.get(i);
            LatLng ll = new LatLng(item.getLatitude(), item.getLongitude());
            bld.include(ll);

            Marker marker = googleMap.addMarker(new MarkerOptions().
                    position(ll).title(item.getTitle()));
            marker.showInfoWindow();
            googleMap.moveCamera( CameraUpdateFactory.newLatLng(ll));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(2));

        }
    }


    /**
     * Dohvaca google map fragment i postavlja marker
     */
    private void setGoogleMaps(View layout, final List<ListViewPlaces> listaMjesta) {
        final LatLng TutorialsPoint = new LatLng(51 , 21);

        try {
            if (googleMap == null) {
                googleMap = ((MapFragment)getActivity().getFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            if (listaMjesta.size()>0) {
                addMarkersToMap(listaMjesta);
            }
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    double longitude = marker.getPosition().longitude;
                    double latitude = marker.getPosition().latitude;
                    ListViewPlaces item;
                    for (int i =0; i<listaMjesta.size();i++){
                        item = listaMjesta.get(i);
                         if ((Double.compare(item.getLongitude(),longitude)==0)&& (Double.compare(item.getLatitude(),latitude)==0)){
                            Intent intent = new Intent(getActivity().getApplicationContext(), PlaceDetailActivity.class);
                            intent.putExtra("MarkerID", item.getID());
                            intent.putExtra("datum", item.getDatum());
                            intent.putExtra("lokacija", item.getTitle());
                            intent.putExtra("url", item.getImageUrl());
                            startActivity(intent);
                            mDrawerLayout.closeDrawer(containerView);
                             }

                    }
                    return true;
                }
            });

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


}
