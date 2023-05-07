package com.thesis.documentscanner;

public class FirebaseQueries {
    //Singleton class to access user data from everywhere

    private static FirebaseQueries instance;

    private static String UID;

        private FirebaseQueries()
        {
            // private constructor
        }

        //method to return instance of class
        public static FirebaseQueries getInstance()
        {
            if (instance == null)
            {
                // if instance is null, initialize
                instance = new FirebaseQueries();
            }
            return instance;
        }

    public static String getUID() {
        return UID;
    }

    public static void setUID(String UID) {
        FirebaseQueries.UID = UID;
    }


}
