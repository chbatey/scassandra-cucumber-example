package info.batey.scassandra.example;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.ReadTimeoutException;
import com.datastax.driver.core.exceptions.WriteTimeoutException;
import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class UserService {

    private Gson gson = new Gson();
    private final Session session;
    private final Cluster cluster;

    public UserService() {
        cluster = Cluster.builder().addContactPoint("localhost").withPort(8042).build();
        session = cluster.connect("users");
    }

    public UserInformation retrieveUserInformation(String id) throws UnableToRetrieveUserInformationException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:8080/user/" + id);
        CloseableHttpResponse response;
        try {
            response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                EntityUtils.consumeQuietly(response.getEntity());
                UserInformation oldUserInformation = getExistingInfo(id);
                if (oldUserInformation != null) {
                    return oldUserInformation;
                } else {
                    throw new UnableToRetrieveUserInformationException();
                }
            }
            String responseAsString = EntityUtils.toString(response.getEntity());
            UserInformation userInformation = gson.fromJson(responseAsString, UserInformation.class);
            try {
                session.execute(String.format("insert into users(id, firstName, lastName) values ('%s','%s','%s')",
                        id, userInformation.getFirstName(), userInformation.getLastName()));
            } catch (WriteTimeoutException | NoHostAvailableException e) {
                // ignore exception
            }
            return userInformation;
        } catch (IOException e) {
            UserInformation oldUserInformation = getExistingInfo(id);
            if (oldUserInformation != null) {
                return oldUserInformation;
            } else {
                throw new UnableToRetrieveUserInformationException();
            }
        }
    }

    private UserInformation getExistingInfo(String id) {
        Row resultSet = null;
        try {
            resultSet = session.execute("select firstName, lastName from users where id = '" + id + "'").one();
        } catch (ReadTimeoutException | NoHostAvailableException e) {
            // ignore
            return null;
        }
        if (resultSet != null) {
            return new UserInformation(resultSet.getString("firstName"), resultSet.getString("lastName"));
        } else {
            return null;
        }
    }

    public void shutdown() {
        this.cluster.close();
    }
}
