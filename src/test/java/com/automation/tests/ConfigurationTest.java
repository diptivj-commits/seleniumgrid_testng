package com.automation.tests;
import com.automation.config.Config;
import com.automation.support.GridWait;
import com.automation.support.PracticeServer;
import org.testng.annotations.Test;
import java.net.*;
import java.nio.charset.StandardCharsets;
import static org.testng.Assert.*;
public class ConfigurationTest {
 @Test public void gridUrlIsHttp(){assertTrue(Config.get("grid.url").startsWith("http"));}
 @Test public void waitIsPositive(){assertTrue(Config.positiveInt("wait.seconds")>0);}
 @Test public void registeredNodesAreParsed(){
  String json="{\"value\":{\"nodes\":[{\"availability\":\"UP\",\"slots\":[{\"stereotype\":{\"browserName\":\"chrome\"}}]},{\"availability\":\"DOWN\",\"slots\":[{\"stereotype\":{\"browserName\":\"firefox\"}}]}]}}";
  assertEquals(GridWait.registeredBrowsers(json),java.util.Set.of("chrome"));
 }
 @Test public void fixtureIncludesHiddenButtonFix()throws Exception {
  try(PracticeServer server=new PracticeServer("127.0.0.1",0)){
   HttpURLConnection connection=(HttpURLConnection)URI.create("http://127.0.0.1:"+server.port()+"/").toURL().openConnection();
   connection.setConnectTimeout(5000);connection.setReadTimeout(5000);
   try{assertEquals(connection.getResponseCode(),200);try(var in=connection.getInputStream()){String html=new String(in.readAllBytes(),StandardCharsets.UTF_8);assertTrue(html.contains("button[hidden]{display:none!important}"));assertTrue(html.contains("id=\"username\""));}}finally{connection.disconnect();}
  }
 }
}
