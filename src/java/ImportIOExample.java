import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import com.importio.api.clientlite.ImportIO;
import com.importio.api.clientlite.MessageCallback;
import com.importio.api.clientlite.data.Progress;
import com.importio.api.clientlite.data.Query;
import com.importio.api.clientlite.data.QueryMessage;
import com.importio.api.clientlite.data.QueryMessage.MessageType;

/**
 * An example class for making use of the import.io Java client library
 * 
 * <p>Modifications from sample code ({@link https://import.io/data/integrate/#easyjava}):
 * <ul>
 * <li>Takes GUID and API key as positional arguments.
 * <li>Moved out of {@code com.importio.api.clientlite.example} package.
 * </ul>
 * 
 * <p>Usage: {@code bazel run -- //src/java:ImportIOExample "<YOUR GUID>" "<YOUR API KEY>"}. 
 * 
 * @author dev@import.io
 * @see https://github.com/import-io/importio-client-libs/tree/master/java
 */
public class ImportIOExample {

  public static void main(String[] args) throws IOException, InterruptedException {

    /**
     * GUID and API key are taken as positional arguments.
     */
    if (args.length != 2) {
      System.err.println("usage: bazel run //src/java:ImportIOExample <GUID> <API Key>");
      System.exit(1);
    }

    /**
     * To use an API key for authentication, use the following code:
     */
    ImportIO client = new ImportIO(UUID.fromString(args[0]), args[1], "import.io");

    /**
     * Once we have started the client and authenticated, we need to connect it to the server:
     */
    client.connect();

    /**
     * Because import.io queries are asynchronous, for this simple script we will use a {@link CountdownLatch}
     * to stop the script from exiting before all of our queries are returned. We are doing 2 queries in this
     * example so we initialise it with "2"
     */
    final CountDownLatch latch = new CountDownLatch(2);

    final List<Object> dataRows = new ArrayList<Object>();

    /**
     * In order to receive the data from the queries we issue, we need to define a callback method
     * This method will receive each message that comes back from the queries, and we can take that
     * data and store it for use in our app. {@see MessageCallback}
     */
    MessageCallback messageCallback = new MessageCallback() {
      /**
       * This method is called every time a new message is received from the server relating to the
       * query that we issued
       */
      @SuppressWarnings("unchecked")
      public void onMessage(Query query, QueryMessage message, Progress progress) {
        if (message.getType() == MessageType.MESSAGE) {
          HashMap<String, Object> resultMessage = (HashMap<String, Object>) message.getData();
          if (resultMessage.containsKey("errorType")) {
            // In this case, we received a message, but it was an error from the external service
            System.err.println("Got an error!");
            System.err.println(message);
          } else {
            // We got a message and it was not an error, so we can process the data
            System.out.println("Got data!");
            System.out.println(message);
            // Save the data we got in our dataRows variable for later
            List<Object> results = (List<Object>) resultMessage.get("results");
            dataRows.addAll(results);
          }
        }
        // When the query is finished, countdown the latch so the program can continue when everything is done
        if ( progress.isFinished() ) {
          latch.countDown();
        }
      }
    };

    Map<String, Object> queryInput;
    Query query;
    List<UUID> connectorGuids;

    // Query for tile Integrate Page Example
    connectorGuids = Arrays.asList(
      UUID.fromString("caff10dc-3bf8-402e-b1b8-c799a77c3e8c")
    );
    queryInput = new HashMap<String,Object>();
    queryInput.put("searchterm", "avengers\r");

    query = new Query();
    query.setConnectorGuids(connectorGuids);
    query.setInput(queryInput);

    client.query(query, messageCallback);

    // Query for tile Integrate Page Example
    connectorGuids = Arrays.asList(
      UUID.fromString("caff10dc-3bf8-402e-b1b8-c799a77c3e8c")
    );
    queryInput = new HashMap<String,Object>();
    queryInput.put("searchterm", "avengers 2");

    query = new Query();
    query.setConnectorGuids(connectorGuids);
    query.setInput(queryInput);

    client.query(query, messageCallback);

    // Wait on all of the queries to be completed
    latch.await();

    // It is best practice to disconnect when you are finished sending queries and getting data - it allows us to
    // clean up resources on the client and the server
    client.disconnect();

    // Now we can print out the data we got
    System.out.println("All data received:");
    System.out.println(dataRows);
  }
}