package com.codeflops.fuelio;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.codeflops.fuelio.adapter.GarageVehicleListAdapter;
import com.codeflops.fuelio.model.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyGarageFragment extends Fragment {
    private static final String DB_REF_GARAGE = "garage";
    private static final String TAG = MyGarageFragment.class.getSimpleName();

    DatabaseReference mGarageRef;
    DatabaseReference mVehicleRef;
    FirebaseAuth mAuth;
    ValueEventListener mValueEventListener;

    String mUserId;
    ActionBar mActionBar;

    @BindView(R.id.garage_vehicles_list)
    ListView mGarageListView;

    GarageVehicleListAdapter garageVehicleListAdapter;
    List<Vehicle> vehicles = new ArrayList<>();

    public MyGarageFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_garage, container, false);
        ButterKnife.bind(this, view);
        setActionBar();
        setGarageList(view);
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!initGarageRefs()) return;
        prepareUI();
        addEventListenerForVehicles();
    }

    private void setActionBar() {
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void setGarageList(View view) {
        garageVehicleListAdapter = new GarageVehicleListAdapter(getActivity(), 0, vehicles);

        mGarageListView.setEmptyView(view.findViewById(R.id.text_garage_empty));
        mGarageListView.setAdapter(garageVehicleListAdapter);
    }


    @OnClick(R.id.fab)
    void onFabClick() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_to_garage, null);

        final MaterialEditText editTextBrand = view.findViewById(R.id.vehicle_brand);
        final MaterialEditText editTextModel = view.findViewById(R.id.vehicle_model);
        final Spinner spinner = view.findViewById(R.id.vehicle_version);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_add_vehicle)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String brand = editTextBrand.getText().toString();
                        String model = editTextModel.getText().toString();
                        String version = spinner.getSelectedItem().toString();
                        if (!validateForm(brand, model))
                            return;
                        createNewVehicle(brand, model, version);
                        alertDialog.dismiss();
                    }

                    private boolean validateForm(String brand, String model) {
                        if (TextUtils.isEmpty(brand)) {
                            editTextBrand.setError("Please add brand information");
                            Toast.makeText(getActivity(), "Brand name can't be empty", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if (TextUtils.isEmpty(model)) {
                            editTextModel.setError("Please provide model details");
                            Toast.makeText(getActivity(), "Version can't be empty", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        return true;
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void prepareUI() {
        garageVehicleListAdapter.setmVehicleRef(mVehicleRef);
        mActionBar.setTitle(mAuth.getCurrentUser().getDisplayName() + "'s Garage");
    }

    private boolean initGarageRefs() {
        mGarageRef = FirebaseDatabase.getInstance().getReference(DB_REF_GARAGE);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getActivity(), "Error getting vehicle list. Please sign in again", Toast.LENGTH_SHORT).show();
            return false;
        }
        mUserId = mAuth.getCurrentUser().getUid();
        mVehicleRef = FirebaseDatabase.getInstance().getReference(DB_REF_GARAGE + "/" + mUserId);
        return true;
    }

    private void addEventListenerForVehicles() {
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (vehicles.size() > 0)
                    vehicles.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    vehicles.add(snapshot.getValue(Vehicle.class));
                garageVehicleListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mVehicleRef.addValueEventListener(mValueEventListener);
    }

    void createNewVehicle(String brand, String model, String version) {
        String id = mVehicleRef.push().getKey();
        Vehicle vehicle = new Vehicle(id, brand, model, version);
        mGarageRef.child(mUserId).child(id).setValue(vehicle);
    }
}
