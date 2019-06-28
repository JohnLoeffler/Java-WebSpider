package com.loeffler.webspider;

import com.loeffler.utilitylibrary.Logging.Logger;
import com.loeffler.utilitylibrary.Statics;
import com.loeffler.utilitylibrary.Tools.Timer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *  <p><strong>WebSpider</strong></p>
 *  <em>@author</em>  John Loeffler
 *  
 *  <strong>Contact</strong> 
 *    <em>@Email</em>     John.Loeffler@gmail.com
 *    <em>@Twitter</em>   @ThisDotJohn
 *    <em>@LinkedIn</em>  LinkedIn.com/in/JohnLoeffler
 *    <em>@Github</em>    Github.com/JohnLoeffler
 *    <em>@Bitbucket</em> Bitbucket.org/JohnLoeffler
 */
public abstract class WebSpider {
  protected static Logger LOG = Logger.GetInstance();
  
  /* DATA MEMBERS */
  protected int               MaxPages, MaxLegs, TimeoutLimit;

  volatile  protected int     LegsSpawned;
  volatile  protected long    AverageTimePerLeg;
  protected List<String>      PagesVisited, PagesToVisit;       
  protected List<WebSpiderLeg>RunningLegs, WaitingLegs;
  protected boolean           bInitialized;
  protected String            Url;
  protected Timer             TimeoutTimer;
  
