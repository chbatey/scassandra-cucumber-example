package info.batey.scassandra.example;

import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserService {

    private Map<String, UserInformation> userInformationMap = new HashMap<String, UserInformation>();
    private Gson gson = new Gson();

    public UserInformation retrieveUserInformation(String id) throws UnableToRetrieveUserInformationException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:8080/user/" + id);
        CloseableHttpResponse response;
        try {
            response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                EntityUtils.consumeQuietly(response.getEntity());
                UserInformation oldUserInformation = userInformationMap.get(id);
                if (oldUserInformation != null) {
                    return oldUserInformation;
                } else {
                    throw new UnableToRetrieveUserInformationException();
                }
            }
            String responseAsString = EntityUtils.toString(response.getEntity());
            UserInformation userInformation = gson.fromJson(responseAsString, UserInformation.class);
            this.userInformationMap.put(id, userInformation);
            return userInformation;
        } catch (IOException e) {
            UserInformation oldUserInformation = userInformationMap.get(id);
            if (oldUserInformation != null) {
                return oldUserInformation;
            } else {
                throw new UnableToRetrieveUserInformationException();
            }
        }
    }
}
