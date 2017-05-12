package com.example.student.fb999;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btnWrite;
    Button btnSaveUser;
    Button btnSignUp;
    Button btnLogin;
    Button btnLogout;
    EditText etName;
    EditText etEmail;
    TextView tvUserStatus;
    DatabaseReference dbRoot;
    DatabaseReference dbUsers;
    FirebaseAuth auth;
    List<User> users = new ArrayList<User>();
    Long userID;
    UsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Reference to database Root and OtherUsers
        dbRoot = FirebaseDatabase.getInstance().getReference();
        dbUsers = dbRoot.child("OtherUsers");

        auth = FirebaseAuth.getInstance();
        auth.signOut();

        etName = (EditText)findViewById(R.id.etName);
        etEmail = (EditText)findViewById(R.id.etEmail);

        tvUserStatus = (TextView)findViewById(R.id.tvUserStatus);
        tvUserStatus.setText("Not Logged In");

        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setEnabled(false);

        //*********************************************************************
        //*********************************************************************
        //Reference to last user ID value---read once example
        dbRoot
                .child("LastUser")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                userID = (Long)dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //*********************************************************************
        //*********************************************************************
        //load data for selected user = "George"
        dbRoot
                .child("OtherUsers")
                .orderByChild("name")
                .equalTo("George")
                .addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    User user = new User();
                    DataSnapshot ds = dataSnapshot.getChildren().iterator().next();
                    user.setName(ds.child("name").getValue().toString());
                    user.setEmail(ds.child("email").getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //*********************************************************************
        //*********************************************************************
        //Monitors additions, changes and removals of Users, writes changes to Log
        //and updates the ListView
        dbUsers
                .addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                Log.d("ADDED---", user.toString());
                Log.d("Name:--", user.getName() + "  Email:--" + user.getEmail());
                //displayUsers(dataSnapshot, "ChildAdded");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                Log.d("CHANGED---", user.toString());
                Log.d("Name:--", user.getName() + "  Email:--" + user.getEmail());
                displayUsers(dataSnapshot, "ChildChanged");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Log.d("REMOVED---", user.toString());
                Log.d("Name:--", user.getName() + "  Email:--" + user.getEmail());
                displayUsers(dataSnapshot, "ChildRemoved");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //*********************************************************************
        //*********************************************************************

        //Loads all the users in Firebase into a List<User> and displays them in a ListView
        loadAllUsers();
        //*********************************************************************
        //*********************************************************************

        btnLogout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                auth.signOut();
                tvUserStatus.setText("Not Logged In");
                btnLogin.setEnabled(true);
                btnLogout.setEnabled(false);
                btnSignUp.setEnabled(true);

                ListView listView = (ListView) findViewById(R.id.lvUsers);
                listView.setAdapter(null);
            }
        });
        //*********************************************************************
        //*********************************************************************
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String email = etName.getText().toString();
                final String password = etEmail.getText().toString();

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                tvUserStatus.setText(e.getMessage());
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>()
                        {
                            @Override
                            public void onSuccess(AuthResult authResult)
                            {
//                                FirebaseUser user = auth.getCurrentUser();
//                                UserProfileChangeRequest upcr = new UserProfileChangeRequest.Builder()
//                                        .setDisplayName("Rodney").build();
//                                user.updateProfile(upcr);

                                tvUserStatus.setText(auth.getCurrentUser().getEmail());

                                etEmail.setText("");
                                etName.setText("");
                                btnSignUp.setEnabled(false);
                                btnLogin.setEnabled(false);
                                btnLogout.setEnabled(true);
                                loadAllUsers();
                            }
                });
            }
        });
        //*********************************************************************
        //*********************************************************************
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String email = etName.getText().toString();
                final String password = etEmail.getText().toString();

                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    FirebaseUser user = auth.getCurrentUser();
                                    tvUserStatus.setText("Welcome: " + user.getEmail());
                                    loadAllUsers();
                                }
                            })
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Log.d("FB999AfterLogin2", authResult.getUser().getEmail());
                                }
                            });
                    etName.setText("");
                    etEmail.setText("");
                    btnLogin.setEnabled(false);
                    btnLogout.setEnabled(true);
                    btnSignUp.setEnabled(false);
            }
        });
        //*********************************************************************
        //*********************************************************************
       //Creates a Users child (table) and adds two users
        //Creates a List of users, add multiple users to the list
        //Write the List to FB
        btnWrite = (Button) findViewById(R.id.btnWrite);
        btnWrite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //generates 10 example users with email address and pushes them to FB
                for (int i = 0; i<10; i++)
                {
                    User user = new User();
                    user.setName("User" + String.valueOf(i));
                    user.setEmail(String.valueOf(i) + "@gmail.com");
                    dbUsers.push().setValue(user); //Child name set by FB push--based on time stamp
                }
            }
        });
        //*********************************************************************
        //*********************************************************************
        //Writes user data from text boxes to FB and updates ListView to show latest added user
        btnSaveUser = (Button) findViewById(R.id.btnSaveUser);
        btnSaveUser.setOnClickListener(new View.OnClickListener()
           {
              @Override
               public void onClick(View v)
              {
                  etName = (EditText)findViewById(R.id.etName);
                  etEmail = (EditText)findViewById(R.id.etEmail);
                  DatabaseReference UsersRef = dbRoot.child("Users");
                  UsersRef.push().setValue(new User(etName.getText().toString(), etEmail.getText().toString()));

                  UsersRef
                          .orderByKey()
                          .addValueEventListener(new ValueEventListener() {
                      @Override
                      public void onDataChange(DataSnapshot dataSnapshot)
                      {
                        displayUsers(dataSnapshot, "SaveUser");
                      }

                      @Override
                      public void onCancelled(DatabaseError databaseError) {

                      }
                  });
              }
           }
        );
    }
    //*********************************************************************
    //*********************************************************************
    // Updates the ListView with the latest list of users
    private void displayUsers(DataSnapshot ds, String source)
    {
        users.clear();
        for (DataSnapshot user: ds.getChildren())
        {
            User newUser = new User();
            newUser.setName((String) user.child("name").getValue());
            newUser.setEmail((String) user.child("email").getValue());
            users.add(newUser);
        }
        adapter = new UsersAdapter(getBaseContext(), users);
        ListView listView = (ListView) findViewById(R.id.lvUsers);
        listView.setAdapter(adapter);
    }

    private void loadAllUsers()
    {
        dbUsers
                .orderByChild("name")
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)

                    {
                        displayUsers(dataSnapshot, "LoadAll");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

    }
}