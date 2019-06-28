
package com.loeffler.webspider;

import com.loeffler.utilitylibrary.Logging.Logger;
import com.loeffler.utilitylibrary.Tools.Timer;

/**
 *  <p><strong>WebSpiderLeg</strong></p>
 *  <em>@author</em>  John Loeffler
 *  
 *  <strong>Contact</strong> 
 *    <em>@Email</em>     John.Loeffler@gmail.com
 *    <em>@Twitter</em>   @ThisDotJohn
 *    <em>@LinkedIn</em>  LinkedIn.com/in/JohnLoeffler
 *    <em>@Github</em>    github.com/JohnLoeffler
 *    <em>@Website</em>   JohnLoeffler.com
 */
public abstract class WebSpiderLeg extends Thread{
  protected static Logger LOG = Logger.GetInstance();
  protected static final String USER_AGENT="Mozilla/5.0 (Windows NT 6.1; WOW64)"
    + "AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
  
  /* DATA MEMBERS */
  protected boolean       bActive, bAlive;
  protected WebSpider     Body;
  protected String        Url;
  protected long          RunningTime;
  protected Timer         RunningTimer;
  
  /* Standard object setters, getters, constructor, and finalize override */
  /**
   *  Parameterized Constructor
   *  @param url  The url to process
   *  @param body A reference to the WebSpider creating the WebSpiderLeg
   */
  public  WebSpiderLeg(String url, WebSpider body){
    Url         = url; 
    Body        = body; 
    bAlive      = true; 
    bActive     = false;
    RunningTime = 0;
    RunningTimer= new Timer();
  }
  /**
   *  Destructor for WebSpiderLeg
   *  @throws Throwable Shouldn't throw anything
   */
  @Override
  protected void      finalize() throws Throwable {
    Url   = null;
    Body  = null;
  }
  /**
   *  Sets whether WebSpiderLeg is actively processing a url
   *  @param bActive True if active, false if not
   */
  public    void      setActive(boolean bActive){this.bActive = bActive;}
  /**
   *  Sets whether the WebSpiderLeg has completed if processing of a url
   *  @param bAlive True if WebSpiderLeg is currently processing or waiting to 
   *    process a url, false the WebSpiderLeg has processed its url and is 
   *    awaiting to either be destroyed or repurposed for another url
   */
  public    void      setAlive(boolean bAlive) {this.bAlive = bAlive;}
  /**
   *  Sets the reference to the WebSpider to which this WebSpiderLeg belongs
   *  @param Body A reference to WebSpider 
   */
  public    void      setBody(WebSpider Body){this.Body = Body;}
  /**
   *  Sets the url that the WebSpiderLeg is going to process
   *  @param Url A String of the url to process
   */
  public    void      setUrl(String Url) {this.Url = Url;}
  /**
   *  Returns whether the leg is actively processing a url
   *  @return True if Process() has been called but hasn't returned, else False
   */
  public    boolean   IsLegActive(){return bActive;}
  /**
   *  Returns whether the leg is currently alive and shouldn't be discarded
   *  @return True if waiting to run or running, false if work is complete
   */
  public    boolean   IsLegAlive(){return bAlive;}
  /**
   *  Returns the url the leg is assigned to
   *  @return a String of the url
   */
  public    String    GetURL(){return Url;}
  /**
   * Returns the time taken to process the url
   * @return A long of the running time taken to process the url
   */
  public    long      GetRunningTime(){return RunningTime;}

  /* Abstract WebSpiderLeg methods for implementations to define accordingly  */
  /**
   *  Abstract process function that will take some action on url according to 
   *    implementation
   *  @param o  An Object parameter in case something is needed to process url
   *  @return An Object is returned according to implementation
   */
  abstract  protected Object    ProcessURL(Object o);
  /**
   * Abstract method to collect the result of WebSpiderLegs processing of url
   * @return  An object containing the results of the url processing
   */
  abstract  public    Object    ReportResults();
  
  /* For testing purposes only */
  protected WebSpider GetBody(){return Body;}
}