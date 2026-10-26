package com.automation.support;
import com.automation.config.Config;
import org.openqa.selenium.json.Json;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
public final class GridWait {
 private GridWait(){}
 public static Set<String> registeredBrowsers(String json){
  Set<String> browsers=new HashSet<>();
  Map<?,?> root=new Json().toType(json,Map.class);
  Object value=root.get("value");
  if(!(value instanceof Map<?,?> status))return browsers;
  Object nodes=status.get("nodes");
  if(nodes instanceof List<?> list)for(Object node:list){
   if(!(node instanceof Map<?,?> n)||!"UP".equals(n.get("availability")))continue;
   if(n.get("slots") instanceof List<?> slots)for(Object slot:slots){
    if(slot instanceof Map<?,?> s && s.get("stereotype") instanceof Map<?,?> stereotype && stereotype.get("browserName") instanceof String name)browsers.add(name);
   }
  }
  return browsers;
 }
 public static void awaitNodes()throws Exception {
  Set<String> required=new HashSet<>();for(String b:Config.get("grid.required.browsers").split(","))required.add(b.trim());
  HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
  String url=Config.get("grid.url").replaceAll("/+$","")+"/status";
  long deadline=System.nanoTime()+Duration.ofSeconds(120).toNanos();String last="No response";
  while(System.nanoTime()<deadline){
   try{
    var request=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build();
    var response=client.send(request,HttpResponse.BodyHandlers.ofString());
    Set<String> available=registeredBrowsers(response.body());last="HTTP "+response.statusCode()+"; nodes="+available;
    if(response.statusCode()==200 && available.containsAll(required)){System.out.println("GRID READY: "+available);return;}
   }catch(InterruptedException e){Thread.currentThread().interrupt();throw e;}catch(Exception e){last=e.toString();}
   Thread.sleep(1000);
  }
  throw new IllegalStateException("Grid nodes not ready at "+url+"; required="+required+"; last="+last);
 }
}