  /* MEMBER METHODS */
  /* Webspider methods for WebSpider Set-Up and Destruction */
  /**
   *  Default constructor
   */
  public                      WebSpider(){
    this.PagesVisited       = new CopyOnWriteArrayList<>();
    this.PagesToVisit       = new CopyOnWriteArrayList<>();
    this.RunningLegs        = new CopyOnWriteArrayList<>();
    this.WaitingLegs        = new CopyOnWriteArrayList<>();
    this.MaxLegs            = 0;
    this.MaxPages           = 0;
    this.bInitialized       = false;
    this.Url                = "";
    this.LegsSpawned        = 0;
    this.AverageTimePerLeg  = 1;
    this.ReadToVisit();
    this.ReadVisited();
  }
  /**
   * Initializes the WebSpider
   * @param pages Maximum number of pages to visit
   * @param legs  Maximum number of concurrent legs
   * @param tout  Maximum timeout to idle before shutting down
   * @return  true if everything is initialized OK, false if Init() fails
   */
  public    boolean           Init(int pages, int legs, int tout){
    try{
      this.MaxPages     = pages;
      this.MaxLegs      = legs;
      this.TimeoutLimit = tout;
      this.TimeoutTimer = new Timer();
    }catch(Exception ex){
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
        String.format("Error initializing WebSpider: %s",ex.getMessage()), 5);
      this.bInitialized = false;
      return false;
    }
    this.bInitialized = true;
    return true;
  }
  /**
   *  Attempts to releases shutdown any running threads, prints urls from lists
   *    if any remain, and attempts to clear memory allocated to Threads and
   *    List objects;
   *  @throws Throwable All Exceptions caught in method, so shouldn't throw any
   */
  @Override
  protected void              finalize() throws Throwable {
    try{ 
      if(!this.RunningLegs.isEmpty()){
        for(int i = 0; i < RunningLegs.size();){
          try{  
            this.PagesToVisit.add(RunningLegs.get(i).GetURL());
            Thread t = RunningLegs.remove(i);
            t.interrupt();
            t = null;
          }catch(SecurityException se){

          }
        }
        this.RunningLegs = null;
        System.gc();
      }
      if(!this.WaitingLegs.isEmpty()){
        for(int i = 0; i < WaitingLegs.size();){
          try{  
            this.PagesToVisit.add(WaitingLegs.get(i).GetURL());
            Thread t = WaitingLegs.remove(i);
            t.interrupt();
            t = null;
          }catch(SecurityException se){

          }
        }
        this.WaitingLegs = null;
        System.gc();
      }
      if(!this.PagesVisited.isEmpty()){
        try{
          this.PrintVisited();
          this.PagesVisited.clear();
          this.PagesVisited = null;
        }catch(Exception ex){

        }
      }
      if(!this.PagesToVisit.isEmpty()){
        try{
          this.PrintToVisit();
          this.PagesToVisit.clear();
          this.PagesToVisit = null;
        }catch(Exception ex){

        }
      }
    }catch(Exception ex){
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
        String.format("Exception thrown while WebSpider decontructing: %s",
          ex.getMessage()), 3);
   }
    super.finalize();
  }
  
  /* Getters related to the current status of the WebSpider */
  /**
   * Queries whether WebSpider has reached the maximum number of running legs
   * @return True if maximum has been reached, else false
   */
  public boolean              hasReachedMaxLegs(){return RunningLegs.size()<MaxLegs;}
  /**
   * Queries whether WebSpider has exceeded its timeout limit
   * @return True if timer elapsed time exceeds the timeout limit, else false
   */
  public boolean              hasTimedOut(){
    if(TimeoutTimer.isActive()){
      return TimeoutTimer.Elapsed()>TimeoutLimit;
    }else{
      return false;
    }
  }
  /**
   * Queries whether WebSpider has filled its waiting leg queue to capacity
   * @return True if waiting queue is at or exceeds its capacity, else false
   */
  public boolean              hasFullLegQueue(){return WaitingLegs.size()>(MaxLegs*3);}
  /**
   * Queries whether WebSpider has more urls to visit in its queue
   * @return True if urls in queue that still need to be visited, else false
   */
  public boolean              hasURLsToVisit(){return !PagesToVisit.isEmpty();}
  /**
   * Returns whether WebSpider has been initialized
   * @return 
   */
  public boolean              isInitialized(){return bInitialized;}
  /**
   * Method that saves urls held by legs in lists, clears the legs lists, 
   *  and prints url lists to file, calling the systems garbage collector when
   *  finished. 
   */
  public void                 ShutdownSpider(){
    for(int i = 0; i < RunningLegs.size();){
      try{
        WebSpiderLeg wsl = RunningLegs.remove(i);
        this.PagesToVisit.add(wsl.GetURL());
        wsl.interrupt();
        wsl = null;
      }catch(Exception ex){
        
      }
    }
    for(int i = 0; i < WaitingLegs.size();){
      try{
        WebSpiderLeg wsl = WaitingLegs.remove(i);
        this.PagesToVisit.add(wsl.GetURL());
        wsl = null;
      }catch(Exception ex){
        
      }
    }
    try{
      this.PrintToVisit();
      this.PrintVisited();
    }catch(Exception ex){
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
        String.format("Error in ShutdownSpider printing urls to file: %s",
          ex.getMessage()),3);
    }
    System.gc();
  }
  /* WebSpider methods related to basic web crawling */
  /**
   * Gets the next available URL in the queue
   * @return  a String of the URL
   */
  protected String            NextURL(){
    String nextUrl = null;
    do{
      if(this.PagesToVisit.isEmpty()){
        return nextUrl;
      }else{
        nextUrl = this.PagesToVisit.remove(0);
        if(this.PagesVisited.contains(nextUrl)){
          nextUrl = null;
        }else{
          return nextUrl;
        }
      }
    }while(!(this.PagesToVisit.isEmpty()));
    return nextUrl;
  }
  /**
   * Pushes a url onto the list of urls to visit
   * @param url The url to push
   */
  protected void              PushURLToLists(String url){
    if(!this.PagesToVisit.contains(url)){
      if(this.PagesVisited.contains(url)){
        return;
      }else{
        this.PagesToVisit.add(url);
      }
    }
  }
  
  /* WebSpider methods related to basic leg maintanence */
  /**
   *  Adds a new WebSpiderLeg to the appropriate Leg List, starting Leg threads
   *    as they are added to RunningLeg List
   *  @param wsl the WebSpiderLeg to add to the List
   *  @return An int status code for the WebSpider to tell if there was an 
   *            error (#), everything is fine (0), or that it needs to wait a
   *            length of time to allow some of the threads in the RunningLeg 
   *            list to finish before trying to run more more legs (-1)
   */
  protected int               AddLeg(WebSpiderLeg wsl){
    try{
      //  Add new leg to back of the waiting queue
      this.WaitingLegs.add(wsl);
      
      //  Move Legs around as necessary
      MoveWaitingLegsToRunningLegs();
      
      //  Check to make sure WaitingLegs queue isn't getting too big.
      if(this.WaitingLegs.size() >= (this.MaxLegs*3)){
        LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
          String.format("WaitingLegs exceeding Thread Buffer; signalling to wait "
            + "for %f milliseconds", AverageTimePerLeg), 1);
        return -1;
      }
      if(this.RunningLegs.size() == 1 && this.WaitingLegs.size() == 0){
        return -2;
      }
      return 0;
    }catch(Exception e){
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
        String.format("Exception thrown in AddLeg(): %s", e.getMessage()));
      return 1;
    }
  }
  /**
   * Calls ClearFinished() to clean out dead legs, then takes as many legs as 
   *  are available, up to MaxPages, calls start on threads, then adds them to
   *  running
   */
  protected synchronized void MoveWaitingLegsToRunningLegs(){
    ClearFinished();
    while(RunningLegs.size() < MaxLegs && !WaitingLegs.isEmpty()){
      try{
        WebSpiderLeg wsl = WaitingLegs.remove(0);
        wsl.start();
        RunningLegs.add(wsl);
      }catch(Exception e){
        LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
          String.format("Exception thrown moving WaitingLeg(0) to RunningLeg:"
          + "\n  %s", e.getMessage()));
      }
    }
  }
  /**
   *  Synchronized method that clears out any WebSpiderLegs from running that
   *    are no longer alive
   */
  protected synchronized void ClearFinished(){
    for(int i = 0; i < this.RunningLegs.size();i++){
      if(!this.RunningLegs.get(i).IsLegAlive()){
        try{
          WebSpiderLeg wsl = this.RunningLegs.remove(i);
          i--;
          AverageTimePerLeg += wsl.GetRunningTime();
          AverageTimePerLeg /= 2;
        }catch(Exception ex){
          LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
            String.format("Exception thrown while clearing out legs. Should be "
            + "okay if out of bounds exception,\n  otherwise, there is an "
            + "issue: %s", ex.getMessage()));
        }
      }
    }
    System.gc();
  }
  
  /* Abstract WebSpider methods that must be implemented by derived Spiders */
  /**
   * Begins the WebSpider crawling process
   * @param url String of the URL to begin crawling from
   * @return  A long of the number of sites crawled
   */
  abstract public long        BeginCrawl(String url) throws Exception;
  /**
   *  Used by the WebSpider's WebSpiderLegs to report that they are finished
   *  @param wsl  the reporting leg
   */
  abstract public void        Notify(WebSpiderLeg wsl) throws Exception;
  
  /* WebSpider methods related to I/O File Operations */
  /**
   * Reads in file of visited pages
   * @return the number of pages read in
   */
  protected int               ReadVisited(){
    File file = new File("VisitedPages.txt");
    if(file.exists()){
      try(BufferedReader br = new BufferedReader(new FileReader(file))){
        String url = br.readLine();
        while(url != null){
          PagesVisited.add(url);
          url = br.readLine();
        }
      }catch(IOException ioe){
        System.err.println("Error reading in Visited Pages into spider");
      }
    }
    return PagesVisited.size();
  }
  /**
   * Read in urls to visit from file
   * @return  the number of urls read in
   */
  protected int               ReadToVisit(){
    File file = new File("PagesToVisit.txt");
    if(file.exists()){
      try(BufferedReader br = new BufferedReader(new FileReader(file))){
        String url = br.readLine();
        while(url != null){
          PagesToVisit.add(url);
          url = br.readLine();
        }
      }catch(IOException ioe){
        //System.err.println("Error reading in Pages to Visit into spider");
      }
    }
    return PagesVisited.size();
  }
  /**
   * Prints urls of visited pages to file
   */
  protected void              PrintVisited(){
    File visitedFile = new File("VisitedPages.txt");
    try(BufferedWriter bw=new BufferedWriter(new FileWriter(visitedFile,true))){
      for(int i = 0; i < PagesVisited.size();){
        String url =  PagesVisited.remove(i);
        bw.write(url);
        bw.newLine();
      }
    }catch(IOException ioe){
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
        String.format("Error printing PagesVisited List: %s",ioe.getMessage()));
    }
  }
  /**
   * Prints urls left to visit to file
   */
  protected void              PrintToVisit(){
    File ToVisitFile = new File("PagesToVisit.txt");
    try(BufferedWriter bw = new BufferedWriter(new FileWriter(ToVisitFile))){
      for(int i = 0; i < PagesToVisit.size();){
        String url =  PagesToVisit.remove(i);
        bw.write(url);
        bw.newLine();
      }
    }catch(IOException ioe){
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(),
        String.format("Error printing PagesToVisit List: %s",ioe.getMessage()));
    }
  }
}