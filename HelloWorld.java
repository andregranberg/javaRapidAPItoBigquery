package functions;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FormatOptions;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobStatistics.LoadStatistics;
import com.google.cloud.bigquery.TableDataWriteChannel;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.WriteChannelConfiguration;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient.Version;
import java.io.FileWriter;  
import java.io.*;
import java.net.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;

import java.util.HashMap;

public class HelloWorld implements HttpFunction {
  @Override
  public void service(HttpRequest request, HttpResponse response) throws Exception {
    java.net.http.HttpRequest APIrequest = java.net.http.HttpRequest.newBuilder()
		.uri(URI.create("https://alpha-vantage.p.rapidapi.com/query?function=GLOBAL_QUOTE&symbol=TSLA"))
		.header("x-rapidapi-key", "key_goes_here")
		.header("x-rapidapi-host", "alpha-vantage.p.rapidapi.com")
		.method("GET", java.net.http.HttpRequest.BodyPublishers.noBody())
		.build();
    java.net.http.HttpResponse<String> APIresponse = java.net.http.HttpClient.newHttpClient().send(APIrequest, java.net.http.HttpResponse.BodyHandlers.ofString());
    
    String data = APIresponse.body();
    
    JsonElement jsonElement = new JsonParser().parse(data);
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    JsonObject parsedJsonObject = jsonObject.get("Global Quote").getAsJsonObject();
    
    
    
    
    
    String symbol = parsedJsonObject.get("01. symbol").toString();
    String open = parsedJsonObject.get("02. open").toString();
    String high = parsedJsonObject.get("03. high").toString();
    String low = parsedJsonObject.get("04. low").toString();
    String price = parsedJsonObject.get("05. price").toString();
    String volume = parsedJsonObject.get("06. volume").toString();
    String latest_trading_day = parsedJsonObject.get("07. latest trading day").toString();
    String previous_close = parsedJsonObject.get("08. previous close").toString();
    String change = parsedJsonObject.get("09. change").toString();
    String change_percent = parsedJsonObject.get("10. change percent").toString();
    
    symbol = symbol.replace("\"","");
    open = open.replace("\"","");
    high = high.replace("\"","");
    low = low.replace("\"","");
    price = price.replace("\"","");
    volume = volume.replace("\"","");
    latest_trading_day = latest_trading_day.replace("\"","");
    previous_close = previous_close.replace("\"","");
    change = change.replace("\"","");
    change_percent = change_percent.replace("\"","");
    
    HashMap<String, String> stockInfo = new HashMap<String, String>();
    
    stockInfo.put("symbol", symbol);
    stockInfo.put("open", open);
    stockInfo.put("high", high);
    stockInfo.put("low", low);
    
    Gson gson = new Gson();
    String jsonString = gson.toJson(stockInfo);
    
    
    
    
    
    BufferedWriter jsonWriter = new BufferedWriter(new FileWriter("/tmp/fileName.json"));
    jsonWriter.write(jsonString);
    
    jsonWriter.close();

    String datasetName = "cloudFunctionETL";
    String tableName = "java2";
    Path jsonPath = FileSystems.getDefault().getPath("/tmp/", "fileName.json");
    // Path jsonPath = Paths.get("c:\\data\\myfile.txt");

    BufferedWriter writer = response.getWriter();

    try {
      BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

      TableId tableId = TableId.of(datasetName, tableName);

      WriteChannelConfiguration writeChannelConfiguration =
          WriteChannelConfiguration.newBuilder(tableId)
          .setFormatOptions(FormatOptions.json())
          .setAutodetect(true)
          .build();
      
      String jobName = "jobId_" + UUID.randomUUID().toString();
      JobId jobId = JobId.newBuilder().setLocation("us").setJob(jobName).build();

      try (TableDataWriteChannel fileWriter = bigquery.writer(jobId, writeChannelConfiguration);
          OutputStream stream = Channels.newOutputStream(fileWriter)) {
        Files.copy(jsonPath, stream);
      }

      Job job = bigquery.getJob(jobId);
      
      job = job.waitFor();
        if (job.isDone()) {
          writer.write("Json from GCS successfully loaded in a table");
        } else {
          writer.write(
            "BigQuery was unable to load into the table due to an error:"
                + job.getStatus().getError());
        }
    } catch (BigQueryException | InterruptedException e) {
        String problem = e.toString();
        writer.write(problem);
    }
  }
}




