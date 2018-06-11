package com.codeflops.fuelio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codeflops.fuelio.model.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int RC_SIGN_IN = 123;


    final List<AuthUI.IdpConfig> mAuthProviders = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build()
    );

    FirebaseAuth mAuth;
    DatabaseReference mDBRef;
    DatabaseReference mUserRef;
    FirebaseDatabase mDatabase;

    TextView mUserName;
    CircleImageView mProfilePic;

    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.container)
    FrameLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_action_menu);
        mDrawerLayout.closeDrawers();
        mNavigationView.setNavigationItemSelectedListener(this);
        startFragment(new HomeFragment());

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        authSignIn();
    }

    private void authSignIn() {
        if (mAuth.getCurrentUser() == null) {
            showSignInDialog();
        } else {
            showSnackText(getString(R.string.msg_welcome, mAuth.getCurrentUser().getEmail()));
            setupUI();
        }
    }

    private void setupUI() {
        View view = mNavigationView.getHeaderView(0);
        mUserName = view.findViewById(R.id.user_name);
        mProfilePic = view.findViewById(R.id.profile_pic);
        mUserName.setText(mAuth.getCurrentUser().getDisplayName());
        Uri photoUri = mAuth.getCurrentUser().getPhotoUrl();
        if (photoUri != null)
            Glide.with(this).load(photoUri).into(mProfilePic);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignIn(resultCode, data);
        }
    }

    private void handleSignIn(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (resultCode == RESULT_OK) {
            createUser();
        } else {
            if (response == null) {
                showSnackText(R.string.error_signed_in_cancel);
                return;
            }

            if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackText(R.string.error_no_internet);
                return;
            }
            if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showSnackText(getString(R.string.error_signing_in) + response.getError());
                //finish();
            }
        }

    }

    private void createUser() {
        String username = mAuth.getCurrentUser().getDisplayName();
        String email = mAuth.getCurrentUser().getEmail();
        String uid = mAuth.getCurrentUser().getUid();
        User user = new User(uid, email, username);
        Log.d("MainActivity", user.getEmail() + " Created at /users/" + user.getId());
        mUserRef = mDatabase.getReference("users");
        mUserRef.child(uid).setValue(user);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                signOut();
                return true;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showSignInDialog();
                    }
                });
    }

    private void showSignInDialog() {
        startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(mAuthProviders)
                        .setLogo(R.drawable.ilogo)
                        .setIsSmartLockEnabled(false)
                        .build()
                , RC_SIGN_IN);
    }

    void showSnackText(int resourceId) {
        Snackbar.make(mDrawerLayout, getString(resourceId), Toast.LENGTH_SHORT).show();
    }

    void showSnackText(String message) {
        Snackbar.make(mDrawerLayout, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_sign_out:
                signOut();
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.nav_my_garage:
                startFragment(new MyGarageFragment());
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.nav_home:
                startFragment(new HomeFragment());
                mDrawerLayout.closeDrawers();
                return true;
        }
        return true;
    }


    private void startFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
