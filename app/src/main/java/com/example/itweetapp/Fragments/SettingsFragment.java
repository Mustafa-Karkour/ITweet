package com.example.itweetapp.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.itweetapp.Activities.LoginActivity;
import com.example.itweetapp.Activities.MainActivity;
import com.example.itweetapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private EditText editUsername;
    private Button confirmBtn, deleteAccountBtn;

    private Dialog deleteAccountDialog;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference postRef;
    private DatabaseReference commentRef;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();
        postRef = FirebaseDatabase.getInstance().getReference("Posts");
        commentRef = FirebaseDatabase.getInstance().getReference("Comments");

        editUsername = view.findViewById(R.id.settings_edit_username);
        confirmBtn = view.findViewById(R.id.settings_confirm_btn);
        deleteAccountBtn = view.findViewById(R.id.delete_account_btn);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updateName = editUsername.getText().toString().trim();

                if(updateName.isEmpty()){
                    editUsername.setFocusable(true);
                    editUsername.setError("Empty Field");
                }else {
                    updateCurrentUserName(updateName);
                }
            }
        });

        deleteAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccountDialog();
            }
        });

        return view;
    }

    private void deleteAccountDialog() {

        deleteAccountDialog = new Dialog(getContext());
        deleteAccountDialog.setContentView(R.layout.delete_account_dialog);
        deleteAccountDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        Button cancelBtn = deleteAccountDialog.findViewById(R.id.cancel_account_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccountDialog.dismiss();
            }
        });

        Button deleteAccountBtn = deleteAccountDialog.findViewById(R.id.delete_account_btn);
        deleteAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUserPhotos();
                deleteAccount();
            }
        });

        deleteAccountDialog.show();

    }

    private void deleteAccount() {


        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    deleteAccountDialog.dismiss();
                    startActivity(new Intent(getContext(), LoginActivity.class));
                    showMessage("Account Deleted");


                }else{

                }
            }
        });

    }

    private void deleteUserPhotos() {

        /*
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){

                    String userEmail = postSnapshot.child("userEmail").getValue(String.class);

                    if(userEmail.equals(currentUser.getEmail())){

                        databaseReference.child(postSnapshot.getKey()).removeValue();
                        //ExploreFragment.postAdapter.notifyDataSetChanged();
                    }
                    ExploreFragment.postAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

         */

        deletePostFromDB();

        deletePostFromFS();

        deleteProfilePhotoFromFS();

        deleteCoverPhotoFromFS();


    }

    private void deleteCoverPhotoFromFS() {

        StorageReference coverReference = firebaseStorage.getReference("Cover_photo");
        if(ProfileFragment.coverPhoto != null){
            StorageReference coverImage = coverReference.child(currentUser.getEmail());
            coverImage.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){

                        // showMessage("Cover Photo deleted");

                    }else{


                        showMessage("Exception: "+task.getException().getMessage());

                    }


                }
            });
        }

    }

    private void deleteProfilePhotoFromFS() {

        StorageReference profileReference = firebaseStorage.getReference("Users_photo");
        final StorageReference profileImage = profileReference.child(currentUser.getEmail());
        profileImage.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    //showMessage("Profile Image is deleted");
                } else {
                    showMessage("Exception: " + task.getException().getMessage());
                }


            }
        });

    }

    private void deletePostFromFS() {

        int postSegmentIndex=0;
        StorageReference postReference = firebaseStorage.getReference("Post_images");
        for(int postIndex = 0 ; postIndex < ExploreFragment.postList.size(); postIndex++) {


            String userEmail = ExploreFragment.postList.get(postIndex).getUserEmail();

            if(userEmail.equals(currentUser.getEmail())){

                if(!MainActivity.postsSegments.isEmpty()) {

                    StorageReference postImage = postReference.child(MainActivity.postsSegments.get(postSegmentIndex++) + "(" + currentUser.getEmail() + ")");
                    postImage.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                //showMessage("Posts deleted");

                            } else {

                                showMessage("Exception: " + task.getException().getMessage());

                            }
                        }
                    });
                }

            }

        }

    }

    private void deletePostFromDB() { //and comments

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(final DataSnapshot postSnapshot : dataSnapshot.getChildren()){

                    String userEmail = postSnapshot.child("userEmail").getValue(String.class);

                    if(userEmail.equals(currentUser.getEmail())){

                        postRef.child(postSnapshot.getKey()).removeValue();

                    }


                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        for(int postIndex = 0 ; postIndex < ExploreFragment.postList.size() ; postIndex++){


            final int finalPostIndex = postIndex;
            commentRef.child(ExploreFragment.postList.get(postIndex).getPostID())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot commentSnapshot : dataSnapshot.getChildren()){


                                String userEmail = commentSnapshot.child("userEmail").getValue(String.class);

                                if(userEmail.equals(currentUser.getEmail())){

                                    commentRef.child(ExploreFragment.postList.get(finalPostIndex).getPostID()).child(commentSnapshot.getKey()).removeValue();


                                }


                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


        }



    }

    private void updateCurrentUserName(String updatedName) {

        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(updatedName)
                .build();

        currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if(ProfileFragment.userName.getText().toString().isEmpty() || MainActivity.navUsername.getText().toString().isEmpty()){
                       ProfileFragment.userName.setText(currentUser.getDisplayName());
                        MainActivity.navUsername.setText(currentUser.getDisplayName());
                    }
                    ProfileFragment.userName.setText(currentUser.getDisplayName());
                    MainActivity.navUsername.setText(currentUser.getDisplayName());
                    showMessage("Updated");
                } else {
                    showMessage("Exception: " + task.getException().getMessage());
                }
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
