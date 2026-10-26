package com.automation.base;
import com.automation.config.Config;
import com.automation.driver.DriverFactory;
import com.automation.support.GridWait;
import org.openqa.selenium.*;
import org.testng.*;
import org.testng.annotations.*;
import java.nio.file.*;
public class BaseTest {
 protected WebDriver getDriver(){return DriverFactory.get();}
 @BeforeSuite(alwaysRun=true) public void waitForGrid()throws Exception{GridWait.awaitNodes();}
 @BeforeMethod(alwaysRun=true)
 @Parameters("browser")
 public void startBrowser(String browser)throws Exception{DriverFactory.start(browser);getDriver().get(Config.get("app.base.url"));}
 @AfterMethod(alwaysRun=true)
 public void stopBrowser(ITestResult result){
  if(!DriverFactory.active())return;
  try{
   if(!result.isSuccess())try{
    Path folder=Paths.get("reports","screenshots");Files.createDirectories(folder);
    Path file=Files.createTempFile(folder,result.getName()+"-",".png");
    Files.write(file,((TakesScreenshot)getDriver()).getScreenshotAs(OutputType.BYTES));Reporter.log("Screenshot: "+file.toAbsolutePath(),true);
   }catch(Exception e){System.err.println("Screenshot failed: "+e.getMessage());}
  }finally{DriverFactory.stop();}
 }
}
