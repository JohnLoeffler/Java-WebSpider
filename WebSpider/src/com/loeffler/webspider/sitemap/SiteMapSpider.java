
package com.loeffler.webspider.sitemap;

import com.loeffler.utilitylibrary.Statics;
import com.loeffler.webspider.WebSpider;
import com.loeffler.webspider.WebSpiderLeg;
import com.loeffler.webspider.sitemap.siteobjects.Hyperlink;
import com.loeffler.webspider.sitemap.siteobjects.PageData;
import com.loeffler.webspider.sitemap.siteobjects.SiteMap;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  <p><strong>SiteMapSpider</strong></p>
 *  <em>@author</em>  John Loeffler
 *  
 *  <strong>Contact</strong> 
 *    <em>@Email</em>     John.Loeffler@gmail.com
 *    <em>@Twitter</em>   @ThisDotJohn
 *    <em>@LinkedIn</em>  LinkedIn.com/in/JohnLoeffler
 *    <em>@Github</em>    github.com/JohnLoeffler
 *    <em>@Website</em>   JohnLoeffler.com
 */
public class SiteMapSpider extends WebSpider{
  /* DATA MEMBERS */
  private SiteMap         SiteMap;
  private String          DomainUrl;
  /**
   * Initializes the SiteMapSpider by calling the WebSpider Init(), and if okay,
   *  creates the SiteMap structure it will use to build the website map
   * @param pages Maximum number of pages 
   * @param legs  Maximum number of concurrent legs
   * @param tout  Maximum timeout limit for the SiteMapSpider
   * @return True if initialized, false if there is an error
   */
  public boolean  Init(int pages, int legs, int tout, String domainUrl) {
    if(super.Init(pages, legs, tout)){
      this.SiteMap  = new SiteMap();
      this.DomainUrl=domainUrl;
    }else{
      return false;
    }
    return true;
  }
  /**
   * Gets the SiteMapSpider's SiteMap
   * @return A SiteMap
   */
  public SiteMap  GetSiteMap(){return SiteMap;}
  /**
   *  Starting from url or from the first url read from file, crawl a website 
   *    and construct a map of the site, with each page and each internal href
   *    found on the page
   *  @param url  The starting url
   *  @return the number of total pages processed
   * @throws Exception if Spider hasn't been initialized
   */
  @Override
  public long     BeginCrawl(String url){
    this.Url = url;
    if(!this.isInitialized()){
      return 0L;
      //throw new Exception("WebSpider hasn't been initialized yet!");
    }
    
    boolean finishedCrawl = false;
    
    //  Main Crawl Loop
    do{
      String currentUrl = null;
      try {
        //  Check for pending URLs
        currentUrl = this.NextURL();
      } catch (Exception e) {
        LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),String.format(
          "Exception thrown while attempting to get nextURL(): %s", 
            e.getMessage()), 3);
      }
      
