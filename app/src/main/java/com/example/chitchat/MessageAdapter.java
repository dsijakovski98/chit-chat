package com.example.chitchat;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> mMessagesList;
    private FirebaseAuth mAuth;
    DatabaseReference mDatabaseReference ;
    Context context;

    public MessageAdapter(List<Messages> mMessagesList) {
        this.mMessagesList = mMessagesList;
    }


    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout2, parent, false);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        return new MessageViewHolder(view);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public TextView displayName;
        public TextView displayTime;
        public CircleImageView profileImage;


        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text_layout);
            displayName = itemView.findViewById(R.id.name_text_layout);
            displayTime = itemView.findViewById(R.id.time_text_layout);
            profileImage = itemView.findViewById(R.id.message_profile_layout);

            context = itemView.getContext();

            itemView.setOnLongClickListener(v -> {

                CharSequence options[] = new CharSequence[]{ "Delete", "Cancel" };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete this message");
                builder.setItems(options, (dialog, which) -> {

                    if(which == 0) {
                        // TODO: Finish delete message code
                        long mesPos = getAdapterPosition();
                        String mesId = mMessagesList.get((int)mesPos).toString();
                        Log.e("Message Id is ", mesId);
                        Log.e("Message is : ", mMessagesList.get((int)mesPos).getMessage());

                    }

                });
                builder.show();

                return true;
            });

        }


    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {
        Messages mes = mMessagesList.get(position);
        String from_user_id = mes.getFrom();

        long timeStamp = mes.getTime();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        String cal[] = calendar.getTime().toString().split(" ");
        String time_of_message = cal[1] + ", " + cal[2] + "  " + cal[3].substring(0, 5);
        Log.e("TIME IS : ", calendar.getTime().toString());

        holder.displayTime.setText(time_of_message);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(from_user_id);

        //---ADDING NAME THUMB_IMAGE TO THE HOLDER----
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                holder.displayName.setText(name);
                Picasso.get().load(image).placeholder(R.drawable.user_img).into(holder.profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        holder.messageText.setText(mes.getMessage());



    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }
}