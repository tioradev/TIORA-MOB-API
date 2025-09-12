package com.tiora.mob.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Firebase Configuration
 * 
 * This is a placeholder configuration for Firebase Admin SDK.
 * To enable FCM notifications, you need to:
 * 
 * 1. Add Firebase Admin SDK dependency to pom.xml:
 *    <dependency>
 *        <groupId>com.google.firebase</groupId>
 *        <artifactId>firebase-admin</artifactId>
 *        <version>9.2.0</version>
 *    </dependency>
 * 
 * 2. Download Firebase service account key from Firebase Console
 * 3. Place the JSON file in src/main/resources/
 * 4. Uncomment and configure the FirebaseMessaging bean below
 * 5. Set the GOOGLE_APPLICATION_CREDENTIALS environment variable
 */
@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    /*
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        logger.info("Initializing Firebase App...");
        
        // Option 1: Using service account key file
        InputStream serviceAccount = getClass().getClassLoader()
            .getResourceAsStream("firebase-service-account-key.json");
        
        if (serviceAccount == null) {
            throw new FileNotFoundException("Firebase service account key not found");
        }
        
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://your-project-id-default-rtdb.firebaseio.com/")
            .build();
        
        // Option 2: Using environment variable (recommended for production)
        // FirebaseOptions options = FirebaseOptions.builder()
        //     .setCredentials(GoogleCredentials.getApplicationDefault())
        //     .setDatabaseUrl("https://your-project-id-default-rtdb.firebaseio.com/")
        //     .build();
        
        FirebaseApp app = FirebaseApp.initializeApp(options);
        logger.info("Firebase App initialized successfully");
        
        return app;
    }
    
    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        logger.info("Creating FirebaseMessaging bean");
        return FirebaseMessaging.getInstance(firebaseApp);
    }
    */
    
    /**
     * Placeholder bean for development
     * Remove this when Firebase is properly configured
     */
    @Bean
    public String firebasePlaceholder() {
        logger.warn("Firebase is not configured. FCM notifications will be logged but not sent.");
        return "Firebase not configured - development mode";
    }
}
