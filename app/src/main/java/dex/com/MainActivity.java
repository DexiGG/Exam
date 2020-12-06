package dex.com;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import android.text.format.DateFormat;

import java.util.LinkedList;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class MainActivity extends AppCompatActivity {

    private static int SIGN_IN_CODE = 1;
    private LinkedList<String> existUsers;
    private LinkedList<String> usersList;
    private Menu menu;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RelativeLayout activity_main;
    private FirebaseListOptions<Message> options;
    private FirebaseListAdapter<Message> adapter;
    private EmojiconEditText emojiconEditText;
    private ImageView emojiBtn;
    private ImageView submitBtn;
    private EmojIconActions emojIconActions;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_CODE) {
            if (requestCode == RESULT_OK) {
                Snackbar.make(
                        activity_main,
                        "User Authorized",
                        Snackbar.LENGTH_LONG
                ).show();
                displayAllMessages();
            } else {
                Snackbar.make(
                        activity_main,
                        "User Not Authorized",
                        Snackbar.LENGTH_LONG
                ).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        existUsers = new LinkedList<>();
        usersList = new LinkedList<String>();
        drawerLayout = findViewById(R.id.drawerLayout);
        activity_main = findViewById(R.id.mainActivity);
        navigationView = findViewById(R.id.navigationView);
        menu = navigationView.getMenu();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                displayAllUsers(usersList);
                drawerLayout.openDrawer(navigationView);
                //Toast.makeText(MainActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        //username = "test";
        submitBtn = findViewById(R.id.submitBtn);
        emojiBtn = findViewById(R.id.emojiBtn);
        emojiconEditText = findViewById(R.id.textField);
        emojIconActions = new EmojIconActions(getApplicationContext(), activity_main, emojiconEditText, emojiBtn);
        emojIconActions.ShowEmojIcon();

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().push().setValue(new Message(
                        FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                        emojiconEditText.getText().toString()
                ));
                emojiconEditText.setText("");
            }
        });

        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_CODE);
        else
            Snackbar.make(
                    activity_main,
                    "User Authorized",
                    Snackbar.LENGTH_LONG
            ).show();

        displayAllMessages();
        if(usersList.size()>=50) usersList.clear();
    }

    private void displayAllMessages() {
        ListView listOfMessages = findViewById(R.id.list_of_messages);
        options = new FirebaseListOptions.Builder<Message>()
                .setQuery(FirebaseDatabase.getInstance().getReference(), Message.class)
                .setLayout(R.layout.list_item)
                .setLifecycleOwner(this)
                .build();

        adapter = new FirebaseListAdapter<Message>(options) {
            @Override
            protected void populateView(View v, Message model, int position) {
                TextView messUser, messTime;
                BubbleTextView messText;
                messUser = v.findViewById(R.id.message_user);
                messTime = v.findViewById(R.id.message_time);
                messText = v.findViewById(R.id.message_text);

                messUser.setText(model.getUserName());
                messText.setText(model.getTextMessage());
                messTime.setText(DateFormat.format("dd-MM-yyyy HH:mm:ss", model.getMessageTime()));

                usersList.add(model.getUserName());
                //Toast.makeText(MainActivity.this, model.getTextMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        listOfMessages.setAdapter(adapter);
    }

    private void displayAllUsers(LinkedList<String> usersList) {

        for (String user : usersList)
        {
            if(!existUsers.contains(user)){
                menu.add(user);
                existUsers.add(user);
            }

        }
        usersList.clear();
        existUsers.clear();
    }


}