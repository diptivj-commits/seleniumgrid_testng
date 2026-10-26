package com.automation.driver;
import com.automation.config.Config;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;
public final class DriverFactory {
 private static final ThreadLocal<WebDriver> DRIVER=new ThreadLocal<>();
 private DriverFactory(){}
 public static void start(String browser)throws Exception {
  boolean headless=Boolean.parseBoolean(Config.get("headless"));
  MutableCapabilities options;
  switch(browser.toLowerCase(Locale.ROOT)) {
   case "chrome":ChromeOptions c=new ChromeOptions();c.addArguments("--window-size=1440,900");if(headless)c.addArguments("--headless=new");options=c;break;
   case "firefox":FirefoxOptions f=new FirefoxOptions();if(headless)f.addArguments("-headless");options=f;break;
   default:throw new IllegalArgumentException("Supported browsers: chrome, firefox");
  }
  WebDriver driver=new RemoteWebDriver(URI.create(Config.get("grid.url")).toURL(),options);
  DRIVER.set(driver);
  driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(Config.positiveInt("page.load.seconds")));
  System.out.println("START browser="+browser+" thread="+Thread.currentThread().getName()+" session="+((RemoteWebDriver)driver).getSessionId());
 }
 public static WebDriver get(){WebDriver d=DRIVER.get();if(d==null)throw new IllegalStateException("No browser for current thread");return d;}
 public static boolean active(){return DRIVER.get()!=null;}
 public static void stop(){try{if(DRIVER.get()!=null)DRIVER.get().quit();}finally{DRIVER.remove();}}
}
