package com.example.itweetapp.Fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.itweetapp.Activities.MainActivity;
import com.example.itweetapp.Adapters.PostAdapterGV;
import com.example.itweetapp.Models.CommentModel;
import com.example.itweetapp.Models.PostModel;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ImageView userPhoto;
    public static ImageView coverPhoto;
    private ImageView profileEditBtn;
    private Uri pickedPhoto = null;
    public static  TextView userName;
    private TextView userEmail;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private DatabaseReference commentRef;
    private ArrayList<PostModel> myPosts;
    private ArrayList<CommentModel> commentList;
    private CommentModel myComments = new CommentModel();
    private GridView myPostsGV;
    private PostAdapterGV postAdapterGV;


    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Posts");
        commentRef = database.getReference("Comments");

        userPhoto = view.findViewById(R.id.profile_user_photo);
        userName = view.findViewById(R.id.profile_username);
        userEmail = view.findViewById(R.id.profile_user_email);
        coverPhoto = view.findViewById(R.id.profile_cover);
        profileEditBtn = view.findViewById(R.id.profile_edit_btn);

        profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        retrieveCoverPhoto(); //if a Cover Photo exist
        /*
        coverPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

         */


        myPostsGV = view.findViewById(R.id.mypostsGV);
        return view;
    }




    private void showDialog() {


        final Dialog profileEditDialog = new Dialog(getActivity());
        profileEditDialog.setContentView(R.layout.profile_edit_dialog);
        profileEditDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);

        TextView editCoverPhoto = profileEditDialog.findViewById(R.id.profile_edit_cover_photo);
        editCoverPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
                profileEditDialog.dismiss();
            }
        });

        TextView editProfilePhoto = profileEditDialog.findViewById(R.id.profile_edit_profile_photo);
        editProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGalleryForProfilePhoto();
                profileEditDialog.dismiss();
            }
        });


        profileEditDialog.show();
    }



    private void addToStorage(Uri pickedPhoto) {

        if (pickedPhoto != null) {
            StorageReference reference = FirebaseStorage.getInstance().getReference().child("Cover_photo");
            final StorageReference imageFilePath = reference.child(currentUser.getEmail());
            imageFilePath.putFile(pickedPhoto).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        showMessage("Added");
                    } else {
                        showMessage("Exception: " + task.getException().getMessage());
                    }
                }
            });
        }
    }

    private void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void openGallery() {
        Intent toGallery = new Intent(Intent.ACTION_GET_CONTENT);
        toGallery.setType("image/*"); // <-- accept any type of images
        startActivityForResult(toGallery, 2);
    }

    private void openGalleryForProfilePhoto() {
        Intent toGallery = new Intent(Intent.ACTION_GET_CONTENT);
        toGallery.setType("image/*"); // <-- accept any type of images
        startActivityForResult(toGallery, 3);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == 2) {
            pickedPhoto = data.getData();
            Glide.with(getActivity()).load(pickedPhoto).into(coverPhoto); // update User's Cover Photo
            addToStorage(pickedPhoto);
        } else if (data != null && requestCode == 3) {
            pickedPhoto = data.getData();
            updateProfilePhoto(pickedPhoto);
        }
    }

    private void updateProfilePhoto(Uri pickedPhoto) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Users_photo");
        final StorageReference imageFilePath = storageReference.child(currentUser.getEmail());
        imageFilePath.putFile(pickedPhoto).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();

                        currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    updatePhotos(); //update User's Profile Photo in Profile Page and Comment Section
                                    showMessage("Changed");

                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void updatePhotos() {
        Glide.with(getActivity()).load(currentUser.getPhotoUrl()).apply(new RequestOptions().circleCrop()).into(userPhoto);
        Glide.with(getActivity()).load(currentUser.getPhotoUrl()).apply(new RequestOptions().circleCrop()).into(MainActivity.navUserPhoto);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        for (int postIndex = 0; postIndex < myPosts.size(); postIndex++) {
            if (myPosts.get(postIndex).getUserEmail().equals(currentUser.getEmail())) {
                DatabaseReference myRef = database.getReference("Posts");
                myRef.child(myPosts.get(postIndex).getPostID()).child("userPhoto").setValue(currentUser.getPhotoUrl().toString());
            }
        }

    }



    @Override
    public void onStart() {
        super.onStart();

        Glide.with(getActivity()).load(currentUser.getPhotoUrl()).apply(new RequestOptions().circleCrop()).into(userPhoto);

        userName.setText(currentUser.getDisplayName());
        userEmail.setText(currentUser.getEmail());

        //retrieveCoverPhoto();

        myRef.addValueEventListener(new ValueEventListener() { //<-- get Posts' list from DB
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myPosts = new ArrayList<>();
                for (DataSnapshot snapshotPost : dataSnapshot.getChildren()) {
                    String userEmail = snapshotPost.child("userEmail").getValue(String.class);
                    if (userEmail.equals(currentUser.getEmail())) {
                        PostModel myPost = snapshotPost.getValue(PostModel.class);
                        myPosts.add(myPost);
                    }
                }
                postAdapterGV = new PostAdapterGV(getActivity(), myPosts);
                myPostsGV.setAdapter(postAdapterGV);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void retrieveCoverPhoto() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Cover_photo");
        final StorageReference imageFilePath = storageReference.child(currentUser.getEmail());
        imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getActivity()).load(uri).into(coverPhoto);
            }
        });

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
