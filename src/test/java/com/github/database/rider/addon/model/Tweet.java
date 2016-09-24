package com.github.database.rider.addon.model;


import javax.persistence.*;
import java.util.Calendar;

/**
 * Created by pestano on 22/07/15.
 */
@Entity
public class Tweet {

    @Id
    @GeneratedValue
    private String id;

    private String content;

    private Integer likes;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar date;

    @ManyToOne
    User user;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }


    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tweet tweet = (Tweet) o;

        return !(id != null ? !id.equals(tweet.id) : tweet.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
