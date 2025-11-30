package com.example.myapplication.Notification;

import com.google.auth.oauth2.GoogleCredentials;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AccessToken {
    
    private static final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";
    
    public String getAccessToken() {
        try {
            String jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"englishdic-80c3a\",\n" +
                    "  \"private_key_id\": \"294ca820eed3f8036efa18265a0678919f8666e2\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCjpsXc5qhXDst9\\nyY3/W/8ujQZPK+6gK8y3ObFlZC/vJBgCjbV+1xTYqnnQ5ymUkZwrAPW43ih/B2wE\\nOI2cR4kuvBq3GoDa1XrpZQmX6TbthjgagyTnKkJAYpzibknVF1aNNbJDQbG4KTwh\\nN9zMEwwEfg8yLncIRJAQ2fQ17QPZx8BmFUBpbXcr7MRbhggHmxaS1VCiMI4avTKo\\nM0OPvakgmQTA9lekgyxIm9X13/ewfJbm3tpQx2TpDIx4B9LHyUJs6aeWsAg+pMCX\\nSMrkrckF0Kt4nTo8xkQ+0Oq70bEpM72Z+s6smjD/HtODoDHz7ybAT4/RyxQhpBLv\\n2BpK5/cFAgMBAAECggEARntBtUlsEA6Rqoufie7TwPdA0+FOZRJoGTOSSOI02NBZ\\nPOROWiIE2unVDBk66b8qnMTNj4CyvT7zkP/QUZHnrcaa1OhnvZf610FKN/Bb1UGc\\nsSJxRtQG85blDYpFZy6leqy5KUlGYtPzjzTxdbzwTJEEN28Wmet5hqBwEreRFH5I\\nafFy/yVFWk7QJkASWcEFVOa5w2K/Lv2WzqD9cY4YkD5LzY4WjLohv0S8TNkwI2FN\\nEgsBovMGxo8cneNZlAUX+2gpNeDsGKaRUbJ3Jdo8eLjXpKNEuOq+KiEzXGd85TLT\\nH/J3yGl12RA+Q/R8UxI6AcSi8eNKpzpsZUjgS48wQwKBgQDSTUZUgMiB0HZenWED\\nhq5EymYpT+qFGlOYCOLc9F5YJf2PAE7uzJD66hpS8xZ7Qne57px5/TybVjuiaMYu\\nx0bVDuut9a33nJSRloAgxkiK116BEF8M/ICOUL1whXyTSqzSMTIwSwm9KbdX+wwO\\neg8uVJKZF9SMnlPWuOg8DzE5NwKBgQDHNmr9qX0kV7ziRU0KsNi6ozKTJ+m1WBMG\\ngFC2ixZNojWo8/enXQoVPXN9PLh4oP8WDVK+43ut+SkuHENIVs/NQNKAn6j1dZCy\\n1ubCcz122+viPlBeFx91PsfO2vXEfl3ry/4zd52FDEMdOUrpMWScdSDt5+LHioXy\\nDaq4GGQ/owKBgQCPzcN/2ShCjFfEzv0duiTEaVp+eWU2axAqcDkhOgChaij+dgmD\\nhWLGEPq7Kfp/gq33+FALorykw8y3e8m5Bw9KdY93Pv4cyXdWLoGi57OHLUo0GwvE\\nB42ow1Em9kB/P5r5iCZ3m3gnX7U9di+CXriux6oPAZqYJVPTClHTb9gK9QKBgCSg\\nE6GZVKGm49ox5Y0uoFoIARdfAi/OBoluc+hrv+j8BqOOXxzNFTp8dswyqrO63csf\\nHfRsfGOq6HBKz+/vzfqtjUYKlJS0TasjqX9ckpNUsZSB4mK6G5WBuliElyTOGtRs\\nR8sMldBNRdWMoAIpi5bcTfaPaHlJNYEgFCAOk/fDAoGBAL5MFIb+fOugJataqJns\\nyixkSwDUAUfoZej6ahx4iRuZezg2bWlKZ0fQDCveb079HbudF3vkap0KPYE5aYCJ\\nvto87K/94/slFNudiw5FzhU8pRjAbQiz19b7q1mCbMRTderbMij8HHZGB8aWNVzi\\nfZ43daCpigmKhnOZwHQrP0ar\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-6tlcp@englishdic-80c3a.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"113082292872690386341\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\"\n" +
                    "}";
            
            InputStream stream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Arrays.asList(firebaseMessagingScope));
            googleCredentials.refresh();
            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            return null;
        }
    }
}
