package com.example.student.fb999;

/**
 * Created by student on 4/19/2017.
 */

public class User {
        private String _name;
        private String _email;
        private String _id;

        public User()
        {

        }
        public User(String name, String email)
        {
            _name = name;
            _email = email;
        }

        public String getName()
        {
            return _name;
        }
        public void setName(String name)
        {
            _name = name;
        }

        public String getEmail()
        {
            return _email;
        }
        public void setEmail(String email)
        {
            _email = email;
        }
    }

