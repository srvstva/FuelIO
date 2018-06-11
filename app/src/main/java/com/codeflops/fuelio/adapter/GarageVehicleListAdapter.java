package com.codeflops.fuelio.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codeflops.fuelio.BuildConfig;
import com.codeflops.fuelio.R;
import com.codeflops.fuelio.model.Vehicle;
import com.google.firebase.database.DatabaseReference;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.List;

public class GarageVehicleListAdapter extends ArrayAdapter<Vehicle> {
    private static final String TAG = "GarageVehicleListAdapte";

    DatabaseReference mVehicleRef;

    public GarageVehicleListAdapter(@NonNull Context context, int resource, @NonNull List<Vehicle> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Vehicle vehicle = getItem(position);
        if (BuildConfig.DEBUG)
            Log.d(TAG, "getView: position is " + position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.garage_item, parent, false);
        }

        TextView brand = convertView.findViewById(R.id.vehicle_brand);
        TextView model = convertView.findViewById(R.id.vehicle_model);
        ImageView listAction = convertView.findViewById(R.id.list_action);


        listAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_vehicle_actions, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.action_edit:
                                showEditDialog(position);
                                return true;
                            case R.id.action_delete:
                                if (mVehicleRef != null) {
                                    mVehicleRef.child(vehicle.getId()).removeValue();
                                } else {
                                    Toast.makeText(getContext(), "Database reference is not set.. not deleting..", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                        }
                        return false;
                    }
                });
            }
        });
        brand.setText(vehicle.getBrand());
        model.setText(vehicle.getModel() + " (" + vehicle.getVersion() + ")");

        return convertView;
    }

    private void showEditDialog(int position) {
        final Vehicle vehicle = getItem(position);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_add_to_garage, null);

        final MaterialEditText editTextBrand = view.findViewById(R.id.vehicle_brand);
        final MaterialEditText editTextModel = view.findViewById(R.id.vehicle_model);
        final Spinner spinner = view.findViewById(R.id.vehicle_version);

        editTextBrand.setText(vehicle.getBrand());
        editTextModel.setText(vehicle.getModel());
        int selectedIndex = -1;
        switch (vehicle.getVersion()) {
            case "Petrol":
                selectedIndex = 0;
                break;
            case "Diesel":
                selectedIndex = 1;
                break;
            case "Electric":
                selectedIndex = 2;
                break;
        }

        spinner.setSelection(selectedIndex);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("Editing " + vehicle.getDisplayName())
                .setView(view)
                .setCancelable(true)
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", null);

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String brand = editTextBrand.getText().toString();
                        String model = editTextModel.getText().toString();
                        String version = spinner.getSelectedItem().toString();
                        if (TextUtils.isEmpty(brand)) {
                            editTextBrand.setError("Brand is empty");
                            return;
                        } else if (TextUtils.isEmpty(model)) {
                            editTextModel.setError("Model is empty");
                            return;
                        }

                        // Update vehicle info
                        vehicle.setBrand(brand);
                        vehicle.setModel(model);
                        vehicle.setVersion(version);

                        mVehicleRef.child(vehicle.getId()).setValue(vehicle);
                        Toast.makeText(getContext(), "Updated successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();

    }

    public void setmVehicleRef(DatabaseReference mVehicleRef) {
        this.mVehicleRef = mVehicleRef;
    }


}