package ch.epfl.sdp.social;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class User {
    @PrimaryKey
    @ColumnInfo(name = "userID")
    @NonNull
    public String email;

    @Ignore
    private String username;

    public User(String email)
    {
        this.email = email;
    }

    // This constructor should not be used for database related operations (hence @Ignore)
    @Ignore
    public User(String email, String username)
    {
        this.email = email;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

}