      int choice = 0;
      try {
        //  Based on currentUrl value make appropriate choice for next instruction
        choice = DecisionTree(currentUrl);
      } catch (Exception e) {
        LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),String.format(
          "Exception thrown while attempting to use DecisionTree: %s", 
            e.getMessage()), 5);
      }
      try {
        switch(choice){
          case 1:
            if(TimeoutTimer.isActive()){ TimeoutTimer.Stop(); }
            MakeLegFromUrl(currentUrl, DomainUrl);
            break;
          case 2:
            if(TimeoutTimer.isActive()){ TimeoutTimer.Stop(); }
            MakeLegFromUrl(url, DomainUrl);
            break;
          case 3:
            if(TimeoutTimer.isActive()){ TimeoutTimer.Stop(); }
            MoveWaitingLegsToRunningLegs();
            break;
          case 4:
            if(!TimeoutTimer.isActive()){ TimeoutTimer.Start(); }
            Thread.sleep(this.TimeoutLimit/2);
            break;
          case 0:
          default:
            finishedCrawl = true;
        }
        //  If nothing left to do, return
        if(finishedCrawl){ return this.SiteMap.getSize(); }
      } catch (Exception e) {
        LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),String.format(
          "Exception thrown while attempting to Make choice on switch %d: %s",
            choice, e.getMessage()), 5);
      }  
        
      //  If SiteMapSpider has timed out
      if(this.TimeoutTimer.isActive()){
        if(this.hasTimedOut()){
          double time = ((double)TimeoutTimer.Elapsed())/1000.0;
          try{
            //  Try to Shutdown the Spider properly
            this.ShutdownSpider();
          }catch(Exception e){
            LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(), 
              String.format("Exception thrown while attempting to properly "
              + "shutdown spider: %s", e.getMessage()), 3);
          }
          LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(), 
            String.format("SiteMapSpider timed out after %f seconds", time), 1);
          return this.SiteMap.getSize();
        }
      }
    }while(this.hasURLsToVisit() || SiteMap.getSize() < 
      MaxPages-RunningLegs.size()-WaitingLegs.size());
    this.TimeoutTimer.Start();
    while(!RunningLegs.isEmpty()){
      if(!WaitingLegs.isEmpty()){
        MoveWaitingLegsToRunningLegs();
      }else{
        ClearFinished();
      }
      if(this.hasTimedOut()){
        try{
          double time = ((double)TimeoutTimer.Elapsed())/1000.0;
          this.ShutdownSpider();
          LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(), 
            String.format("SiteMapSpider timed out after %f seconds", time), 1);
        }catch(Exception e){
          return this.SiteMap.getSize();
        }
      }
    }
    return this.SiteMap.getSize();
  }
  /**
   *  Attempts to cast the returned value of method ReportResult as a PageData 
   *    object. If successful, attempts to push PageData's internal urls to 
   *    PagesToVisit list. Then, it attempts to add PageData to SiteMap.
   * @param wsl The WebSpiderLeg that is calling this method.
   */
  @Override
  public void     Notify(WebSpiderLeg wsl) {
    PageData pd = null;
    //  attempt to recover PageData from WebSpiderLeg
    try{
      pd = ((PageData)wsl.ReportResults());
    }catch(Exception e){
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
        String.format("Exception thrown while trying to cast WebSpiderLeg to"
        + " SiteMapSpiderLeg, which cast return value of ReportResults "
        + "to PageData. \n  %s", e.getMessage()),2);
      return;
    }
    //  Attempt to add hyperlinks from PageData to PagesToVisit
    try{
      if(pd == null){
        return; 
      }else{ 
        for(Hyperlink h : pd.HRefs){ 
          this.PushURLToLists(h.Url); 
        } 
      }
    }catch(Exception e){
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
        String.format("Exception thrown while attempting to push PageData"
        + " urls to PagesToVisit: %s", e.getMessage()), 2);
    }
    //  Attempt to add PageData to SiteMap
    try{
      this.SiteMap.addPage(pd);
    }catch(Exception e){
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
        String.format("Exception thrown while attempting to add PageData"
        + " to SiteMap: %s", e.getMessage()), 2);
    }
    //  Attempt to add PageData url to pages visited
    try{
      this.PagesVisited.add(pd.Url);
    }catch(Exception e){
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
        String.format("Exception thrown while attempting to : %s", 
          e.getMessage()), 1);
    }
  }
  /**
   * On passing url to method, attempts to create new SiteMapSpiderLeg from it.
   *  If successful, calls AddLeg and based on response returned from method,
   *  will either log an error, go to sleep for twice the AverageTimePerLeg, or
   *  just return.
   * @param url The url to make a leg out of.
   */
  private void    MakeLegFromUrl(String url, String domainUrl){
    WebSpiderLeg smsl = new SiteMapSpiderLeg(url, domainUrl, this);
    switch(AddLeg(smsl)){
      case 1:
        LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
          String.format("Error creating WebSpiderLeg from Url: %s", url));
        return;
      case -1:
        LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
          String.format("Waiting Queue is full, Sleeping for %d milliseconds",
            AverageTimePerLeg));
        try{
          Thread.sleep(AverageTimePerLeg*2);
        }catch(InterruptedException ie){
          LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
            String.format("Spider's sleep interrupted: %s",ie.getMessage()));
        }
        return;
      case -2:
        LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
          String.format("Only 1 leg running,sleep to give it time to finish"));
        try{
          Thread.sleep(30000);
        }catch(InterruptedException ie){
          LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
            String.format("Spider's sleep interrupted: %s",ie.getMessage()));
        }
        return;
      default:
        return;
    }
  }
  /**
   *  Takes currentUrl and makes a decision based on WebSpider conditions, these
   *    are: 
   *  @param currentUrl The currentUrl exactly as assigned by NextURL(). 
   *          If nextURL() assigned null, this method assigns the url assigned 
   *          to WebSpider when initialized to currentURL, so currentURL should
   *          always be used if '1' is returned by method.
   *  @return A decision about how to proceed based on current conditions. 
   *          Possible options are:
   *            0 - Crawl is over, return, 1 - Make leg from currentURL, 
   *            2 - Make leg from Paramurl,3 - Move WaitingLegs to RunningLegs
   *            4 - sleep for AverageTimePerLeg to give legs time to add urls
   */
  protected int   DecisionTree(String currentUrl){
    if(currentUrl != null){ //  {I}
      if(!this.PagesVisited.contains(Url)&&!this.PagesToVisit.contains(Url)){ 
        this.PagesToVisit.add(Url);
      }
      return 1; /*  Make new leg from currentUrl */
    }else{                  //  {II}
      if(PagesVisited.contains(this.Url)){  // [A]
        if(!RunningLegs.isEmpty()){             //  [1]
          if(RunningLegs.size() < MaxLegs){         //  (a)
            if(!WaitingLegs.isEmpty()){                 //  (i)
              return 3; /* Move WaitingLegs to RunningLegs */
            }else{                                      //  (ii)
              return 4;/*Sleep for AverageTimePerLeg,give legs time to add url*/
            }
          }else{                                    //  (b)
            return 4;/*Sleep for AverageTimePerLeg,give legs time to add url*/
          }
        }else if(!WaitingLegs.isEmpty()){       //  [2]
          return 3; /* Move WaitingLegs to RunningLegs */
        }else{                                  //  [3]  
          return 0; /* No more to be done, Crawl is finished */
        }
      }else{                                //  [B]
        return 2; /*  Make new leg from currentUrl */
      }
    }
  }
}
