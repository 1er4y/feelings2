package com.sloth.feelings;

public interface Config {

    // used to share GCM regId with application server - using php app server
   /// static final String APP_SERVER_URL = "http://ec2-52-10-76-194.us-west-2.compute.amazonaws.com/SendPushNotification";

    // GCM server using java
    static final String APP_SERVER_URL =
    "http://androidserver-jxbj8qesua.elasticbeanstalk.com/GCMNotificationServlet";

    // Google Project Number
    static final String GOOGLE_PROJECT_ID = "472464617563";
    static final String MESSAGE_KEY = "message";
    static final String REGISTER_ID = "currentUserId";
    static final String TO_ID = "secondUserId";
}
